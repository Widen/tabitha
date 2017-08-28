package com.widen.tabitha.formats

import com.widen.tabitha.RowReader
import com.widen.tabitha.formats.excel.XLSXRowReader

class XLSXReadTest extends BaseReadTest {
    @Override
    protected String getTestFile() {
        return "Workbook1.xlsx"
    }

    @Override
    protected RowReader open(File file) {
        return XLSXRowReader.open(file)
    }

    @Override
    protected RowReader open(InputStream inputStream) {
        return XLSXRowReader.open(inputStream)
    }
}
