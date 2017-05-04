package com.widen.tabitha.parallel;

import com.widen.tabitha.Row;

/**
 * Processes a stream of rows.
 */
@FunctionalInterface
public interface RowProcessor {
    /**
     * Process a single row.
     * <p>
     * This method is required to be thread-safe, as it may get called concurrently from multiple threads at once.
     *
     * @param row The row to process.
     */
    void process(Row row);

    /**
     * Method called when the processor begins processing a data set.
     */
    default void onStart() {
    }

    /**
     * Method called when the processor reaches the end of a data set.
     */
    default void onComplete() {
    }
}
