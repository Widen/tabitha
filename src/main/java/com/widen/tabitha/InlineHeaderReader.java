package com.widen.tabitha;

import java.io.IOException;
import java.util.Optional;

/**
 * Wraps another reader, interpreting the first row of each page of data as the header for subsequent rows.
 */
class InlineHeaderReader implements RowReader {
    private final RowReader inner;
    private Header header;

    InlineHeaderReader(RowReader inner) {
        this.inner = inner;
    }

    @Override
    public Optional<Row> read() throws IOException {
        if (header == null) {
            header = inner.read()
                .map(this::createHeaderFromRow)
                .orElse(null);

            if (header == null) {
                return Optional.empty();
            }
        }

        return inner.read().map(row -> row.withHeader(header));
    }

    private Header createHeaderFromRow(Row row) {
        Header.Builder builder = Header.builder();

        for (Variant value : row) {
            builder.add(value.toString());
        }

        return builder.build();
    }
}
