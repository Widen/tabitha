package com.widen.tabitha;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Provides various supporting utility methods used within Tabitha.
 */
public final class Utils {
    /**
     * Apply a function to an array and return a new array with the transformed values.
     *
     * @param array    The array to map.
     * @param function The function to apply.
     * @param <T>      The item type of the original array.
     * @param <R>      The item type of the new array.
     * @return The new array.
     */
    public static <T, R> R[] mapArray(T[] array, Class<R> type, Function<T, R> function) {
        @SuppressWarnings("unchecked")
        R[] mapped = (R[]) Array.newInstance(type, array.length);

        for (int i = 0; i < array.length; ++i) {
            mapped[i] = function.apply(array[i]);
        }

        return mapped;
    }

    /**
     * Create an iterator that applies a function to every item yielded by the original iterator.
     *
     * @param iterator The iterator to map.
     * @param function The function to apply.
     * @param <T>      The item type of the original iterator.
     * @param <R>      The item type of the new iterator.
     * @return The new iterator.
     */
    public static <T, R> Iterator<R> mapIterator(Iterator<T> iterator, Function<T, R> function) {
        return new Iterator<R>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public R next() {
                return function.apply(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    /**
     * Create an iterable that applies a function to every item yielded by the original iterable.
     *
     * @param iterable The iterable to map.
     * @param function The function to apply.
     * @param <T>      The item type of the original iterator.
     * @param <R>      The item type of the new iterator.
     * @return The new iterable.
     */
    public static <T, R> Iterable<R> mapIterable(Iterable<T> iterable, Function<T, R> function) {
        return () -> mapIterator(iterable.iterator(), function);
    }
}
