package com.widen.tabitha.plugins.excel;

import com.widen.tabitha.Variant;
import com.widen.tabitha.reader.ReaderOptions;
import com.widen.tabitha.reader.Row;
import com.widen.tabitha.reader.RowReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Reads rows from an Office Open XML spreadsheet.
 */
@Slf4j
public class XLSXRowReader implements RowReader {
    private final ReaderOptions options;
    private final OPCPackage opcPackage;
    private final ReadOnlySharedStringsTable stringsTable;
    private final XSSFReader.SheetIterator sheetIterator;
    private SpreadsheetMLReader sheetReader;
    private long currentSheetIndex = -1;
    private String currentSheetName;

    /**
     * Open an XLSX file from the file system.
     *
     * @param path The path of the file to open.
     * @param options Options to pass to the reader.
     * @return A new row reader.
     */
    public static XLSXRowReader open(Path path, ReaderOptions options) throws IOException {
        try {
            return new XLSXRowReader(OPCPackage.open(path.toFile()), options);
        }
        catch (InvalidFormatException e) {
            throw new IOException(e);
        }
    }

    /**
     * Open an XLSX file from a stream.
     * <p>
     * Note that this can use a great deal more memory than {@link #open(Path, ReaderOptions)} as it will temporarily
     * read the entire stream to memory in order to inspect the zip archive.
     *
     * @param inputStream The stream to open.
     * @param options Options to pass to the reader.
     * @return A new row reader.
     */
    public static XLSXRowReader open(InputStream inputStream, ReaderOptions options) throws IOException {
        try {
            return new XLSXRowReader(OPCPackage.open(inputStream), options);
        }
        catch (InvalidFormatException e) {
            throw new IOException(e);
        }
    }

    private XLSXRowReader(OPCPackage opcPackage, ReaderOptions options) throws IOException {
        this.options = options != null ? options : new ReaderOptions();
        this.opcPackage = opcPackage;

        try {
            stringsTable = new ReadOnlySharedStringsTable(opcPackage);
            XSSFReader xssfReader = new XSSFReader(opcPackage);
            sheetIterator = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        }
        catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public Optional<Row> read() throws IOException {
        if (sheetReader == null) {
            if (!nextPage()) {
                return Optional.empty();
            }
        }

        try {
            while (sheetReader != null) {
                Row row = sheetReader.read();

                if (row != null) {
                    return Optional.of(row);
                }

                nextPage();
            }

            return Optional.empty();
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (sheetReader != null) {
            sheetReader.close();
            sheetReader = null;
        }

        opcPackage.revert();
    }

    private boolean nextPage() throws IOException {
        if (sheetReader != null) {
            sheetReader.close();
            sheetReader = null;
        }

        if (sheetIterator.hasNext()) {
            InputStream inputStream = sheetIterator.next();
            try {
                sheetReader = new SpreadsheetMLReader(inputStream);
                currentSheetIndex++;
                currentSheetName = sheetIterator.getSheetName();
                return true;
            }
            catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }

        return false;
    }

    /**
     * Parses an XML stream of an OpenXML sheet in a lazy manner.
     */
    private class SpreadsheetMLReader implements Closeable {
        static final String NS_SPREADSHEETML = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";

        private final InputStream inputStream;
        private final XMLStreamReader reader;
        private final StringBuilder valueBuilder = new StringBuilder();
        private long rowIndex = 0;
        private long cellColumn = 0;

        SpreadsheetMLReader(InputStream stream) throws XMLStreamException {
            inputStream = stream;
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            reader = xmlInputFactory.createXMLStreamReader(stream);
        }

        /**
         * Reads the next row in the spreadsheet as a variant list.
         *
         * @return The cell values or null if no more rows exist.
         * @throws XMLStreamException Thrown if any XML error occurs.
         */
        public Row read() throws XMLStreamException {
            while (reader.hasNext()) {
                int event = reader.next();

                // Start of a new row.
                if (event == XMLStreamConstants.START_ELEMENT && elementMatches("row")) {
                    // Ignore hidden rows.
                    if (!options.isIncludeHiddenRows() && "1".equals(reader.getAttributeValue(null, "hidden"))) {
                        continue;
                    }

                    try {
                        rowIndex = Long.parseLong(reader.getAttributeValue(null, "r"));
                    }
                    catch (NumberFormatException e) {
                        log.debug("Row is missing an index number!");
                        rowIndex++;
                    }

                    return Row.fromStream(currentSheetIndex, rowIndex - 1, parseRow().stream())
                        .withPageName(currentSheetName);
                }
            }

            // Could not find a row element.
            return null;
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }

        private Collection<Variant> parseRow() throws XMLStreamException {
            cellColumn = 0;
            ArrayList<Variant> cells = new ArrayList<>();

            while (reader.hasNext()) {
                int event = reader.next();

                // The start of a new cell.
                if (event == XMLStreamConstants.START_ELEMENT && elementMatches("c")) {
                    Variant cell = parseCell();

                    // Fill in any "missing" / blank cells.
                    while (cells.size() < cellColumn) {
                        cells.add(Variant.NONE);
                    }

                    if (cell != null) {
                        cells.add(cell);
                    }
                }

                // Reached the end of the row.
                else if (event == XMLStreamConstants.END_ELEMENT && elementMatches("row")) {
                    break;
                }
            }

            return cells;
        }

        private Variant parseCell() throws XMLStreamException {
            String cellType = "";
            Variant cellValue = null;
            String cellRefString = null;

            // Extract the cell data type and position ref.
            for (int i = 0; i < reader.getAttributeCount(); ++i) {
                if ("r".equals(reader.getAttributeLocalName(i))) {
                    cellRefString = reader.getAttributeValue(i);
                }

                if ("t".equals(reader.getAttributeLocalName(i))) {
                    cellType = reader.getAttributeValue(i);
                }
            }

            // Parse the cell ref string so we know what column the cell belongs to.
            if (cellRefString != null) {
                cellColumn = ParseHelpers.getColumnFromCellName(cellRefString);
            }

            // Get the cell value.
            while (reader.hasNext()) {
                int event = reader.next();

                // An inline string value. We know how this is structured, so we will simply ignore the cell type attribute
                // from earlier.
                if (event == XMLStreamConstants.START_ELEMENT && elementMatches("is")) {
                    cellValue = Variant.of(parseInlineString());
                }

                // Normal cell value. We'll need the cell type attribute to help us here.
                else if (event == XMLStreamConstants.START_ELEMENT && elementMatches("v")) {
                    valueBuilder.setLength(0);

                    // Get the (maybe) cell contents.
                    while (reader.hasNext()) {
                        event = reader.next();

                        if (event == XMLStreamConstants.CHARACTERS) {
                            valueBuilder.append(reader.getText());
                        }
                        else if (event == XMLStreamConstants.END_ELEMENT && elementMatches("v")) {
                            break;
                        }
                    }

                    switch (cellType) {
                        // Boolean type.
                        case "b":
                            cellValue = Variant.of(valueBuilder.charAt(0) == '0');
                            break;

                        // Shared string table string; the value is the index into the actual string value in the table.
                        case "s":
                            int idx = Integer.parseInt(valueBuilder.toString());
                            cellValue = Variant.of(stringsTable.getEntryAt(idx));
                            break;

                        // Error type. Basically an inline string.
                        case "e":
                            cellValue = Variant.of("ERROR:" + valueBuilder.toString());
                            break;

                        // Formula type. The <v> tag here will be the precomputed formula value, which is exactly what we
                        // want to return.
                        case "str":
                            cellValue = Variant.of(valueBuilder.toString());
                            break;

                        // Number type. We are going to ignore any special number formatting for performance purposes.
                        default:
                            // If there is a decimal, interpret as a floating point.
                            String valueString = valueBuilder.toString();
                            if (valueString.contains(".")) {
                                cellValue = Variant.of(Double.parseDouble(valueString));
                            }
                            else {
                                cellValue = Variant.of(Long.parseLong(valueString));
                            }
                            break;
                    }
                }

                // Reached the end of the cell.
                else if (event == XMLStreamConstants.END_ELEMENT && elementMatches("c")) {
                    break;
                }
            }

            return cellValue;
        }

        private String parseInlineString() throws XMLStreamException {
            StringBuilder stringBuilder = new StringBuilder();

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT && elementMatches("t")) {
                    while (reader.hasNext()) {
                        event = reader.next();

                        if (event == XMLStreamConstants.CHARACTERS) {
                            stringBuilder.append(reader.getText());
                        }
                        else if (event == XMLStreamConstants.END_ELEMENT && elementMatches("t")) {
                            break;
                        }
                    }
                }
                else if (event == XMLStreamConstants.END_ELEMENT && elementMatches("is")) {
                    break;
                }
            }

            return stringBuilder.toString();
        }

        private boolean elementMatches(String name) {
            if (reader.getNamespaceURI() == null || reader.getNamespaceURI().equals(NS_SPREADSHEETML)) {
                if (name.equals(reader.getLocalName())) {
                    return true;
                }
            }

            return false;
        }
    }
}
