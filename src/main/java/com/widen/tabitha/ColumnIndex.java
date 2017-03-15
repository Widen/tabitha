package com.widen.tabitha;

import java.util.*;

/**
 * Stores column names in order, with O(1) lookup times by index or by name.
 */
public class ColumnIndex implements Iterable<String>
{
    // Ordered list of column names.
    private List<String> columnNames;

    // Maps the hashcode of a column name to the column index. Used for fast lookup of column index by name.
    private Map<Integer, Integer> columnMap;

    /**
     * Get the number of columns.
     *
     * @return The number of columns.
     */
    public int size()
    {
        return columnNames.size();
    }

    /**
     * Get the name of a column by index.
     *
     * @param index The column index.
     * @return The name of the column if the index is valid.
     */
    public Optional<String> columnName(int index)
    {
        if (index < columnNames.size())
        {
            return Optional.of(columnNames.get(index));
        }

        return Optional.empty();
    }

    /**
     * Get the index of a column by name.
     *
     * @param name The column name.
     * @return The index of the column if the name exists.
     */
    public Optional<Integer> columnIndex(String name)
    {
        return Optional.ofNullable(columnMap.get(name.hashCode()));
    }

    /**
     * Get an iterator over all column names in order.
     *
     * @return Iterator over column names.
     */
    public Iterator<String> iterator()
    {
        return columnNames.iterator();
    }

    /**
     * Returns an array containing all of the column names in order.
     *
     * @return Array of column names.
     */
    public String[] toArray()
    {
        return (String[]) columnNames.toArray();
    }

    /**
     * Creates column indexes.
     */
    public static class Builder
    {
        private List<String> columnNames;
        private Map<Integer, Integer> columnMap;

        public Builder()
        {
            columnNames = new ArrayList<>();
            columnMap = new HashMap<>();
        }

        public Builder addColumn(String name)
        {
            int hashCode = name.hashCode();

            if (columnMap.containsKey(hashCode))
            {
                throw new RuntimeException("Column already exists");
            }

            columnMap.put(hashCode, columnNames.size());
            columnNames.add(name);

            return this;
        }

        public ColumnIndex build()
        {
            ColumnIndex columnIndex = new ColumnIndex();
            columnIndex.columnNames = Collections.unmodifiableList(columnNames);
            columnIndex.columnMap = Collections.unmodifiableMap(columnMap);

            return columnIndex;
        }
    }
}
