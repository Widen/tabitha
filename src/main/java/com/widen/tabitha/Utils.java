package com.widen.tabitha;

import java.util.function.Function;

public class Utils
{
    public static <T, R> R[] mapArray(T[] array, Function<T, R> function)
    {
        Object[] objects = new Object[array.length];

        for (int i = 0; i < array.length; ++i)
        {
            objects[i] = function.apply(array[i]);
        }

        @SuppressWarnings("unchecked")
        R[] mapped = (R[]) objects;

        return mapped;
    }
}
