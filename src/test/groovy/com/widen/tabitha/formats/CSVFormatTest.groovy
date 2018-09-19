package com.widen.tabitha.formats

import com.widen.tabitha.formats.delimited.DelimitedFormat
import com.widen.tabitha.formats.delimited.DelimitedRowReader
import com.widen.tabitha.formats.delimited.DelimitedRowWriter
import com.widen.tabitha.reader.RowReader
import com.widen.tabitha.writer.RowWriter

import java.nio.file.Path

class CSVFormatTest extends BaseFormatTest {
    @Override
    RowReader createReader(Path path) {
        return createReader(path.newInputStream())
    }

    @Override
    RowReader createReader(InputStream inputStream) {
        return new DelimitedRowReader(inputStream, DelimitedFormat.CSV)
    }

    @Override
    RowWriter createWriter(Path path) {
        return new DelimitedRowWriter(path.newOutputStream(), DelimitedFormat.CSV)
    }
}
