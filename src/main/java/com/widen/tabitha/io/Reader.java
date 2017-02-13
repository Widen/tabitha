package com.widen.tabitha.io;

import com.widen.tabitha.Row;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

public interface Reader<T> extends Iterable<Row<T>>
{
    /**
     * Attempt to read the next row.
     */
    Optional<Row<T>> read() throws IOException;

    /**
     * Get an iterator over all rows produced by the reader.
     */
    default Iterator<Row<T>> iterator()
    {
        return new Iterator<Row<T>>() {
            private Row<T> nextRow;

            public boolean hasNext()
            {
                if (nextRow == null)
                {
                    try
                    {
                        nextRow = read().orElse(null);
                    }
                    catch (IOException e)
                    {
                        return false;
                    }
                }

                return nextRow != null;
            }

            public Row<T> next()
            {
                if (nextRow == null)
                {
                    try
                    {
                        nextRow = read().orElse(null);
                    }
                    catch (IOException e)
                    {
                        return null;
                    }
                }

                Row<T> row = nextRow;
                nextRow = null;
                return row;
            }
        };
    }

    /**
      * Pipe all remaining rows into a writer.
      */
    default void pipe(Writer<T> writer) throws IOException
    {
        for (Row<T> row : this)
        {
            writer.write(row);
        }
    }
}
