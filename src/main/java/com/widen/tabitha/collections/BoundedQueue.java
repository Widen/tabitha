package com.widen.tabitha.collections;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An efficient, fixed-size queue that is closeable.
 * <p>
 * This queue uses internal synchronization and is fully thread-safe.
 */
public class BoundedQueue<T> implements Closeable
{
    private final CircularFifoQueue<T> queue;
    private final AtomicBoolean closed;
    private final Lock lock;
    private final Condition canEnqueue;
    private final Condition canDequeue;

    /**
     * Create a new queue.
     *
     * @param capacity The queue capacity.
     */
    public BoundedQueue(int capacity)
    {
        queue = new CircularFifoQueue<>(capacity);
        closed = new AtomicBoolean();
        lock = new ReentrantLock();
        canEnqueue = lock.newCondition();
        canDequeue = lock.newCondition();
    }

    /**
     * Check if the queue is closed. There may still be deque-able items in the queue after it is closed.
     *
     * @return True if {@link #close} has been called.
     */
    public boolean isClosed()
    {
        return closed.get();
    }

    /**
     * Add an item to the back of the queue.
     * <p>
     * If the queue is currently full, this method will block until either an item is removed, or the queue is closed.
     *
     * @param item The item to enqueue.
     * @return True if the item was added, false if the queue is closed.
     */
    public boolean enqueue(T item)
    {
        if (isClosed())
        {
            return false;
        }

        lock.lock();

        try
        {
            while (queue.isAtFullCapacity())
            {
                canEnqueue.awaitUninterruptibly();

                if (isClosed())
                {
                    return false;
                }
            }

            queue.add(item);
            canDequeue.signal();
            return true;
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Removes an item from the front of the queue.
     * <p>
     * If the queue is currently empty, this method will block until either an item is added, or the queue is closed.
     *
     * @return The removed item, or null if the queue is empty and closed.
     */
    public T dequeue()
    {
        lock.lock();

        try
        {
            while (queue.isEmpty())
            {
                if (isClosed())
                {
                    return null;
                }

                canDequeue.awaitUninterruptibly();
            }

            T item = queue.remove();
            canEnqueue.signal();
            return item;
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Clear all items from the queue.
     */
    public void clear()
    {
        lock.lock();

        try
        {
            queue.clear();
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Close the queue, preventing additional items from being added.
     */
    @Override
    public void close()
    {
        if (!closed.getAndSet(true))
        {
            lock.lock();

            try
            {
                canEnqueue.signalAll();
                canDequeue.signalAll();
            }
            finally
            {
                lock.unlock();
            }
        }
    }
}
