package com.widen.tabitha.plugins.delimited;

import com.widen.tabitha.plugins.ReaderPlugin;
import com.widen.tabitha.plugins.WriterPlugin;
import com.widen.tabitha.reader.InlineHeaderReader;
import com.widen.tabitha.reader.ReaderOptions;
import com.widen.tabitha.reader.RowReader;
import com.widen.tabitha.writer.RowWriter;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

public class CSVPlugin implements ReaderPlugin, WriterPlugin {
    @Override
    public boolean supportsFormat(String mimeType) {
        return Stream.of("text/csv", "text/plain").anyMatch(mimeType::equals);
    }

    @Override
    public RowReader createReader(InputStream inputStream, ReaderOptions options) {
        return InlineHeaderReader
            .decorate(new DelimitedRowReader(inputStream, DelimitedFormat.CSV), options);
    }

    @Override
    public RowWriter createWriter(OutputStream outputStream) {
        return new DelimitedRowWriter(outputStream, DelimitedFormat.CSV);
    }
}
