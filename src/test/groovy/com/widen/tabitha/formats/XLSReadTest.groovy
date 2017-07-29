package com.widen.tabitha.formats

import com.widen.tabitha.RowReader

class XLSReadTest extends BaseReadTest {
    @Override
    protected String getTestFile() {
        return "Workbook1.xls"
    }

    @Override
    protected RowReader open(File file) {
        return new ExcelRowReader(file)
    }

    @Override
    protected RowReader open(InputStream inputStream) {
        return new ExcelRowReader(inputStream)
    }
}
