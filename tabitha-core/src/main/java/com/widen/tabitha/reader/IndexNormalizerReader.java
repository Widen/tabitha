package com.widen.tabitha.reader;

import java.io.IOException;
import java.util.Optional;

/**
 * Decorates another reader and normalizes page and row indexes to always be sequential.
 */
public class IndexNormalizerReader implements RowReader {
    private final RowReader inner;
    private long lastPage = -1;
    private long pageIndex = -1;
    private long index = 0;

    IndexNormalizerReader(RowReader inner) {
        this.inner = inner;
    }

    @Override
    public Optional<Row> read() throws IOException {
        return inner.read().map(row -> {
            if (row.pageIndex() != lastPage) {
                lastPage = row.pageIndex();
                pageIndex++;
                index = 0;
            }
            else {
                index++;
            }

            return row.withPageIndex(pageIndex).withIndex(index);
        });
    }
}
