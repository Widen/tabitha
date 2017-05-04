package com.widen.tabitha;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Reads data rows from a data source.
 */
@FunctionalInterface
public interface RowReader extends Iterable<Row>, Closeable
{
    /**
     * A row reader that produces no rows.
     *
     * Closing has no effect on this row reader and is always re-usable.
     */
    RowReader VOID = Optional::empty;

    /**
     * Create a row reader from an array of rows.
     *
     * @param rows Rows to create from.
     * @return The new reader.
     */
    static RowReader from(Row... rows)
    {
        return new RowReader()
        {
            private int index = 0;

            @Override
            public Optional<Row> read() throws IOException
            {
                if (index < rows.length)
                {
                    return Optional.of(rows[index++]);
                }

                return Optional.empty();
            }
        };
    }

    /**
     * Create a row reader from an iterator.
     *
     * Calling {@link #read} on the returned row reader will advance the iterator if more items remain, or return empty
     * when the end of the iterator is reached.
     *
     * @param iterator Iterator to create from.
     * @return The new reader.
     */
    static RowReader from(Iterator<Row> iterator)
    {
        return () ->
        {
            if (iterator.hasNext())
            {
                return Optional.of(iterator.next());
            }

            return Optional.empty();
        };
    }

    /**
     * Create a row reader from an iterable.
     *
     * @param iterable Iterable to create from.
     * @return The new reader.
     */
    static RowReader from(Iterable<Row> iterable)
    {
        return from(iterable.iterator());
    }

    /**
     * Create a row reader from a {@link Stream}.
     *
     * @param stream Stream to create from.
     * @return The new reader.
     */
    static RowReader from(Stream<Row> stream)
    {
        return from(stream.iterator());
    }

    /**
     * Create a new reader that produces rows from the given readers in sequence.
     *
     * @param rowReaders The row readers to chain.
     * @return The chained reader.
     */
    static RowReader chain(RowReader... rowReaders)
    {
        return new RowReader()
        {
            private int index = 0;

            @Override
            public Optional<Row> read() throws IOException
            {
                while (index < rowReaders.length)
                {
                    Optional<Row> row = rowReaders[index].read();

                    if (row.isPresent())
                    {
                        return row;
                    }

                    ++index;
                }

                return Optional.empty();
            }

            @Override
            public void close() throws IOException
            {
                for (RowReader rowReader : rowReaders)
                {
                    rowReader.close();
                }
            }
        };
    }

    /**
     * Create a new reader that joins columns from this reader with columns from another reader.
     *
     * @param rowReaders Another row reader to join with.
     * @return The joined row reader.
     */
    static RowReader zip(RowReader... rowReaders)
    {
        return new RowReader()
        {
            private final Row[] rows = new Row[rowReaders.length];
            private boolean stillMore = true;

            @Override
            public Optional<Row> read() throws IOException
            {
                if (!stillMore)
                {
                    return Optional.empty();
                }

                // Read one row from all readers.
                stillMore = false;
                for (int i = 0; i < rows.length; ++i)
                {
                    rows[i] = rowReaders[i].read().orElse(null);
                    if (rows[i] != null)
                    {
                        stillMore = true;
                    }
                }

                if (stillMore)
                {
                    return Optional.of(Row.merge(rows));
                }
                else
                {
                    return Optional.empty();
                }
            }

            @Override
            public void close() throws IOException
            {
                for (RowReader rowReader : rowReaders)
                {
                    rowReader.close();
                }
            }
        };
    }

    /**
     * Attempt to read the next row.
     *
     * @throws IOException Thrown if an I/O error occurs.
     * @return The next row if read, or an empty {@link Optional} if the end of the reader has been reached.
     */
    Optional<Row> read() throws IOException;

    /**
     * Create a new reader that filters every row read based on a predicate.
     *
     * @param predicate A predicate to apply to each row to determine if it should be included.
     * @return A filtered reader.
     */
    default RowReader filter(Predicate<Row> predicate)
    {
        return new RowReader()
        {
            @Override
            public Optional<Row> read() throws IOException
            {
                for (Row row : RowReader.this)
                {
                    if (predicate.test(row))
                    {
                        return Optional.of(row);
                    }
                }

                return Optional.empty();
            }

            @Override
            public void close() throws IOException
            {
                RowReader.this.close();
            }
        };
    }

    /**
     * Filter rows returned based on the value of a given column. If the given column is not set for a row, that row is
     * filtered out.
     *
     * @param column The column to filter by.
     * @param predicate A predicate to apply to each value in the column to determine if the row should be included.
     * @return A filtered row reader.
     */
    default RowReader filterBy(String column, Predicate<Variant> predicate)
    {
        return filter(row -> row.get(column)
            .map(predicate::test)
            .orElse(false));
    }

    /**
     * Create a new reader that applies a function to every row read.
     *
     * @param mapper A function to apply to each row.
     * @return A mapped reader.
     */
    default RowReader map(Function<Row, Row> mapper)
    {
        return new RowReader()
        {
            @Override
            public Optional<Row> read() throws IOException
            {
                return RowReader.this.read().map(mapper);
            }

            @Override
            public void close() throws IOException
            {
                RowReader.this.close();
            }
        };
    }

    /**
     * Get a new row reader that returns the values for only the given columns.
     *
     * @param columns The names of columns to keep.
     * @return The new row reader.
     */
    default RowReader select(String... columns)
    {
        return map(row -> row.select(columns));
    }

    /**
     * Get a new row reader that returns the values for only columns in the given range.
     *
     * @param start The start index, inclusive.
     * @param end The ending index, exclusive.
     * @return The new row reader.
     */
    default RowReader range(int start, int end)
    {
        return map(row -> row.range(start, end));
    }

    /**
     * Get a new row reader that skips a certain number of rows.
     *
     * @param count The number of rows to skip.
     * @return The new row reader.
     */
    default RowReader skip(int count)
    {
        return new RowReader()
        {
            private int skipped = 0;

            @Override
            public Optional<Row> read() throws IOException
            {
                while (skipped < count)
                {
                    RowReader.this.read();
                    ++skipped;
                }

                return RowReader.this.read();
            }

            @Override
            public void close() throws IOException
            {
                RowReader.this.close();
            }
        };
    }

    /**
     * Get a new row reader that reads only up to the given number of rows.
     *
     * @param count The maximum number of rows to read.
     * @return The new row reader.
     */
    default RowReader take(int count)
    {
        return new RowReader()
        {
            private int index = 0;

            @Override
            public Optional<Row> read() throws IOException
            {
                if (index < count)
                {
                    Optional<Row> row = RowReader.this.read();

                    if (row.isPresent())
                    {
                        ++index;
                    }

                    return row;
                }

                return Optional.empty();
            }

            @Override
            public void close() throws IOException
            {
                RowReader.this.close();
            }
        };
    }

    /**
     * Pipe all remaining rows in this reader into a writer.
     *
     * @param rowWriter A row writer to write all rows to.
     * @throws IOException Thrown if an I/O error occurs.
     */
    default void pipe(RowWriter rowWriter) throws IOException
    {
        for (Row row : this)
        {
            rowWriter.write(row);
        }
    }

    @Override
    default Iterator<Row> iterator()
    {
        return new Iterator<Row>()
        {
            private Row nextRow;

            @Override
            public boolean hasNext()
            {
                if (nextRow == null)
                {
                    try
                    {
                        nextRow = read().orElse(null);
                    }
                    catch (IOException e)
                    {
                        return false;
                    }
                }

                return nextRow != null;
            }

            @Override
            public Row next()
            {
                if (nextRow == null)
                {
                    try
                    {
                        nextRow = read().orElse(null);
                    }
                    catch (IOException e)
                    {
                        return null;
                    }
                }

                Row row = nextRow;
                nextRow = null;
                return row;
            }
        };
    }

    // Provide a default close method that does nothing.
    @Override
    default void close() throws IOException
    {
    }
}
