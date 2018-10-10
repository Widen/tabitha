package com.widen.tabitha.reader;

import com.widen.tabitha.Variant;

import java.io.IOException;
import java.util.Optional;

/**
 * Decorates another reader, emitting blank rows whenever a row index is skipped by the inner reader.
 */
public class BlankRowReader implements RowReader {
    private final RowReader inner;
    private long pageIndex = -1;
    private long index = 0;
    private Row nextRow;

    BlankRowReader(RowReader inner) {
        this.inner = inner;
    }

    @Override
    public Optional<Row> read() throws IOException {
        // Get the next row if we don't have one.
        if (nextRow == null) {
            nextRow = inner.read().orElse(null);
        }

        // End of stream.
        if (nextRow == null) {
            return Optional.empty();
        }

        // Reset index on page change.
        if (nextRow.pageIndex() != pageIndex) {
            index = 0;
        }

        pageIndex = nextRow.pageIndex();

        // If this is the next sequential row index, return it.
        if (nextRow.index() == index) {
            index++;
            Row row = nextRow;
            nextRow = null;
            return Optional.of(row);
        }

        // Return a blank "padding" row.
        return Optional.of(new Row(pageIndex, index++, new Variant[0]));
    }
}
