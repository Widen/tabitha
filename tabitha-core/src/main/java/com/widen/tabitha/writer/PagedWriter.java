package com.widen.tabitha.writer;

import com.widen.tabitha.Variant;

import java.io.IOException;
import java.util.List;

/**
 * Extension of the {@link RowWriter} interface for writers that support multiple pages of data.
 * <p>
 * Pages are ordered sequentially and rows are always written to the "active" page.
 */
public interface PagedWriter extends RowWriter {
    /**
     * Begin a new page of data. Subsequent rows written will begin in the newly active page.
     * <p>
     * This method will be called automatically if you begin writing rows without creating a page first.
     */
    void beginPage();

    /**
     * Begin a new page of data with a specific page name.
     *
     * @param name The name of the page.
     */
    void beginPage(String name);

    /**
     * Create a writer that divides up rows written into multiple pages of a given size.
     * <p>
     * This is especially useful when creating very large spreadsheet documents, where splitting up the data into
     * multiple pages makes it less difficult to open the the file in a visual editor.
     *
     * @param size The maximum number of rows per page.
     * @return A new writer.
     */
    default RowWriter partitioned(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Partition size must be at least 1");
        }

        return new RowWriter() {
            private int currentSize = 0;

            @Override
            public void write(List<Variant> cells) throws IOException {
                if (currentSize >= size) {
                    PagedWriter.this.beginPage();
                    currentSize = 0;
                }

                PagedWriter.this.write(cells);
                ++currentSize;
            }

            @Override
            public void close() throws IOException {
                PagedWriter.this.close();
            }
        };
    }
}
