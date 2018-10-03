package com.widen.tabitha.plugins.delimited;

import com.widen.tabitha.plugins.ReaderPlugin;
import com.widen.tabitha.plugins.WriterPlugin;
import com.widen.tabitha.reader.InlineHeaderReader;
import com.widen.tabitha.reader.ReaderOptions;
import com.widen.tabitha.reader.RowReader;
import com.widen.tabitha.writer.RowWriter;

import java.io.InputStream;
import java.io.OutputStream;

public class TSVPlugin implements ReaderPlugin, WriterPlugin {
    @Override
    public boolean supportsFormat(String mimeType) {
        return "text/tab-separated-values".equals(mimeType);
    }

    @Override
    public RowReader createReader(InputStream inputStream, ReaderOptions options) {
        return InlineHeaderReader
            .decorate(new DelimitedRowReader(inputStream, DelimitedFormat.TSV), options);
    }

    @Override
    public RowWriter createWriter(OutputStream outputStream) {
        return new DelimitedRowWriter(outputStream, DelimitedFormat.TSV);
    }
}
