package com.widen.tabitha;

import java.io.Closeable;
import java.io.IOException;

/**
 * Writes data rows to an output.
 */
@FunctionalInterface
public interface RowWriter extends Closeable
{
    /**
     * Writes a row to the output.
     *
     * @param row A row to write.
     * @throws IOException Thrown if an I/O error occurs.
     */
    void write(Row row) throws IOException;

    /**
     * Writes multiple rows to the output.
     *
     * @param rows The rows to write.
     * @throws IOException Thrown if an I/O error occurs.
     */
    default void writeAll(Row... rows) throws IOException
    {
        for (Row row : rows)
        {
            write(row);
        }
    }

    // Provide a default close method that does nothing.
    @Override
    default void close() throws IOException
    {
    }
}
