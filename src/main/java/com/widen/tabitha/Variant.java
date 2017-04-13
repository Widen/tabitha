package com.widen.tabitha;

import java.util.Optional;

/**
 * A boxed type for a typed primitive value.
 * <p>
 * A variant can either be a {@link Bool}, a {@link String}, an {@link Int}, a {@link Float}, or {@link #NONE}.
 */
public abstract class Variant
{
    /**
     * Create a new boolean variant.
     *
     * @param value The boolean value.
     * @return The new variant.
     */
    public static Variant of(boolean value)
    {
        // Use static objects already initialized to save memory.
        return value ? Bool.TRUE : Bool.FALSE;
    }

    /**
     * Create a new string variant.
     *
     * @param value The string value.
     * @return The new variant.
     */
    public static Variant of(java.lang.String value)
    {
        if (value == null)
        {
            return NONE;
        }

        return new String(value);
    }

    /**
     * Create a new integer variant.
     *
     * @param value The integer value.
     * @return The new variant.
     */
    public static Variant of(long value)
    {
        return new Int(value);
    }

    /**
     * Create a new floating-point variant.
     *
     * @param value The floating-point value.
     * @return The new variant.
     */
    public static Variant of(double value)
    {
        return new Float(value);
    }

    /**
     * Check if the variant is empty.
     *
     * @return True if the variant is equal to {@link #NONE}, otherwise false.
     */
    public boolean isNone()
    {
        return false;
    }

    /**
     * Get the string value if this is a string variant.
     */
    public Optional<java.lang.String> getString()
    {
        return Optional.empty();
    }

    /**
     * Get the boolean value if this is a boolean variant.
     */
    public Optional<Boolean> getBoolean()
    {
        return Optional.empty();
    }

    /**
     * Get the integer value as a long if this is an integer variant.
     */
    public Optional<Long> getInteger()
    {
        return Optional.empty();
    }

    /**
     * Get the float value as a double if this is a float variant.
     */
    public Optional<Double> getFloat()
    {
        return Optional.empty();
    }

    // Private to prevent extending with unbounded variant types.
    private Variant()
    {
    }

    /**
     * A variant that represents an empty value.
     */
    public static final Variant NONE = new Variant()
    {
        @Override
        public boolean isNone()
        {
            return true;
        }

        @Override
        public java.lang.String toString()
        {
            return "";
        }
    };

    /**
     * A variant representing a boolean true or false value.
     */
    public static final class Bool extends Variant
    {
        private final boolean value;

        /**
         * The boxed value of {@code true}.
         */
        public static final Bool TRUE = new Bool(true);

        /**
         * The boxed value of {@code false}.
         */
        public static final Bool FALSE = new Bool(false);

        // Private to prevent more than two instances from existing.
        private Bool(boolean value)
        {
            this.value = value;
        }

        @Override
        public Optional<Boolean> getBoolean()
        {
            return Optional.of(value);
        }

        @Override
        public java.lang.String toString()
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
     * A variant containing a string.
     */
    public static final class String extends Variant
    {
        private final java.lang.String value;

        /**
         * Create a new string variant.
         *
         * @param value The string value.
         */
        public String(java.lang.String value)
        {
            this.value = value;
        }

        @Override
        public Optional<java.lang.String> getString()
        {
            return Optional.of(value);
        }

        @Override
        public java.lang.String toString()
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
     * A variant containing an integer.
     */
    public static final class Int extends Variant
    {
        private final long value;

        /**
         * Create a new integer variant.
         *
         * @param value The integer value.
         */
        public Int(long value)
        {
            this.value = value;
        }

        @Override
        public Optional<Long> getInteger()
        {
            return Optional.of(value);
        }

        @Override
        public java.lang.String toString()
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
     * A variant containing a floating-point number. Stored as a double-width float.
     */
    public static final class Float extends Variant
    {
        private final double value;

        /**
         * Create a new float variant.
         *
         * @param value The float value.
         */
        public Float(double value)
        {
            this.value = value;
        }

        @Override
        public Optional<Double> getFloat()
        {
            return Optional.of(value);
        }

        @Override
        public java.lang.String toString()
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
