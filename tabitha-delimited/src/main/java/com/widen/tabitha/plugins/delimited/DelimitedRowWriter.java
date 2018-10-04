package com.widen.tabitha.plugins.delimited;

import com.opencsv.CSVWriter;
import com.widen.tabitha.Variant;
import com.widen.tabitha.writer.RowWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Writes rows of values to a delimiter-separated text file.
 */
public class DelimitedRowWriter implements RowWriter {
    private CSVWriter writer;

    public DelimitedRowWriter(OutputStream outputStream, DelimitedFormat format) {
        this.writer = new CSVWriter(
            new OutputStreamWriter(outputStream, format.charset),
            format.delimiter,
            format.quoteCharacter,
            format.escapeCharacter
        );
    }

    @Override
    public void write(List<Variant> cells) {
        String[] cols = cells
            .stream()
            .map(Object::toString)
            .map(String.class::cast)
            .toArray(String[]::new);

        writer.writeNext(cols);
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }
}
