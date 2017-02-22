package com.widen.tabitha;

import java.util.Optional;

/**
 * A wrapper for a dynamically typed value.
 */
public interface Value
{
    /**
     * Check if the value can be converted to the given type.
     */
    boolean isConvertibleTo(Class<?> type);

    /**
     * Get the value as the given type.
     */
    <T> Optional<T> as(Class<? extends T> type);

    /**
     * Get the value as a string.
     */
    String asString();
}
