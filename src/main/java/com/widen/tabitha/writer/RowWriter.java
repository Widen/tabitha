package com.widen.tabitha.writer;

import com.widen.tabitha.Variant;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Writes data rows to an output.
 */
public interface RowWriter extends Closeable {
    /**
     * A row writer that discards all rows written to it.
     * <p>
     * Closing has no effect on this row writer and is always re-usable.
     */
    RowWriter VOID = row -> {
    };

    /**
     * Writes a row to the output.
     *
     * @param cells A list of cell values to be written.
     * @throws IOException Thrown if an I/O error occurs.
     */
    void write(List<Variant> cells) throws IOException;

    /**
     * Writes a row to the output.
     *
     * @param cells An array of cell values to be written.
     * @throws IOException Thrown if an I/O error occurs.
     */
    default void write(Variant... cells) throws IOException {
        write(Arrays.asList(cells));
    }

    // Provide a default close method that does nothing.
    @Override
    default void close() throws IOException {
    }
}
