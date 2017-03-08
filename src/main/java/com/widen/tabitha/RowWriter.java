package com.widen.tabitha;

import java.io.IOException;

/**
 * Writes data rows to an output.
 */
@FunctionalInterface
public interface RowWriter
{
    /**
     * Writes a row to the output.
     *
     * @param row
     */
    void write(Row row) throws IOException;

    /**
     * Writes multiple rows to the output.
     *
     * @param rows
     * @throws IOException
     */
    default void writeAll(Row... rows) throws IOException
    {
        for (Row row : rows)
        {
            write(row);
        }
    }

    /**
     * Saves and closes the writer, finalizing its output.
     *
     * @throws IOException
     */
    default void close() throws IOException
    {
    }
}
