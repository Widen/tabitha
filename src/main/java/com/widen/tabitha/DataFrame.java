package com.widen.tabitha;

import com.widen.tabitha.collections.RingBuffer;

import java.util.Optional;

/**
 * An in-memory ordered series of data rows.
 */
public class DataFrame
{
    private final RingBuffer<Row> rows;

    public DataFrame()
    {
        rows = new RingBuffer<>();
    }

    public DataFrame(Row... rows)
    {
        this.rows = new RingBuffer<>(rows);
    }

    /**
     * Get the number of rows in the data frame.
     */
    public int size()
    {
        return rows.size();
    }

    /**
     * Get a row by index.
     */
    public Optional<Row> get(int index)
    {
        return rows.get(index);
    }

    /**
     * Add a row to the beginning of the frame.
     */
    public void pushFront(Row row)
    {
        rows.pushFront(row);
    }

    /**
     * Add a row to the end of the frame.
     */
    public void pushBack(Row row)
    {
        rows.pushBack(row);
    }

    /**
     * Remove a row from the beginning of the frame.
     */
    public Optional<Row> popFront()
    {
        return rows.popFront();
    }

    /**
     * Remove a row from the end of the frame.
     */
    public Optional<Row> popBack()
    {
        return rows.popBack();
    }

    /**
     * Create a row reader for this data frame. Note that the new reader will drain this frame of values until it is
     * empty while reading.
     */
    public RowReader reader()
    {
        // This creates a row reader using a method reference as the implementation for read().
        return this::popFront;
    }

    /**
     * Create a row writer for this data frame. Rows written will be appended to the end of the frame.
     */
    public RowWriter writer()
    {
        // This creates a row writer using a method reference as the implementation for write().
        return this::pushBack;
    }
}
