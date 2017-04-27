package com.widen.tabitha;

import com.widen.tabitha.collections.RingBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

/**
 * A mutable, ordered series of data rows.
 * <p>
 * Data frames can also be used as a lazy buffer for row readers. A data frame created from a row reader is called a
 * <i>streaming</i> data frame. Rows are read from the reader lazily and only when needed to fulfil the result of a
 * query.
 * <p>
 * This class has no methods for getting a list of the columns stored in the frame. This is because columns are stored
 * per-cell instead of for a whole data set. While this offers greater flexibility, as a result, each row in a frame can
 * contain different columns than each other and there is no way for a data frame to know all the possible columns it
 * may contain.
 */
public class DataFrame implements Iterable<Row>, Closeable
{
    private final RingBuffer<Row> rows;
    private RowReader rowReader;

    /**
     * Create a new streaming data frame from a row reader.
     *
     * @param rowReader The row reader used to stream in data.
     * @return A new data frame.
     */
    public static DataFrame streaming(RowReader rowReader)
    {
        DataFrame dataFrame = new DataFrame();
        dataFrame.rowReader = rowReader;

        return dataFrame;
    }

    /**
     * Create a new in-memory data frame.
     */
    public DataFrame()
    {
        rows = new RingBuffer<>();
        rowReader = null;
    }

    /**
     * Create a new in-memory data frame pre-filled with the given rows.
     *
     * @param rows The rows to put in the data frame.
     */
    public DataFrame(Row... rows)
    {
        this.rows = new RingBuffer<>(rows);
        rowReader = null;
    }

    /**
     * Checks if this data frame is a streaming data frame.
     *
     * @return True if this is a streaming data frame.
     */
    public boolean isStreaming()
    {
        return rowReader != null;
    }

    /**
     * Checks if the data frame is currently empty.
     *
     * @return True if the data frame contains no elements.
     */
    public boolean isEmpty()
    {
        return rows.isEmpty();
    }

    /**
     * Get the number of rows in the data frame.
     *
     * @return The data frame size.
     */
    public int size()
    {
        return rows.size();
    }

    /**
     * Get a row by index.
     * <p>
     * If this is a streaming data frame, rows will be read if possible until the given index is reachable.
     *
     * @param index The index of the row to get.
     * @return The row, or none if the index does not exist.
     */
    public Optional<Row> get(int index)
    {
        if (isStreaming())
        {
            while (index >= size())
            {
                try
                {
                    Row row = rowReader.read().orElse(null);

                    if (row == null)
                    {
                        break;
                    }

                    pushBack(row);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        return rows.get(index);
    }

    /**
     * Add a row to the beginning of the frame.
     *
     * @param row The row to push
     */
    public void pushFront(Row row)
    {
        rows.pushFront(row);
    }

    /**
     * Add a row to the end of the frame.
     *
     * @param row The row to push
     */
    public void pushBack(Row row)
    {
        rows.pushBack(row);
    }

    /**
     * Remove a row from the beginning of the frame.
     * <p>
     * If this is a streaming data frame and the data frame is empty, a row will be read and returned if possible.
     *
     * @return The removed row, or none if the data frame is empty.
     */
    public Optional<Row> popFront()
    {
        if (isEmpty() && isStreaming())
        {
            try
            {
                return rowReader.read();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        return rows.popFront();
    }

    /**
     * Remove a row from the end of the frame.
     * <p>
     * If this is a streaming data frame and the data frame is empty, a row will be read and returned if possible.
     *
     * @return The removed row, or none if the data frame is empty.
     */
    public Optional<Row> popBack()
    {
        if (isEmpty() && isStreaming())
        {
            try
            {
                return rowReader.read();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        return rows.popBack();
    }

    /**
     * Create a row reader for this data frame.
     *
     * @return The row reader.
     */
    public RowReader reader()
    {
        return RowReader.from(iterator());
    }

    /**
     * Create a row writer for this data frame. Rows written will be appended to the end of the frame.
     *
     * @return The row writer.
     */
    public RowWriter writer()
    {
        // This creates a row writer using a method reference as the implementation for write().
        return this::pushBack;
    }

    @Override
    public Iterator<Row> iterator()
    {
        return rows.iterator();
    }

    /**
     * Close the data frame.
     * <p>
     * If this is a streaming data frame, the underlying reader will be closed and no more rows will be read. This
     * method has no effect on non-streaming data frames.
     *
     * @throws IOException Thrown if an I/O error occurs.
     */
    @Override
    public void close() throws IOException
    {
        if (rowReader != null)
        {
            rowReader.close();
            rowReader = null;
        }
    }
}
