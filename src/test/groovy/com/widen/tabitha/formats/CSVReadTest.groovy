package com.widen.tabitha.formats

import com.widen.tabitha.RowReader

class CSVReadTest extends BaseReadTest {
    @Override
    protected String getTestFile() {
        return "Workbook1.csv"
    }

    @Override
    protected RowReader open(File file) {
        return new DelimitedRowReader(new FileReader(file), DelimitedTextFormat.CSV)
    }

    @Override
    protected RowReader open(InputStream inputStream) {
        return new DelimitedRowReader(inputStream, DelimitedTextFormat.CSV)
    }
}
