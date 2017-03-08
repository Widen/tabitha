package com.widen.tabitha;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

/**
 * Reads data rows from a data source.
 */
@FunctionalInterface
public interface RowReader extends Iterable<Row>
{
    /**
     * Attempt to read the next row.
     */
    Optional<Row> read() throws IOException;

    /**
     * Get an iterator over all rows produced by the reader.
     */
    default Iterator<Row> iterator()
    {
        return new Iterator<Row>() {
            private Row nextRow;

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

            public Row next()
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

                Row row = nextRow;
                nextRow = null;
                return row;
            }
        };
    }

    /**
      * Pipe all remaining rows into a writer.
      */
    default void pipe(RowWriter rowWriter) throws IOException
    {
        for (Row row : this)
        {
            rowWriter.write(row);
        }
    }
}
