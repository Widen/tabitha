package com.widen.tabitha.io;

import com.opencsv.CSVReader;
import com.widen.tabitha.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

/**
 * Reads a CSV file into rows of strings.
 */
public class CsvRowReader implements RowReader
{
    private CSVReader reader;
    private ColumnIndex columnIndex;

    public CsvRowReader(Reader reader)
    {
        this.reader = new CSVReader(reader);
    }

    @Override
    public Optional<Row> read() throws IOException
    {
        if (columnIndex == null)
        {
            readHeaders();
        }

        String[] columns = reader.readNext();
        if (columns == null)
        {
            return Optional.empty();
        }

        Value[] values = Utils.mapArray(columns, Value.String::new);

        return Optional.of(new Row(columnIndex, values));
    }

    private void readHeaders() throws IOException
    {
        String[] columns = reader.readNext();
        ColumnIndex.Builder builder = new ColumnIndex.Builder();

        for (String column : columns)
        {
            builder.addColumn(column);
        }

        columnIndex = builder.build();
    }
}
