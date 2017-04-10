package com.widen.tabitha.parallel;

import com.widen.tabitha.Row;
import com.widen.tabitha.RowReader;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Executes a row processor in an efficient multi-threaded manner.
 */
public class ProcessorExecutor
{
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private final Stack<Thread> threadPool;
    private final ArrayBlockingQueue<MaybePoison> buffer;
    private final RowProcessor processor;

    /**
     * Create a new processor executor with the default configuration.
     *
     * @param rowProcessor Processor to process rows with.
     */
    public static ProcessorExecutor createDefault(RowProcessor rowProcessor)
    {
        return new ProcessorExecutor(DEFAULT_BUFFER_SIZE, rowProcessor);
    }

    /**
     * Create a new processor executor.
     *
     * @param bufferSize Maximum number of rows to buffer.
     * @param rowProcessor Processor to process rows with.
     */
    public ProcessorExecutor(int bufferSize, RowProcessor rowProcessor)
    {
        threadPool = new Stack<>();
        buffer = new ArrayBlockingQueue<>(bufferSize);
        processor = rowProcessor;
    }

    /**
     * Process all rows from the given reader using the given row processor.
     *
     * @param rowReader Reader to read rows from.
     */
    public void execute(RowReader rowReader)
    {
        buffer.clear();

        // Spawn one consumer threads per processor.
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); ++i)
        {
            Thread thread = new Thread(this::consumeBuffer);
            thread.start();

            threadPool.push(thread);
        }

        processor.onStart();

        fillBuffer(rowReader);

        // Wait for all consumer threads to finish.
        shutdown();

        processor.onComplete();
    }

    /**
     * Fill the buffer with rows read from the given reader.
     *
     * @param rowReader Row reader to read from.
     */
    private void fillBuffer(RowReader rowReader)
    {
        // Block the current thread, filling the buffer up as needed until we reach the end of the reader.
        rowReader.forEach(row -> {
            while (true)
            {
                try
                {
                    buffer.put(new MaybePoison(row));
                    break;
                }
                catch (InterruptedException e)
                {
                    // Failed to put the row in the buffer, retry.
                }
            }
        });

        // Poison the queue to indicate we finished.
        while (true)
        {
            try
            {
                buffer.put(MaybePoison.POISON);
                break;
            }
            catch (InterruptedException e)
            {
                // Retry
            }
        }
    }

    /**
     * Consume rows from the buffer.
     */
    private void consumeBuffer()
    {
        while (true)
        {
            try
            {
                MaybePoison maybePoison = buffer.take();

                // Queue is poisoned, shut down.
                if (maybePoison == MaybePoison.POISON)
                {
                    buffer.put(maybePoison);
                    break;
                }

                processor.process(maybePoison.row);
            }
            catch (InterruptedException e)
            {
                // Retry
            }
        }
    }

    /**
     * Shutdown all consumer threads in the pool.
     */
    private void shutdown()
    {
        // Wait for all threads to terminate.
        while (!threadPool.isEmpty())
        {
            Thread thread = threadPool.pop();
            while (true)
            {
                try
                {
                    thread.join();
                    break;
                }
                catch (InterruptedException e)
                {
                    // Failed to join, retry.
                }
            }
        }
    }

    private static class MaybePoison
    {
        private static MaybePoison POISON = new MaybePoison(null);
        private final Row row;

        private MaybePoison(Row row)
        {
            this.row = row;
        }
    }
}
