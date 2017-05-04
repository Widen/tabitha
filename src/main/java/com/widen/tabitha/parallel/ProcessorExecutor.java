package com.widen.tabitha.parallel;

import com.widen.tabitha.Row;
import com.widen.tabitha.RowReader;
import com.widen.tabitha.collections.BoundedQueue;

import java.util.Stack;

/**
 * Executes a row processor in an efficient multi-threaded manner.
 */
public class ProcessorExecutor {
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private final Stack<Thread> threadPool;
    private final BoundedQueue<Row> queue;
    private final RowProcessor processor;

    /**
     * Create a new processor executor with the default configuration.
     *
     * @param rowProcessor Processor to process rows with.
     * @return The new executor.
     */
    public static ProcessorExecutor createDefault(RowProcessor rowProcessor) {
        return new ProcessorExecutor(DEFAULT_BUFFER_SIZE, rowProcessor);
    }

    /**
     * Create a new processor executor.
     *
     * @param bufferSize   Maximum number of rows to queue.
     * @param rowProcessor Processor to process rows with.
     */
    public ProcessorExecutor(int bufferSize, RowProcessor rowProcessor) {
        threadPool = new Stack<>();
        queue = new BoundedQueue<>(bufferSize);
        processor = rowProcessor;
    }

    /**
     * Process all rows from the given reader using the given row processor.
     *
     * @param rowReader Reader to read rows from.
     */
    public void execute(RowReader rowReader) {
        queue.clear();

        // Spawn two consumer threads per processor.
        int count = Runtime.getRuntime().availableProcessors() * 2;
        for (int i = 0; i < count; ++i) {
            Thread thread = new Thread(this::consumer);
            thread.start();

            threadPool.push(thread);
        }

        processor.onStart();

        producer(rowReader);

        // Wait for all consumer threads to finish.
        shutdown();

        processor.onComplete();
    }

    /**
     * Fill the queue with rows read from the given reader.
     *
     * @param rowReader Row reader to read from.
     */
    private void producer(RowReader rowReader) {
        // Block the current thread, filling the queue up as needed until we reach the end of the reader.
        rowReader.forEach(queue::enqueue);

        // Done reading, close the queue.
        queue.close();
    }

    /**
     * Consume rows from the queue and process them.
     */
    private void consumer() {
        while (true) {
            Row row = queue.dequeue();

            if (row == null) {
                // Queue is closed, shut down.
                break;
            }

            processor.process(row);
        }
    }

    /**
     * Shutdown all consumer threads in the pool.
     */
    private void shutdown() {
        // Wait for all threads to terminate.
        while (!threadPool.isEmpty()) {
            Thread thread = threadPool.pop();
            while (true) {
                try {
                    thread.join();
                    break;
                } catch (InterruptedException e) {
                    // Failed to join, retry.
                }
            }
        }
    }
}
