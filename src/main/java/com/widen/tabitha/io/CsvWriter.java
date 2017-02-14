package com.widen.tabitha.io;

import com.opencsv.CSVWriter;
import com.widen.tabitha.Row;

import java.io.IOException;

/**
 * Writes rows of strings to a CSV file.
 */
public class CsvWriter implements Writer<String>
{
	private CSVWriter writer;
	private boolean headersWritten = false;

	public CsvWriter(java.io.Writer writer)
	{
		this.writer = new CSVWriter(writer);
	}

	@Override
	public void write(Row<String> row) throws IOException
	{
		if (!headersWritten)
		{
			headersWritten = true;
			writer.writeNext(row.columns().toArray());
		}
	}
}
