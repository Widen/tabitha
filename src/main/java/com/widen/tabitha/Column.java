package com.widen.tabitha;

/**
 * A column in a schema.
 */
public final class Column
{
    /**
     * The name of the column.
     */
    public final String name;

    /**
     * Create a new column.
     *
     * @param name The column name.
     */
    Column(String name)
    {
        if (name == null)
        {
            throw new NullPointerException();
        }

        this.name = name.intern();
    }

    @Override
    public String toString()
    {
        return name;
    }
}
