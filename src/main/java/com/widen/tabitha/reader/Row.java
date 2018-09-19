package com.widen.tabitha.reader;

import com.widen.tabitha.Variant;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import org.apache.commons.collections4.iterators.ArrayIterator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Stores a single row of data values, ordered by column position.
 * <p>
 * Row objects are always immutable. To modify a row, several methods are provided that instead return a copy of the row
 * with the requested changes incorporated. This offers several benefits including performance and thread safety, since
 * a deep copy of row data is never required and we can reuse object references everywhere without fear of concurrent
 * modification.
 */
@Wither
@AllArgsConstructor
public class Row implements Iterable<Variant> {
    private final Header header;
    private final long page;
    private final long index;
    private final Variant[] cells;

    public Row(Header header, long page, long index, Collection<Variant> cells) {
        this(header, page, index, cells.toArray(new Variant[0]));
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
     * Get the page number this row was found in. Page numbers start from index 0.
     *
     * @return The row index.
     */
    public long page() {
        return page;
    }

    /**
     * Get the index of the row in the source file. Rows start from index 0.
     *
     * @return The row index.
     */
    public long index() {
        return index;
    }

    /**
     * Get the cell values in the row from left to right.
     *
     * @return The cell values.
     */
    public Stream<Variant> cells() {
        return Arrays.stream(cells);
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
     * Get the values of the cells in the given index range.
     *
     * @param start The start index, inclusive.
     * @param end The ending index, exclusive.
     * @return The selected cell values.
     */
    public Stream<Variant> range(int start, int end) {
        return cells().skip(start).limit(end - start);
    }

    /**
     * Get the values of the cells corresponding to the given column names.
     * <p>
     * If there is no cell mapping for any of the given column names, that value will instead be filled in with
     * {@link Variant#NONE}.
     *
     * @param columns The names of columns to select.
     * @return The selected cell values.
     */
    public Stream<Variant> select(String... columns) {
        return Arrays.stream(columns)
            .map(column -> get(column).orElse(Variant.NONE));
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

    @Override
    public String toString() {
        return Arrays.toString(cells);
    }
}