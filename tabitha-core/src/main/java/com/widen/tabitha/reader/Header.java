package com.widen.tabitha.reader;

import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Defines an ordered list of named columns.
 */
@EqualsAndHashCode
public class Header implements Iterable<String> {
    // Ordered list of columns.
    private final List<String> columnsByIndex;

    // Maps the column name to the column index. Used for fast lookup of column index by name.
    private final Map<String, Integer> columnsByName;

    /**
     * Create a header from a row, treating each cell value as a column name.
     *
     * @param row The row to create from.
     * @return The new header.
     */
    public static Header fromRow(Row row) {
        return new Header(row.cells().stream()
            .map(cell -> cell.isNone() ? null : cell.toString()));
    }

    /**
     * Create a new header builder.
     *
     * @return A header builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new column index that combines all the columns in the given indexes.
     * <p>
     * Columns are ordered from left to right the order of the indexes given.
     *
     * @param headers The headers to merge.
     * @return The merged header.
     * @throws DuplicateColumnException Thrown if duplicate column names are found.
     */
    public static Header merge(Header... headers) {
        return new Header(Arrays.stream(headers)
            .flatMap(header -> header.columnsByIndex.stream()));
    }

    /**
     * Create a new column index with the given column names in order.
     * <p>
     * Null columns will be unnamed, but have their positions maintained.
     *
     * @param columns The column names.
     */
    public Header(String... columns) {
        this(Arrays.stream(columns));
    }

    /**
     * Create a new column index with the given column names in order.
     * <p>
     * Null columns will be unnamed, but have their positions maintained.
     *
     * @param columns The column names.
     */
    public Header(Iterable<String> columns) {
        this(StreamSupport.stream(columns.spliterator(), false));
    }

    /**
     * Create a new column index with the given column names in order.
     * <p>
     * Null columns will be unnamed, but have their positions maintained.
     *
     * @param columns The column names.
     */
    public Header(Stream<String> columns) {
        columnsByIndex = new ArrayList<>();
        columnsByName = new HashMap<>();

        columns.forEachOrdered(column -> {
            columnsByIndex.add(column);

            if (column != null) {
                column = column.intern();
                columnsByName.put(column, columnsByIndex.size() - 1);
            }
        });
    }

    /**
     * Get the number of columns.
     *
     * @return The number of columns.
     */
    public int size() {
        return columnsByIndex.size();
    }

    /**
     * Get a column name by index.
     *
     * @param index The column index.
     * @return The column if the index is valid.
     */
    public Optional<String> nameOf(int index) {
        if (index >= 0 && index < columnsByIndex.size()) {
            return Optional.ofNullable(columnsByIndex.get(index));
        }

        return Optional.empty();
    }

    /**
     * Get the position of a column by name.
     *
     * @param name The column name.
     * @return The column position if it exists.
     */
    public Optional<Integer> indexOf(String name) {
        if (name == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(columnsByName.get(name));
    }

    /**
     * Get an array containing all of the column names in order.
     *
     * @return The new array.
     */
    public String[] toArray() {
        return columnsByIndex.toArray(new String[columnsByIndex.size()]);
    }

    @Override
    public Iterator<String> iterator() {
        Iterator<String> delegate = columnsByIndex.iterator();
        return new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public String next() {
                return delegate.next();
            }
        };
    }

    /**
     * Creates headers incrementally.
     */
    public static class Builder {
        private List<String> columns = new ArrayList<>();

        /**
         * Add a column to the header.
         *
         * @param name The column name.
         * @return This builder.
         * @throws DuplicateColumnException if duplicate column names are found.
         */
        public Builder add(String name) {
            if (columns.contains(name)) {
                throw new DuplicateColumnException(name);
            }

            columns.add(name);

            return this;
        }

        /**
         * Create a header from the builder.
         *
         * @return The new header.
         */
        public Header build() {
            return new Header(columns);
        }
    }

    /**
     * Exception thrown when duplicate column names are attempted to be added to a header.
     */
    public static class DuplicateColumnException extends RuntimeException {
        public DuplicateColumnException(String column) {
            super("The column '" + column + "' already exists.");
        }
    }

    @Override
    public String toString() {
        return columnsByIndex.toString();
    }
}
