package com.widen.tabitha.plugins.json;

import com.widen.tabitha.plugins.ReaderPlugin;
import com.widen.tabitha.reader.InlineHeaderReader;
import com.widen.tabitha.reader.ReaderOptions;
import com.widen.tabitha.reader.RowReader;

import java.io.InputStream;
import java.util.stream.Stream;

public class JsonPlugin implements ReaderPlugin {
    @Override
    public boolean supportsFormat(String mimeType) {
        return Stream.of("application/x-ndjson").anyMatch(mimeType::equals);
    }

    @Override
    public RowReader createReader(InputStream inputStream, ReaderOptions options) {
        return InlineHeaderReader.decorate(new JsonRowReader(inputStream), options);
    }
}
