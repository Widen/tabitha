package com.widen.tabitha.formats

import com.widen.tabitha.RowReader
import com.widen.tabitha.formats.excel.WorkbookRowReader
import com.widen.tabitha.formats.excel.XLSRowReader

class XLSReadTest extends BaseReadTest {
    @Override
    protected String getTestFile() {
        return "Workbook1.xls"
    }

    @Override
    protected RowReader open(File file) {
        return XLSRowReader.open(file)
    }

    @Override
    protected RowReader open(InputStream inputStream) {
        return XLSRowReader.open(inputStream)
    }
}
