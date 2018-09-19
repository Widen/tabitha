package com.widen.tabitha.formats

import com.widen.tabitha.formats.excel.WorkbookRowWriter
import com.widen.tabitha.formats.excel.XLSRowReader
import com.widen.tabitha.reader.RowReader
import com.widen.tabitha.writer.RowWriter

import java.nio.file.Path

class XLSFormatTest extends BaseFormatTest {
    @Override
    RowReader createReader(Path path) {
        return XLSRowReader.open(path, null)
    }

    @Override
    RowReader createReader(InputStream inputStream) {
        return XLSRowReader.open(inputStream, null)
    }

    @Override
    RowWriter createWriter(Path path) {
        return WorkbookRowWriter.xls(path)
    }
}
