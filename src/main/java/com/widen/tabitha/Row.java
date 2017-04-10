package com.widen.tabitha;

import java.util.*;

/**
 * Stores a single row of data values, indexed by column.
 */
public class Row implements Iterable<Row.Cell>
{
    private final Row.Cell[] cells;

    /**
     * Create a new row that merges the columns and values of all the given rows.
     *
     * @param rows The rows to merge.
     * @return The merged row.
     */
    public static Row merge(Row... rows)
    {
        List<Cell> cells = new ArrayList<>();

        for (int i = 0; i < rows.length; ++i)
        {
            cells.addAll(Arrays.asList(rows[i].cells));
        }

        return new Row(cells.toArray(new Cell[cells.size()]));
    }

    /**
     * Create a row containing the given cells.
     *
     * @param cells The cells to contain.
     */
    Row(Cell... cells)
    {
        this.cells = cells;
    }

    /**
     * Get the number of values in the row.
     *
     * @return The number of values in the row.
     */
    public int size()
    {
        return cells.length;
    }

    /**
     * Get the value of a cell by column name.
     *
     * @param name The name of the column.
     * @return The value for the given column, if present.
     */
    public Optional<Value> get(String name)
    {
        for (Cell cell : cells)
        {
            if (cell.column.name.equals(name))
            {
                return Optional.of(cell.value);
            }
        }

        return Optional.empty();
    }

    /**
     * Get the value of a cell by index.
     *
     * @param index The index of the column.
     * @return The value for the given column, if present.
     */
    public Optional<Value> get(int index)
    {
        if (index >= cells.length)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(cells[index].value);
    }

    /**
     * Returns an array containing the cell columns used in the row.
     *
     * @return Array of columns.
     */
    public Column[] columns()
    {
        return Utils.mapArray(cells, cell -> cell.column);
    }

    /**
     * Returns an array containing the cell values.
     *
     * @return Array of values.
     */
    public Value[] values()
    {
        return Utils.mapArray(cells, cell -> cell.value);
    }

    /**
     * Get a new row that contains the values for only the given columns.
     *
     * @param columns The names of columns to keep.
     * @return The new row.
     */
    public Row select(String... columns)
    {
        List<String> columnsToRetain = Arrays.asList(columns);
        ArrayList<Cell> cells = new ArrayList<>();

        for (Cell cell : this)
        {
            if (columnsToRetain.contains(cell.column.name))
            {
                cells.add(cell);
            }
        }

        return new Row(cells.toArray(new Cell[cells.size()]));
    }

    @Override
    public Iterator<Cell> iterator()
    {
        return new Iterator<Cell>()
        {
            private int index = 0;

            @Override
            public boolean hasNext()
            {
                return index < cells.length;
            }

            @Override
            public Cell next()
            {
                return cells[index];
            }
        };
    }

    /**
     * A single cell from a row containing a value.
     */
    public static final class Cell
    {
        /**
         * The column the cell belongs to.
         */
        public final Column column;

        /**
         * The cell value.
         */
        public final Value value;

        /**
         * Create a new cell.
         *
         * @param column The cell column.
         * @param value The cell value.
         */
        Cell(Column column, Value value)
        {
            if (column == null || value == null)
            {
                throw new NullPointerException();
            }

            this.column = column;
            this.value = value;
        }
    }
}
