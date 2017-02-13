package com.widen.tabitha;

import java.util.Optional;

/**
 * Stores a single row of data of some type, indexed by column.
 */
public class Row<T>
{
    private final ColumnIndex columnIndex;
    private final T[] data;

    public Row(ColumnIndex columnIndex, T... data)
    {
        this.columnIndex = columnIndex;
        this.data = data;
    }

    /**
     * Get the column index.
     *
     * @return
     */
    public ColumnIndex columns()
    {
        return columnIndex;
    }

    /**
     * Get the value of a cell by column name.
     */
    public Optional<T> get(String name)
    {
        return columnIndex.columnIndex(name).flatMap(this::atIndex);
    }

    /**
     * Get the value of a cell by index.
     */
    public Optional<T> atIndex(int index)
    {
        if (index >= data.length)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(data[index]);
    }

    /**
     * Returns an array containing the values in the row in order.
     * <p>
     * Empty cell values are preserved as null entries in the array.
     */
    public T[] toArray()
    {
        return data.clone();
    }
}
