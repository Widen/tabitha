package com.widen.tabitha.formats

import com.widen.tabitha.formats.excel.WorkbookRowWriter
import com.widen.tabitha.formats.excel.XLSXRowReader
import com.widen.tabitha.reader.RowReader
import com.widen.tabitha.writer.RowWriter

import java.nio.file.Path

class XLSXFormatTest extends BaseFormatTest {
    @Override
    RowReader createReader(Path path) {
        return XLSXRowReader.open(path.toFile(), null)
    }

    @Override
    RowReader createReader(InputStream inputStream) {
        return XLSXRowReader.open(inputStream, null)
    }

    @Override
    RowWriter createWriter(Path path) {
        return WorkbookRowWriter.xlsx(path.toFile())
    }
}
