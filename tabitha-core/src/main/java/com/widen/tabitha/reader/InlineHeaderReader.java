package com.widen.tabitha.reader;

import java.io.IOException;
import java.util.Optional;

/**
 * Decorates another reader, interpreting the first row of each page of data as the header for subsequent rows.
 */
public class InlineHeaderReader implements RowReader {
    private final RowReader inner;
    private Header currentHeader;
    private long currentPage = -1;

    public static RowReader decorate(RowReader reader, ReaderOptions options) {
        if (options.isInlineHeaders()) {
            reader = new InlineHeaderReader(reader);
        }
        return reader;
    }

    private InlineHeaderReader(RowReader inner) {
        this.inner = inner;
    }

    @Override
    public Optional<Row> read() throws IOException {
        while (true) {
            Optional<Row> row = inner.read();

            if (!row.isPresent()) {
                return Optional.empty();
            }

            long page = row.get().pageIndex();

            if (page != currentPage) {
                currentHeader = null;
            }

            currentPage = page;

            if (currentHeader == null) {
                currentHeader = Header.fromRow(row.get());
                continue;
            }

            return row.map(r -> r.withHeader(currentHeader));
        }
    }
}
