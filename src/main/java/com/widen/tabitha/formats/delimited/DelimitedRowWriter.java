package com.widen.tabitha.formats.delimited;

import com.opencsv.CSVWriter;
import com.widen.tabitha.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

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

        String[] cells = Utils.mapArray(row.toArray(), String.class, Variant::toString);

        int index = 0;
        for (Variant cell : row) {
            cells[index] = cell.toString();
            ++index;
        }

        writer.writeNext(cells);
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }
}