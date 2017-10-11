package com.widen.tabitha;

import java.io.IOException;
import java.util.Optional;

/**
 * Wraps another reader, interpreting the first row of each page of data as the header for subsequent rows.
 */
class InlineHeaderReader extends RowReaderDecorator {
    private Header header;

    InlineHeaderReader(RowReader inner) {
        super(inner);
    }

    @Override
    public Optional<Row> read() throws IOException {
        if (header == null) {
            header = super.read()
                .map(this::createHeaderFromRow)
                .orElse(null);

            if (header == null) {
                return Optional.empty();
            }
        }

        return super.read().map(row -> row.withHeader(header));
    }

    @Override
    public boolean nextPage() throws IOException {
        if (super.nextPage()) {
            header = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean seekPage(String name) throws IOException {
        if (super.seekPage(name)) {
            header = null;
            return true;
        }
        return false;
    }

    private Header createHeaderFromRow(Row row) {
        Header.Builder builder = Header.builder();

        for (Variant value : row) {
            builder.add(value.toString());
        }

        return builder.build();
    }
}
