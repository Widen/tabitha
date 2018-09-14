package com.widen.tabitha;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import lombok.SneakyThrows;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

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
public interface RowReader extends Iterable<Row>, Publisher<Row>, Closeable {
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
     * Convert this reader into a reactive stream of rows.
     *
     * @return A reactive stream of rows.
     */
    default Flowable<Row> rows() {
        return Flowable.generate(
            () -> this,
            (reader, emitter) -> {
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
            },
            RowReader::close
        );
    }

    /**
     * Pipe all remaining rows in this reader into a writer.
     *
     * @param rowWriter A row writer to write all rows to.
     * @throws IOException Thrown if an I/O error occurs.
     */
    default void pipe(RowWriter rowWriter) throws IOException {
        for (Row row : this) {
            rowWriter.write(row);
        }
    }

    @Override
    default Iterator<Row> iterator() {
        return rows().blockingIterable().iterator();
    }

    @Override
    default void subscribe(Subscriber<? super Row> subscriber) {
        rows().subscribe(subscriber);
    }

    // Provide a default close method that does nothing.
    @Override
    default void close() throws IOException {
    }
}
