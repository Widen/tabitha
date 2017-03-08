package com.widen.tabitha.collections;

import java.util.Optional;

/**
 * Circular ring buffer backed by a growable array. Provides O(1) implementations for most methods.
 */
// Custom implementation, because none of the collections out there had the memory and performance optimizations that
// I knew would give the best results for DataFrame.
public class RingBuffer<E>
{
    private static final int DEFAULT_INITIAL_CAPACITY = 32;

    private Object[] buffer;
    private int head;
    private int size;

    public RingBuffer()
    {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public RingBuffer(int initialCapacity)
    {
        buffer = new Object[initialCapacity];
        head = 0;
        size = 0;
    }

    @SafeVarargs
    public RingBuffer(E... elements)
    {
        buffer = elements;
        head = 0;
        size = elements.length;
    }

    public boolean isEmpty()
    {
        return size == 0;
    }

    public int size()
    {
        return size;
    }

    public Optional<E> get(int index)
    {
        if (index < size)
        {
            return Optional.of(getAtOffset(offsetOf(index)));
        }

        return Optional.empty();
    }

    public void pushFront(E element)
    {
        if (size == buffer.length)
        {
            resize(buffer.length * 2);
        }

        if (head == 0)
        {
            head = buffer.length - 1;
        }
        else
        {
            --head;
        }

        buffer[head] = element;
        ++size;
    }

    public void pushBack(E element)
    {
        if (size == buffer.length)
        {
            resize(buffer.length * 2);
        }

        buffer[offsetOf(size)] = element;
        ++size;
    }

    public Optional<E> popFront()
    {
        if (size == 0)
        {
            return Optional.empty();
        }

        E element = getAtOffset(head);
        buffer[head] = null;
        ++head;
        --size;

        return Optional.of(element);
    }

    public Optional<E> popBack()
    {
        if (size == 0)
        {
            return Optional.empty();
        }

        int offset = offsetOf(size - 1);
        E element = getAtOffset(offset);
        buffer[offset] = null;
        --size;

        return Optional.of(element);
    }

    private int offsetOf(int index)
    {
        int offset = head + index;
        if (offset >= buffer.length)
        {
            offset = offset % buffer.length;
        }
        return offset;
    }

    private E getAtOffset(int offset)
    {
        @SuppressWarnings("unchecked")
        E element = (E) buffer[offset];
        return element;
    }

    private void resize(int capacity)
    {
        if (capacity > buffer.length)
        {
            Object[] dest = new Object[capacity];

            // While we're reallocating, reorganize the new array to be contiguous.
            if (head + size > buffer.length)
            {
                int headSize = buffer.length - head;
                System.arraycopy(buffer, head, dest, 0, headSize);
                System.arraycopy(buffer, 0, dest, headSize, size - headSize);
            }
            else
            {
                System.arraycopy(buffer, head, dest, 0, size);
            }

            buffer = dest;
            head = 0;
        }
    }
}
