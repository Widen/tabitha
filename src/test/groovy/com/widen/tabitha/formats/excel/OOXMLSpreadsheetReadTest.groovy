package com.widen.tabitha.formats.excel

import com.widen.tabitha.RowReader
import com.widen.tabitha.formats.BaseReadTest

class OOXMLSpreadsheetReadTest extends BaseReadTest {
    @Override
    protected String getTestFile() {
        return "Workbook1.xlsx"
    }

    @Override
    protected RowReader open(File file) {
        return OOXMLSpreadsheetRowReader.open(file)
    }

    @Override
    protected RowReader open(InputStream inputStream) {
        return OOXMLSpreadsheetRowReader.open(inputStream)
    }
}
