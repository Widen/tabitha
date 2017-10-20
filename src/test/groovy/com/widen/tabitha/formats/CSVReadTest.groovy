package com.widen.tabitha.formats

import com.widen.tabitha.RowReader
import com.widen.tabitha.formats.delimited.DelimitedFormat
import com.widen.tabitha.formats.delimited.DelimitedRowReader

class CSVReadTest extends BaseReadTest {
    @Override
    protected String getTestFile() {
        return "Workbook1.csv"
    }

    @Override
    protected RowReader open(File file) {
        return new DelimitedRowReader(new FileInputStream(file), DelimitedFormat.CSV)
    }

    @Override
    protected RowReader open(InputStream inputStream) {
        return new DelimitedRowReader(inputStream, DelimitedFormat.CSV)
    }

    @Override
    protected List<List<Object>> getExpectedData() {
        return [
            ["Column A", "Column B", "Column C"],
            ["foo", "Party", "Time"],
            ["bar", null, "World"],
            ["baz", "Excel", "10009"],
        ]
    }
}
