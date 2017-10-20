package com.widen.tabitha;

import java.io.IOException;
import java.util.Optional;

abstract class RowReaderDecorator implements RowReader {
    private final RowReader inner;

    RowReaderDecorator(RowReader inner) {
        this.inner = inner;
    }

    @Override
    public Optional<Row> read() throws IOException {
        return inner.read();
    }

    @Override
    public Optional<String> getPageName() {
        return inner.getPageName();
    }

    @Override
    public boolean nextPage() throws IOException {
        return inner.nextPage();
    }

    @Override
    public boolean seekPage(String name) throws IOException {
        return inner.seekPage(name);
    }

    @Override
    public void close() throws IOException {
        inner.close();
    }
}
