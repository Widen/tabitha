package com.widen.tabitha;

import org.apache.commons.collections4.iterators.ObjectArrayIterator;

import java.util.Iterator;
import java.util.Optional;

/**
 * Stores a single row of data values, indexed by column.
 */
public class Row implements Iterable<Value>
{
    private final ColumnIndex columnIndex;
    private final Value[] values;

    public Row(ColumnIndex columnIndex, Value... values)
    {
        this.columnIndex = columnIndex;
        this.values = values;
    }

    /**
     * Get the number of values in the row.
     *
     * @return The number of values in the row.
     */
    public int size()
    {
        return values.length;
    }

    /**
     * Get the column index.
     *
     * @return The column index.
     */
    public ColumnIndex columns()
    {
        return columnIndex;
    }

    /**
     * Get the value of a cell by column name.
     *
     * @param name The name of the column.
     * @return The value for the given column, if present.
     */
    public Optional<Value> get(String name)
    {
        return columnIndex.columnIndex(name).flatMap(this::get);
    }

    /**
     * Get the value of a cell by index.
     *
     * @param index The index of the column.
     * @return The value for the given column, if present.
     */
    public Optional<Value> get(int index)
    {
        if (index >= values.length)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(values[index]);
    }

    /**
     * Get an iterator over the values in the row.
     *
     * @return Iterator over the values in the row.
     */
    public Iterator<Value> iterator()
    {
        return new ObjectArrayIterator<>(values);
    }

    /**
     * Returns an array containing the values in the row in order.
     *
     * Empty cell values are preserved as null entries in the array.
     *
     * @return Array of values.
     */
    public Value[] toArray()
    {
        return values.clone();
    }
}
