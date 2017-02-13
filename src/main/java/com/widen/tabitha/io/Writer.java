package com.widen.tabitha.io;

import com.widen.tabitha.Row;

import java.io.IOException;

/**
 * Writes data rows to an output.
 */
public interface Writer<T>
{
    /**
     * Writes a row to the output.
     *
     * @param row
     */
    void write(Row<T> row) throws IOException;

    /**
     * Writes multiple rows to the output.
     *
     * @param rows
     * @throws IOException
     */
    default void writeAll(Row<T>... rows) throws IOException
    {
        for (Row<T> row : rows)
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
