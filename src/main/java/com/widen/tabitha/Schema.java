package com.widen.tabitha;

import org.apache.commons.collections4.iterators.ArrayIterator;

import java.util.*;

/**
 * Defines a list of columns. Used to create rows that follow the schema.
 */
public class Schema implements Iterable<Column>
{
    // Ordered list of columns.
    private Column[] columnsByIndex;

    // Maps the column name to the column index. Used for fast lookup of column index by name.
    private Map<String, Integer> columnsByName;

    /**
     * Create a new column index that combines all the columns in the given indexes.
     *
     * Columns are ordered from left to right the order of the indexes given.
     *
     * @param schemas The schemas to merge.
     * @return The merged schema.
     * @throws DuplicateColumnException if duplicate column names are found.
     */
    public static Schema merge(Schema... schemas)
    {
        Builder builder = new Builder();

        for (Schema schema : schemas)
        {
            if (schema != null)
            {
                for (Column column : schema)
                {
                    builder.add(column.name);
                }
            }
        }

        return builder.build();
    }

    /**
     * Create a new column index with the given column names in order.
     *
     * @param columns The column names.
     */
    public Schema(String... columns)
    {
        columnsByIndex = new Column[columns.length];
        columnsByName = new HashMap<>();

        for (int i = 0; i < columns.length; ++i)
        {
            columnsByIndex[i] = new Column(columns[i]);
            columnsByName.put(columns[i], i);
        }
    }

    /**
     * Create a new row following the schema with the given values.
     *
     * @param values Values to put in the row, in column order.
     * @return A new row.
     */
    public Row createRow(Value... values)
    {
        int count = Math.min(values.length, size());
        Row.Cell[] cells = new Row.Cell[count];

        for (int i = 0; i < count; ++i)
        {
            cells[i] = new Row.Cell(columnsByIndex[i], values[i]);
        }

        return new Row(cells);
    }

    /**
     * Get the number of columns.
     *
     * @return The number of columns.
     */
    public int size()
    {
        return columnsByIndex.length;
    }

    /**
     * Get a column by index.
     *
     * @param index The column index.
     * @return The column if the index is valid.
     */
    public Optional<Column> getColumn(int index)
    {
        if (index < columnsByIndex.length)
        {
            return Optional.of(columnsByIndex[index]);
        }

        return Optional.empty();
    }

    /**
     * Get a column by name.
     *
     * @param name The column name.
     * @return The column if it exists.
     */
    public Optional<Column> getColumn(String name)
    {
        Integer index = columnsByName.get(name);

        if (index != null)
        {
            return Optional.of(columnsByIndex[index]);
        }

        return Optional.empty();
    }

    @Override
    public Iterator<Column> iterator()
    {
        return new ArrayIterator<>(columnsByIndex);
    }

    /**
     * Creates schemas incrementally.
     */
    public static class Builder
    {
        private List<String> columns;

        /**
         * Create a new empty schema builder.
         */
        public Builder()
        {
            columns = new ArrayList<>();
        }

        /**
         * Add a column to the schema.
         *
         * @param name The column name.
         * @return The builder.
         * @throws DuplicateColumnException if duplicate column names are found.
         */
        public Builder add(String name)
        {
            if (columns.contains(name))
            {
                throw new DuplicateColumnException(name);
            }

            columns.add(name);

            return this;
        }

        /**
         * Create a schema from the builder.
         *
         * @return The new schema.
         */
        public Schema build()
        {
            return new Schema((String[]) columns.toArray());
        }
    }

    /**
     * Exception thrown when duplicate column names are attempted to be added to a schema.
     */
    public static class DuplicateColumnException extends RuntimeException
    {
        public DuplicateColumnException(String column)
        {
            super("The column '" + column + "' already exists.");
        }
    }
}
