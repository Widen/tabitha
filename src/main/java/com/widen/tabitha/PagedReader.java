package com.widen.tabitha;

import java.io.IOException;
import java.util.Optional;

/**
 * Extension of the {@link RowReader} interface for readers that split data over multiple pages.
 * <p>
 * Pages are ordered sequentially and rows are always read from the "active" page.
 */
public interface PagedReader extends Paged, RowReader {
    /**
     * Advance the reader to the next page of data.
     *
     * @return True if successful, or false if there is not another page to advance to.
     */
    boolean nextPage();

    /**
     * Create a new reader over the rows of all pages.
     *
     * @return The new reader.
     */
    default RowReader allPages() {
        return new RowReader() {
            @Override
            public Optional<Row> read() throws IOException {
                Optional<Row> nextRow = PagedReader.this.read();

                while (!nextRow.isPresent()) {
                    if (PagedReader.this.nextPage()) {
                        nextRow = PagedReader.this.read();
                    } else {
                        break;
                    }
                }

                return nextRow;
            }

            @Override
            public void close() throws IOException {
                PagedReader.this.close();
            }
        };
    }
}
