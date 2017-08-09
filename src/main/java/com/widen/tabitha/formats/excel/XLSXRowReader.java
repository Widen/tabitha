package com.widen.tabitha.formats.excel;

import com.widen.tabitha.PagedReader;
import com.widen.tabitha.Row;
import com.widen.tabitha.Schema;
import com.widen.tabitha.Variant;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private Schema schema;

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

        // Each page has its own schema.
        schema = null;

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
        if (schema == null) {
            schema = readSchema();

            if (schema == null) {
                return Optional.empty();
            }
        }

        Collection<Variant> values = readValues();

        if (values != null) {
            return Optional.of(schema.createRow(values));
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

    private Schema readSchema() throws IOException {
        Collection<Variant> values = readValues();

        if (values != null) {
            Schema.Builder builder = Schema.builder();

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
}
