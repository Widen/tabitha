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
     * Seek the reader to the first row of the given page.
     * <p>
     * The default implementation of this method traverses pages sequentially using {@link #nextPage()} until the
     * desired index is reached and so does not support rewinding. If your implementation supports seeking, you should
     * override this method with a more optimal implementation.
     *
     * @param index The page index to advance to.
     * @return True if successful, or false if the page was not found.
     */
    default boolean seekPage(int index)
    {
        while (index > getPageIndex()) {
            if (!nextPage()) {
                return false;
            }
        }

        return index == getPageIndex();
    }

    /**
     * Seek the reader to the first row of the given page.
     * <p>
     * The default implementation of this method traverses pages sequentially using {@link #nextPage()} until a page
     * with the desired name is reached and so does not support rewinding. If your implementation supports seeking, you
     * should override this method with a more optimal implementation.
     *
     * @param name The name of the page to advance to.
     * @return True if successful, or false if the page was not found.
     */
    default boolean seekPage(String name)
    {
        do {
            if (name.equals(getPageName().orElse(null))) {
                return true;
            }
        } while (nextPage());

        return false;
    }

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
