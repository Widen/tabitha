package com.widen.tabitha.formats.delimited;

import com.opencsv.CSVWriter;
import com.widen.tabitha.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;

/**
 * Writes rows of values to a delimiter-separated text file.
 */
public class DelimitedRowWriter implements RowWriter {
    private CSVWriter writer;
    private boolean headersWritten = false;

    public DelimitedRowWriter(OutputStream outputStream, DelimitedFormat format) {
        this.writer = new CSVWriter(
            new OutputStreamWriter(outputStream, format.charset),
            format.delimiter,
            format.quoteCharacter,
            format.escapeCharacter
        );
    }

    @Override
    public void write(Row row) throws IOException {
        if (!headersWritten) {
            row.header().ifPresent(header -> writer.writeNext(header.toArray()));
            headersWritten = true;
        }

        String[] cells = row.cells().map(Variant::toString).toArray(String[]::new);

        writer.writeNext(cells);
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }
}
