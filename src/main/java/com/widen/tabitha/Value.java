package com.widen.tabitha;

import java.util.Optional;

/**
 * A boxed type for a typed primitive value.
 *
 * By default a value can either be a {@link String}, a {@link Bool}, an {@link Int}, a {@link Float}, or {@link #NONE}.
 */
public interface Value
{
    /**
     * Check if the value is empty.
     *
     * @return True if the value is equal to {@link #NONE}, otherwise false.
     */
    default boolean isNone()
    {
        return false;
    }

    /**
     * Get the string value if the value is a string type.
     */
    default Optional<java.lang.String> stringValue()
    {
        return Optional.empty();
    }

    /**
     * Get the boolean value if the value is a boolean type.
     */
    default Optional<Boolean> booleanValue()
    {
        return Optional.empty();
    }

    /**
     * Get the integer value as a long if the value is an integer type.
     */
    default Optional<Long> integerValue()
    {
        return Optional.empty();
    }

    /**
     * Get the float value as a double if the value is a float type.
     */
    default Optional<Double> floatValue()
    {
        return Optional.empty();
    }

    /**
     * Get the value as a string. Non-string values will be converted to a string.
     */
    java.lang.String asString();

    /**
     * Represents an empty value.
     */
    Value NONE = new Value() {
        @Override
        public boolean isNone()
        {
            return true;
        }

        @Override
        public java.lang.String asString()
        {
            return "";
        }
    };

    /**
     * A string value.
     */
    class String implements Value
    {
        private final java.lang.String value;

        public String(java.lang.String value)
        {
            this.value = value;
        }

        @Override
        public Optional<java.lang.String> stringValue()
        {
            return Optional.of(value);
        }

        @Override
        public java.lang.String asString()
        {
            return value;
        }

        @Override
        public boolean equals(Object other)
        {
            if (value.equals(other))
            {
                return true;
            }

            if (other instanceof String)
            {
                return value.equals(((String) other).value);
            }

            return false;
        }
    }

    /**
     * A boolean true or false value.
     */
    class Bool implements Value
    {
        /**
         * The boxed value of {@code true}.
         */
        public static final Bool TRUE = new Bool(true);

        /**
         * The boxed value of {@code false}.
         */
        public static final Bool FALSE = new Bool(false);

        // The value of the boolean.
        private boolean value;

        /**
         * Get a {@link Bool} instance representing the given boolean value.
         *
         * @param value The boolean value.
         * @return The equivalent value instance.
         */
        public static Bool valueOf(boolean value)
        {
            // Use static objects already initialized to save memory.
            return value ? TRUE : FALSE;
        }

        // Private to prevent more than two instances from existing.
        private Bool(boolean value)
        {
            this.value = value;
        }

        @Override
        public Optional<Boolean> booleanValue()
        {
            return Optional.of(value);
        }

        @Override
        public java.lang.String asString()
        {
            return Boolean.toString(value);
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == this)
            {
                return true;
            }

            if (other instanceof Boolean)
            {
                return value == (Boolean) other;
            }

            if (other instanceof Bool)
            {
                return value == ((Bool) other).value;
            }

            return false;
        }
    }

    /**
     * An integer value.
     */
    class Int implements Value
    {
        private long value;

        public Int(long value)
        {
            this.value = value;
        }

        @Override
        public Optional<Long> integerValue()
        {
            return Optional.of(value);
        }

        @Override
        public java.lang.String asString()
        {
            return Long.toString(value);
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == this)
            {
                return true;
            }

            if (other instanceof Number)
            {
                return value == ((Number) other).longValue();
            }

            if (other instanceof Int)
            {
                return value == ((Int) other).value;
            }

            return false;
        }
    }

    /**
     * A floating-point number. Stored as a double-width float.
     */
    class Float implements Value
    {
        private double value;

        public Float(double value)
        {
            this.value = value;
        }

        @Override
        public Optional<Double> floatValue()
        {
            return Optional.of(value);
        }

        @Override
        public java.lang.String asString()
        {
            return Double.toString(value);
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == this)
            {
                return true;
            }

            if (other instanceof Number)
            {
                return value == ((Number) other).doubleValue();
            }

            if (other instanceof Float)
            {
                return value == ((Float) other).value;
            }

            return false;
        }
    }
}
