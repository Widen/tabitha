package com.widen.tabitha;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reads data rows from a data source.
 */
@FunctionalInterface
public interface RowReader extends Iterable<Row>, Closeable
{
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
        return () ->
        {
            for (Row row : this)
            {
                if (predicate.test(row))
                {
                    return Optional.of(row);
                }
            }

            return Optional.empty();
        };
    }

    /**
     * Create a new reader that applies a function to every row read.
     *
     * @param mapper A function to apply to each row.
     * @return A mapped reader.
     */
    default RowReader map(Function<Row, Row> mapper)
    {
        return () -> read().map(mapper);
    }

    /**
     * Get a new row reader that returns the values for only the given columns.
     *
     * @param columns The names of columns to keep.
     * @return The new row reader.
     */
    default RowReader columns(String... columns)
    {
        return map(row ->
        {
            // TODO: This is pretty ugly. Improve the Row/ColumnIndex API.
            Set<String> columnsToKeep = new HashSet<>(Arrays.asList(columns));
            ColumnIndex.Builder builder = new ColumnIndex.Builder();
            ArrayList<Value> values = new ArrayList<>();

            for (String column : row.columns())
            {
                if (columnsToKeep.contains(column))
                {
                    builder.addColumn(column);
                    values.add(row.get(column).get());
                }
            }

            return new Row(builder.build(), values.toArray(new Value[values.size()]));
        });
    }

    /**
     * Filter rows returned based on the value of a given column. If the given column is not set for a row, that row is
     * filtered out.
     *
     * @param predicate A predicate to apply to each value in the column to determine if the row should be included.
     * @return A filtered row reader.
     */
    default RowReader filterColumn(String column, Predicate<Value> predicate)
    {
        return filter(row -> row.get(column)
            .map(predicate::test)
            .orElse(false));
    }

    /**
     * Create a new reader that joins columns from this reader with columns from another reader.
     *
     * @param rowReader Another row reader to join with.
     * @return The joined row reader.
     */
    default RowReader zip(RowReader rowReader)
    {
        return () ->
        {
            Optional<Row> left = read();
            Optional<Row> right = rowReader.read();

            if (!left.isPresent() && !right.isPresent())
            {
                // TODO: We need to close both readers in the close() method.
                return Optional.empty();
            }

            ColumnIndex.Builder builder = new ColumnIndex.Builder();
            ArrayList<Value> values = new ArrayList<>();

            left.ifPresent(row ->
            {
                row.columns().forEach(builder::addColumn);
                row.forEach(values::add);
            });

            right.ifPresent(row ->
            {
                row.columns().forEach(builder::addColumn);
                row.forEach(values::add);
            });

            return Optional.of(new Row(
                builder.build(),
                values.toArray(new Value[values.size()])
            ));
        };
    }

    /**
     * Pipe all remaining rows in this reader into a writer.
     *
     * @param rowWriter A row writer to write all rows to.
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
