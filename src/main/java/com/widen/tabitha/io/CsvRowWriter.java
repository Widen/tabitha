package com.widen.tabitha.io;

import com.opencsv.CSVWriter;
import com.widen.tabitha.Row;
import com.widen.tabitha.Utils;
import com.widen.tabitha.Value;

import java.io.IOException;
import java.io.Writer;

/**
 * Writes rows of strings to a CSV file.
 */
public class CsvRowWriter implements RowWriter
{
    private CSVWriter writer;
    private boolean headersWritten = false;

    public CsvRowWriter(Writer writer)
    {
        this.writer = new CSVWriter(writer);
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
