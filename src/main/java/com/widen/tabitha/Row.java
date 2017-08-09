package com.widen.tabitha;

import org.apache.commons.collections4.iterators.ArrayIterator;

import java.util.*;
import java.util.function.Function;

/**
 * Stores a single row of data values, ordered by column position.
 */
public class Row implements Iterable<Variant> {
    // The column schema this row is mapped to. May be null.
    private final Schema schema;

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

        for (Variant cell : cells) {
            if (cell == null) {
                cells[index] = Variant.NONE;
            } else {
                cells[index] = cell;
            }

            index += 1;
        }

        return new Row(null, cells);
    }

    /**
     * Create a row containing the given cell values. This does not do any null checks.
     *
     * @param schema The column schema to use.
     * @param cells The cells to contain.
     */
    Row(Schema schema, Variant[] cells) {
        this.schema = schema;
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
     * Get the schema used by this row, if any.
     *
     * @return The schema.
     */
    public Optional<Schema> schema() {
        return Optional.ofNullable(schema);
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
        if (schema != null) {
            return schema.indexOf(name).flatMap(this::get);
        }

        return Optional.empty();
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
        }

        return new Row(schema, mapped);
    }

    /**
     * Get a new row that contains the values for only columns in the given range.
     *
     * @param start The start index, inclusive.
     * @param end   The ending index, exclusive.
     * @return The new row.
     */
    public Row range(int start, int end) {
        if (start < 0 || end > cells.length) {
            throw new IndexOutOfBoundsException();
        }

        return new Row(schema, Arrays.copyOfRange(cells, start, end));
    }

    /**
     * Get a new row that contains the values for the given columns.
     *
     * If there is no cell mapping for any of the given column names, that value will instead be filled in with
     * {@link Variant#NONE}.
     *
     * @param columns The names of columns to select.
     * @return The new row.
     */
    public Row select(String... columns) {
        ArrayList<Variant> cells = new ArrayList<>();

        for (String column : columns) {
            cells.add(get(column).orElse(Variant.NONE));
        }

        return new Row(schema, cells.toArray(new Variant[cells.size()]));
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
}
