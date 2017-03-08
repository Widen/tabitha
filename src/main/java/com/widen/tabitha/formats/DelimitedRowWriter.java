package com.widen.tabitha.formats;

import com.opencsv.CSVWriter;
import com.widen.tabitha.Row;
import com.widen.tabitha.RowWriter;
import com.widen.tabitha.Utils;
import com.widen.tabitha.Value;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Writes rows of values to a delimiter-separated text file.
 */
public class DelimitedRowWriter implements RowWriter
{
    private CSVWriter writer;
    private boolean headersWritten = false;

    public DelimitedRowWriter(OutputStream outputStream, DelimitedTextFormat format)
    {
        this(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), format);
    }

    public DelimitedRowWriter(Writer writer, DelimitedTextFormat format)
    {
        this.writer = new CSVWriter(writer, format.getDelimiter(), format.getQuoteCharacter(), format.getEscapeCharacter());
    }

    @Override
    public void write(Row row) throws IOException
    {
        if (!headersWritten)
        {
            writer.writeNext(row.columns().toArray());
            headersWritten = true;
        }

        String[] cells = Utils.mapArray(row.toArray(), Value::asString);

        int index = 0;
        for (Value value : row)
        {
            cells[index] = value.asString();
            ++index;
        }

        writer.writeNext(cells);
    }
}
