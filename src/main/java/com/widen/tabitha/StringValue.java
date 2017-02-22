package com.widen.tabitha;

import java.util.Optional;

public class StringValue implements Value
{
    private final String value;

    public StringValue(String value)
    {
        this.value = value;
    }

    @Override
    public boolean isConvertibleTo(Class<?> type)
    {
        return type != null && type.isAssignableFrom(String.class);
    }

    @Override
    public <T> Optional<T> as(Class<? extends T> type)
    {
        if (isConvertibleTo(type))
        {
            return Optional.of(type.cast(value));
        }

        return Optional.empty();
    }

    @Override
    public String asString()
    {
        return value;
    }
}
