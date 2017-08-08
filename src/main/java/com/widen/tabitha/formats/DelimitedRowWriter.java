package com.widen.tabitha.formats;

import com.opencsv.CSVWriter;
import com.widen.tabitha.Row;
import com.widen.tabitha.RowWriter;
import com.widen.tabitha.Utils;
import com.widen.tabitha.Variant;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Writes rows of values to a delimiter-separated text file.
 */
public class DelimitedRowWriter implements RowWriter {
    private CSVWriter writer;
    private boolean headersWritten = false;

    public DelimitedRowWriter(OutputStream outputStream, DelimitedTextFormat format) {
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
            writer.writeNext(Utils.mapArray(row.columns(), String.class, column -> column.name));
            headersWritten = true;
        }

        String[] cells = Utils.mapArray(row.values(), String.class, Variant::toString);

        int index = 0;
        for (Row.Cell cell : row) {
            cells[index] = cell.value.toString();
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
