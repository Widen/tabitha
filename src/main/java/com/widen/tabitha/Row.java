package com.widen.tabitha;

import org.apache.commons.collections4.iterators.ArrayIterator;

import java.util.*;
import java.util.function.Function;

/**
 * Stores a single row of data values, ordered by column position.
 * <p>
 * Row objects are always immutable. To modify a row, several methods are provided that instead return a copy of the row
 * with the requested changes incorporated. This offers several benefits including performance and thread safety, since
 * a deep copy of row data is never required and we can reuse object references everywhere without fear of concurrent
 * modification.
 */
public class Row implements Iterable<Variant> {
    // The header this row is mapped to. May be null.
    private final Header header;

    // Cell values in column order.
    private final Variant[] cells;

    /**
     * Create a new row that contains the values of all the given rows.
     *
     * @param rows The rows to merge.
     * @return The merged row.
     */
    public static Row merge(Row... rows) {
        List<Variant> cells = new ArrayList<>();

        for (int i = 0; i < rows.length; ++i) {
            cells.addAll(Arrays.asList(rows[i].cells));
        }

        return new Row(null, cells.toArray(new Variant[cells.size()]));
    }

    /**
     * Create a new row containing the given cell values.
     *
     * @param values The cell values to contain.
     * @return The new row.
     */
    public static Row create(Variant... values) {
        if (values == null) {
            return new Row(null, new Variant[0]);
        }

        return create(Arrays.asList(values));
    }

    /**
     * Create a row containing the given cell values.
     *
     * @param values The cell values to contain.
     * @return The new row.
     */
    public static Row create(Collection<Variant> values) {
        if (values == null) {
            return new Row(null, new Variant[0]);
        }

        Variant[] cells = new Variant[values.size()];
        int index = 0;

        for (Variant value : values) {
            if (value == null) {
                cells[index] = Variant.NONE;
            } else {
                cells[index] = value;
            }

            index += 1;
        }

        return new Row(null, cells);
    }

    /**
     * Create a row containing the given cell values. This does not do any null checks.
     *
     * @param header The column header to use.
     * @param cells The cells to contain.
     */
    private Row(Header header, Variant[] cells) {
        this.header = header;
        this.cells = cells;
    }

    /**
     * Get the number of values in the row.
     *
     * @return The number of values in the row.
     */
    public int size() {
        return cells.length;
    }

    /**
     * Get the header used by this row, if any.
     *
     * @return The header.
     */
    public Optional<Header> header() {
        return Optional.ofNullable(header);
    }

    /**
     * Get the value of a cell by index.
     *
     * @param index The index of the column.
     * @return The value for the given column, if the index exists.
     */
    public Optional<Variant> get(int index) {
        if (index >= cells.length) {
            return Optional.empty();
        }

        return Optional.ofNullable(cells[index]);
    }

    /**
     * Get the value of a cell by column name.
     *
     * @param name The name of the column.
     * @return The value for the given column, if present.
     */
    public Optional<Variant> get(String name) {
        return header()
            .flatMap(header -> header.indexOf(name))
            .flatMap(this::get);
    }

    /**
     * Create a new row, applying a function to every cell value.
     *
     * @param mapper A function to apply to each cell value.
     * @return The new row.
     */
    public Row map(Function<Variant, Variant> mapper) {
        Variant[] mapped = new Variant[cells.length];

        for (int i = 0; i < mapped.length; ++i) {
            mapped[i] = mapper.apply(cells[i]);

            if (mapped[i] == null) {
                mapped[i] = Variant.NONE;
            }
        }

        return new Row(header, mapped);
    }

    /**
     * Get a new row that contains the values for only columns in the given range.
     *
     * @param start The start index, inclusive.
     * @param end The ending index, exclusive.
     * @return The new row.
     */
    public Row range(int start, int end) {
        if (start < 0 || end > cells.length) {
            throw new IndexOutOfBoundsException();
        }

        if (start > end) {
            throw new IllegalArgumentException("Starting index cannot be greater than ending index.");
        }

        return new Row(header, Arrays.copyOfRange(cells, start, end));
    }

    /**
     * Get a new row that contains the values for the given columns.
     * <p>
     * If there is no cell mapping for any of the given column names, that value will instead be filled in with
     * {@link Variant#NONE}.
     *
     * @param columns The names of columns to select.
     * @return The new row.
     */
    public Row select(String... columns) {
        Variant[] cells = new Variant[columns.length];

        for (int i = 0; i < columns.length; ++i) {
            cells[i] = get(columns[i]).orElse(Variant.NONE);
        }

        return new Row(header, cells);
    }

    /**
     * Create a copy of this row with a new header, preserving the original values.
     * <p>
     * The ordering and number of the existing values will be preserved, but because the new header may have different
     * column names or positions, values might be assigned to different column names.
     *
     * @param header The new header.
     * @return The new row.
     */
    public Row withHeader(Header header) {
        // Since we do not modify any values, copying the cells array is not necessary.
        return new Row(header, cells);
    }

    /**
     * Create a new row modifier that can be used to create a modified copy of this row.
     *
     * @return A row modifier.
     */
    public Modifier copy() {
        return new Modifier(header, cells);
    }

    /**
     * Get an array containing all of the values in this row.
     *
     * @return The new array.
     */
    public Variant[] toArray() {
        return Arrays.copyOf(cells, cells.length);
    }

    @Override
    public Iterator<Variant> iterator() {
        return new ArrayIterator<>(cells);
    }

    /**
     * Creates modified copies of another row.
     */
    public static class Modifier {
        private final Header header;
        private final Variant[] cells;

        /**
         * Set the value of a column.
         *
         * @param columnName The name of the column to set.
         * @param value The column value.
         * @return This modifier.
         */
        public Modifier set(String columnName, Variant value) {
            return set(header.indexOf(columnName).orElseThrow(IllegalArgumentException::new), value);
        }

        /**
         * Set the value of a column.
         *
         * @param columnIndex The index of the column to set.
         * @param value The column value.
         * @return This modifier.
         */
        public Modifier set(int columnIndex, Variant value) {
            if (columnIndex < 0 || columnIndex >= cells.length) {
                throw new IndexOutOfBoundsException();
            }

            cells[columnIndex] = value;

            return this;
        }

        /**
         * Create a row copy from the current state.
         *
         * @return The new row.
         */
        public Row create() {
            return new Row(header, Arrays.copyOf(cells, cells.length));
        }

        private Modifier(Header header, Variant[] cells) {
            this.header = header;
            this.cells = cells;
        }
    }

    @Override
    public String toString() { return Arrays.toString(cells); }
}
