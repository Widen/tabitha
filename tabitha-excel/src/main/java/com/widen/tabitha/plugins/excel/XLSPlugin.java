package com.widen.tabitha.plugins.excel;

import com.widen.tabitha.plugins.ReaderPlugin;
import com.widen.tabitha.plugins.WriterPlugin;
import com.widen.tabitha.reader.InlineHeaderReader;
import com.widen.tabitha.reader.ReaderOptions;
import com.widen.tabitha.reader.RowReader;
import com.widen.tabitha.writer.RowWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class XLSPlugin implements ReaderPlugin, WriterPlugin {
    @Override
    public boolean supportsFormat(String mimeType) {
        return "application/vnd.ms-excel".equals(mimeType);
    }

    @Override
    public RowReader createReader(Path path, ReaderOptions options) throws IOException {
        return InlineHeaderReader
            .decorate(XLSRowReader.open(path, options), options);
    }

    @Override
    public RowReader createReader(InputStream inputStream, ReaderOptions options) throws IOException {
        return InlineHeaderReader
            .decorate(XLSRowReader.open(inputStream, options), options);
    }

    @Override
    public RowWriter createWriter(OutputStream outputStream) {
        return WorkbookRowWriter.xls(outputStream);
    }
}
