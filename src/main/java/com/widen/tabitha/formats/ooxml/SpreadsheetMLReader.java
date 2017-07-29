package com.widen.tabitha.formats.ooxml;

import com.widen.tabitha.Variant;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Parses an XML stream of an OpenXML sheet in a lazy manner.
 * <p>
 * This is an internal helper class to support the
 */
class SpreadsheetMLReader implements Closeable {
    public static final String NS_SPREADSHEETML = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";

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
     * @throws XMLStreamException
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
