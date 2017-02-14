package com.widen.tabitha.io;

import com.opencsv.CSVReader;
import com.widen.tabitha.ColumnIndex;
import com.widen.tabitha.Row;

import java.io.IOException;
import java.util.Optional;

/**
 * Reads a CSV file into rows of strings.
 */
public class CsvReader implements Reader<String>
{
	private CSVReader reader;
	private ColumnIndex columnIndex;

	public CsvReader(java.io.Reader reader)
	{
		this.reader = new CSVReader(reader);
	}

	@Override
	public Optional<Row<String>> read() throws IOException
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

		return Optional.of(new Row<>(columnIndex, columns));
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
