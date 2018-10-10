package com.widen.tabitha.reader;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import lombok.SneakyThrows;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Reads data rows from a data source.
 * <p>
 * A data source stores rows in a sequential order, grouped into one or more pages. Pages are ordered sequentially and
 * rows are always read from the current page.
 */
public interface RowReader extends Iterable<Row>, Closeable {
    /**
     * A row reader that produces no rows.
     * <p>
     * Closing has no effect on this row reader and is always re-usable.
     */
    RowReader VOID = Optional::empty;

    /**
     * Create a row reader from an array of rows.
     *
     * @param rows Rows to create from.
     * @return The new reader.
     */
    static RowReader from(Row... rows) {
        return from(Arrays.asList(rows));
    }

    /**
     * Create a row reader from a {@link Stream}.
     *
     * @param stream Stream to create from.
     * @return The new reader.
     */
    static RowReader from(Stream<Row> stream) {
        return from(stream.iterator());
    }

    /**
     * Create a row reader from a {@link Flowable}.
     *
     * @param observable Observable to create from.
     * @return The new reader.
     */
    static RowReader from(Observable<Row> observable) {
        return from(observable.blockingIterable());
    }

    /**
     * Create a row reader from a {@link Flowable}.
     *
     * @param flowable Flowable to create from.
     * @return The new reader.
     */
    static RowReader from(Flowable<Row> flowable) {
        return from(flowable.blockingIterable());
    }

    /**
     * Create a row reader from an iterable.
     *
     * @param iterable Iterable to create from.
     * @return The new reader.
     */
    static RowReader from(Iterable<Row> iterable) {
        return from(iterable.iterator());
    }

    /**
     * Create a row reader from an iterator.
     * <p>
     * Calling {@link #read} on the returned row reader will advance the iterator if more items remain, or return empty
     * when the end of the iterator is reached.
     *
     * @param iterator Iterator to create from.
     * @return The new reader.
     */
    static RowReader from(Iterator<Row> iterator) {
        return new RowReader() {
            @Override
            public Optional<Row> read() {
                if (iterator.hasNext()) {
                    return Optional.of(iterator.next());
                }

                return Optional.empty();
            }

            @Override
            @SneakyThrows
            public void close() {
                if (iterator instanceof Disposable) {
                    ((Disposable) iterator).dispose();
                }
                else if (iterator instanceof AutoCloseable) {
                    ((AutoCloseable) iterator).close();
                }
            }
        };
    }

    /**
     * Attempt to read the next row.
     *
     * @return The next row if read, or an empty {@link Optional} if the end of the reader has been reached.
     * @throws IOException Thrown if an I/O error occurs.
     */
    Optional<Row> read() throws IOException;

    /**
     * Create a new row reader that emits rows from this reader with its indexes normalized to be sequential.
     * <p>
     * The page index and row index of each row emitted will be changed to be sequential. For example, if the inner row
     * reader emits a row with indexes 0, 1, 2, and 5, the normalizer will change the indexes to be 0, 1, 2, and 3. The
     * same normalizing is done on the page indexes. Data in the rows will not be changed.
     *
     * @return A new row reader.
     */
    default RowReader withSequentialIndexes() {
        return new IndexNormalizerReader(this);
    }

    /**
     * Create a new row reader that fills gaps between rows with blank rows.
     *
     * @return A new row reader.
     */
    default RowReader withBlankRows() {
        return new BlankRowReader(this);
    }

    /**
     * Convert this reader into a reactive stream of rows.
     *
     * @return A reactive stream of rows.
     */
    default Flowable<Row> rows() {
        return Observable.
            <Row, RowReader>generate(() -> this, (reader, emitter) -> {
                try {
                    Optional<Row> row = reader.read();
                    if (row.isPresent()) {
                        emitter.onNext(row.get());
                    }
                    else {
                        emitter.onComplete();
                    }
                }
                catch (IOException e) {
                    emitter.onError(e);
                }
            })
            .toFlowable(BackpressureStrategy.BUFFER)
            .share();
    }

    @Override
    default Iterator<Row> iterator() {
        return rows().blockingIterable().iterator();
    }

    // Provide a default close method that does nothing.
    @Override
    default void close() throws IOException {
    }
}
