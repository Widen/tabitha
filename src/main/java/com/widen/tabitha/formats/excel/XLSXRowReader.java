package com.widen.tabitha.formats.excel;

import com.widen.tabitha.PagedReader;
import com.widen.tabitha.Row;
import com.widen.tabitha.Header;
import com.widen.tabitha.Variant;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

/**
 * Reads rows from an Office Open XML spreadsheet.
 */
public class XLSXRowReader implements PagedReader {
    private final OPCPackage opcPackage;
    private final ReadOnlySharedStringsTable stringsTable;
    private final Iterator<InputStream> sheetIterator;
    private SpreadsheetMLReader sheetReader;
    private Header header;

    public static XLSXRowReader open(File file) throws IOException {
        try {
            return new XLSXRowReader(OPCPackage.open(file));
        } catch (InvalidFormatException e) {
            throw new IOException(e);
        }
    }

    public static XLSXRowReader open(InputStream inputStream) throws IOException {
        try {
            return new XLSXRowReader(OPCPackage.open(inputStream));
        } catch (InvalidFormatException e) {
            throw new IOException(e);
        }
    }

    private XLSXRowReader(OPCPackage opcPackage) throws IOException {
        this.opcPackage = opcPackage;

        try {
            stringsTable = new ReadOnlySharedStringsTable(opcPackage);
            XSSFReader xssfReader = new XSSFReader(opcPackage);
            sheetIterator = xssfReader.getSheetsData();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public int getPageIndex() {
        return 0;
    }

    @Override
    public boolean nextPage() throws IOException {
        if (sheetReader != null) {
            sheetReader.close();
            sheetReader = null;
        }

        // Each page has its own header.
        header = null;

        if (sheetIterator.hasNext()) {
            InputStream inputStream = sheetIterator.next();
            try {
                sheetReader = new SpreadsheetMLReader(stringsTable, inputStream);
                return true;
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }

        return false;
    }

    @Override
    public Optional<Row> read() throws IOException {
        if (header == null) {
            header = readHeader();

            if (header == null) {
                return Optional.empty();
            }
        }

        Collection<Variant> values = readValues();

        if (values != null) {
            return Optional.of(Row.create(values).withHeader(header));
        }

        return Optional.empty();
    }

    @Override
    public void close() throws IOException {
        if (sheetReader != null) {
            sheetReader.close();
            sheetReader = null;
        }

        opcPackage.close();
    }

    private Header readHeader() throws IOException {
        Collection<Variant> values = readValues();

        if (values != null) {
            Header.Builder builder = Header.builder();

            for (Variant value : values) {
                builder.add(value.toString());
            }

            return builder.build();
        }

        return null;
    }

    private Collection<Variant> readValues() throws IOException {
        if (sheetReader == null) {
            if (!nextPage()) {
                return null;
            }
        }

        try {
            return sheetReader.read();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * Parses an XML stream of an OpenXML sheet in a lazy manner.
     */
    private static class SpreadsheetMLReader implements Closeable {
        static final String NS_SPREADSHEETML = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";

        private final InputStream inputStream;
        private final XMLStreamReader reader;
        private final ReadOnlySharedStringsTable stringsTable;
        private final StringBuilder valueBuilder = new StringBuilder();

        SpreadsheetMLReader(ReadOnlySharedStringsTable strings, InputStream stream) throws XMLStreamException {
            inputStream = stream;
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            reader = xmlInputFactory.createXMLStreamReader(stream);
            stringsTable = strings;
        }

        /**
         * Reads the next row in the spreadsheet as a variant list.
         *
         * @return The cell values or null if no more rows exist.
         * @throws XMLStreamException Thrown if any XML error occurs.
         */
        public Collection<Variant> read() throws XMLStreamException {
            while (reader.hasNext()) {
                int event = reader.next();

                // Start of a new row.
                if (event == XMLStreamConstants.START_ELEMENT && elementMatches("row")) {
                    return parseRow();
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
            ArrayList<Variant> cells = new ArrayList<>();

            while (reader.hasNext()) {
                int event = reader.next();

                // The start of a new cell.
                if (event == XMLStreamConstants.START_ELEMENT && elementMatches("c")) {
                    Variant cell = parseCell();
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

            // Figure out the cell data type.
            for (int i = 0; i < reader.getAttributeCount(); ++i) {
                if ("t".equals(reader.getAttributeLocalName(i))) {
                    cellType = reader.getAttributeValue(i);
                    break;
                }
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

                    while (reader.hasNext()) {
                        event = reader.next();

                        if (event == XMLStreamConstants.CHARACTERS) {
                            valueBuilder.append(reader.getText());
                        } else if (event == XMLStreamConstants.END_ELEMENT && elementMatches("v")) {
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
                            } else {
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
                            stringBuilder.append(reader.getTextCharacters());
                        } else if (event == XMLStreamConstants.END_ELEMENT && elementMatches("t")) {
                            break;
                        }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT && elementMatches("is")) {
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
