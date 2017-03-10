package com.widen.tabitha.collections;

import java.util.*;

/**
 * Circular ring buffer backed by a growable array. Null elements are not supported.
 */
// Custom implementation, because none of the collections out there had the memory and performance optimizations that
// I knew would give the best results for DataFrame.
public class RingBuffer<E> extends AbstractCollection<E> implements Collection<E>
{
    private static final int DEFAULT_INITIAL_CAPACITY = 32;

    private Object[] buffer;
    private int head;
    private int size;

    /**
     * Create a new empty ring buffer with the default capacity.
     */
    public RingBuffer()
    {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Create a new empty ring buffer with a given capacity.
     */
    public RingBuffer(int initialCapacity)
    {
        buffer = new Object[initialCapacity];
        head = 0;
        size = 0;
    }

    /**
     * Create a new ring buffer from the given array.
     */
    public RingBuffer(E[] elements)
    {
        buffer = elements;
        head = 0;
        size = elements.length;
    }

    /**
     * Gets the number of elements in the buffer.
     */
    @Override
    public int size()
    {
        return size;
    }

    /**
     * Gets the number of elements that the buffer can hold without reallocating.
     */
    public int capacity()
    {
        return buffer.length;
    }

    /**
     * Get an element in the buffer by index.
     */
    public Optional<E> get(int index)
    {
        if (index >= size || index < 0)
        {
            return Optional.empty();
        }

        return Optional.of(getAtOffset(wrappingOffset(index)));
    }

    /**
     * Remove the element by index.
     */
    public boolean remove(int index)
    {
        if (index >= size || index < 0)
        {
            return false;
        }

        int offset = wrappingOffset(index);

        if (offset < head)
        {
            int tail = wrappingOffset(size - 1);
            System.arraycopy(buffer, offset + 1, buffer, offset, tail - offset);
        }
        else
        {
            System.arraycopy(buffer, head, buffer, head + 1, index + 1);
            --head;
        }
        --size;

        return true;
    }

    /**
     * Get the element at the front of the buffer.
     */
    public Optional<E> front()
    {
        if (size == 0)
        {
            return Optional.empty();
        }

        E element = getAtOffset(head);

        return Optional.of(element);
    }

    /**
     * Get the element at the back of the buffer.
     */
    public Optional<E> back()
    {
        if (size == 0)
        {
            return Optional.empty();
        }

        int offset = wrappingOffset(size - 1);
        E element = getAtOffset(offset);

        return Optional.of(element);
    }

    /**
     * Add an element to the front of the buffer.
     */
    public void pushFront(E element)
    {
        if (size == buffer.length)
        {
            resize(buffer.length * 2);
        }

        head = wrappingOffset(-1);
        buffer[head] = element;
        ++size;
    }

    /**
     * Add an element to the back of the buffer.
     */
    public void pushBack(E element)
    {
        if (size == buffer.length)
        {
            resize(buffer.length * 2);
        }

        buffer[wrappingOffset(size)] = element;
        ++size;
    }

    /**
     * Remove the element at the front of the buffer.
     */
    public Optional<E> popFront()
    {
        if (size == 0)
        {
            return Optional.empty();
        }

        E element = getAtOffset(head);
        buffer[head] = null;
        head = wrappingOffset(1);
        --size;

        return Optional.of(element);
    }

    /**
     * Remove the element at the back of the buffer.
     */
    public Optional<E> popBack()
    {
        if (size == 0)
        {
            return Optional.empty();
        }

        int offset = wrappingOffset(size - 1);
        E element = getAtOffset(offset);
        buffer[offset] = null;
        --size;

        return Optional.of(element);
    }

    @Override
    public void clear()
    {
        while (size > 0)
        {
            buffer[wrappingOffset(size - 1)] = null;
            --size;
        }
    }

    @Override
    public Object[] toArray()
    {
        Object[] dest = new Object[size];
        return toArray(dest);
    }

    @Override
    public <T> T[] toArray(T[] array)
    {
        Object[] dest = array;

        // If the given array is not large enough, allocate a new one.
        if (dest.length < size)
        {
            dest = new Object[size];
        }

        // Current buffer is wrapped, copy head segment first and then the tail segment.
        if (head + size > buffer.length)
        {
            int headSize = buffer.length - head;
            System.arraycopy(buffer, head, dest, 0, headSize);
            System.arraycopy(buffer, 0, dest, headSize, size - headSize);
        }
        // Buffer is contiguous, copy in one step.
        else
        {
            System.arraycopy(buffer, head, dest, 0, size);
        }

        return array;
    }

    @Override
    public Iterator<E> iterator()
    {
        return new Iterator<E>()
        {
            private int nextIndex = 0;
            private int lastIndex = -1;

            @Override
            public boolean hasNext()
            {
                return nextIndex < size;
            }

            @Override
            public E next()
            {
                lastIndex = nextIndex;
                ++nextIndex;

                return get(lastIndex).orElseThrow(NoSuchElementException::new);
            }

            @Override
            public void remove()
            {
                if (lastIndex < 0)
                {
                    throw new IllegalStateException();
                }

                if (!RingBuffer.this.remove(lastIndex))
                {
                    throw new IllegalStateException();
                }

                lastIndex = -1;
                --nextIndex;
            }
        };
    }

    private int wrappingOffset(int index)
    {
        int offset = head + index;

        if (offset >= buffer.length)
        {
            offset -= buffer.length;
        }
        else if (offset < 0)
        {
            offset += buffer.length;
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
            // Create a new array with double the current capacity.
            Object[] dest = new Object[capacity];

            // Copy the elements into the new array.
            dest = toArray(dest);

            // Replace the buffer.
            buffer = dest;
            head = 0;
        }
    }
}
