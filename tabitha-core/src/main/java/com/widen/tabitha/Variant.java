package com.widen.tabitha;

import java.util.Optional;

/**
 * A boxed type for a typed primitive value.
 * <p>
 * A variant can either be a {@link Bool}, a {@link String}, an {@link Int}, a {@link Float}, or {@link #NONE}.
 */
public abstract class Variant {
    /**
     * Create a new boolean variant.
     *
     * @param value The boolean value.
     * @return The new variant.
     */
    public static Variant of(boolean value) {
        return value ? Bool.TRUE : Bool.FALSE;
    }

    /**
     * Create a new string variant.
     *
     * @param value The string value.
     * @return The new variant.
     */
    public static Variant of(java.lang.String value) {
        if (value == null) {
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
    public static Variant of(long value) {
        return new Int(value);
    }

    /**
     * Create a new floating-point variant.
     *
     * @param value The floating-point value.
     * @return The new variant.
     */
    public static Variant of(double value) {
        return new Float(value);
    }

    /**
     * Attempt to create a variant from a value.
     *
     * @param value The value to create from.
     * @return The new variant.
     * @throws IllegalArgumentException if the object is not representable as a variant.
     */
    public static Variant from(Object value) throws IllegalArgumentException {
        if (value == null) {
            return NONE;
        }
        if (value instanceof Variant) {
            return (Variant) value;
        }
        if (value instanceof Boolean) {
            return of((Boolean) value);
        }
        if (value instanceof java.lang.String) {
            return of((java.lang.String) value);
        }
        if (value instanceof Integer) {
            return of((Integer) value);
        }
        if (value instanceof Long) {
            return of((Long) value);
        }
        if (value instanceof Number) {
            return of(((Number) value).doubleValue());
        }

        throw new IllegalArgumentException();
    }

    /**
     * Check if the variant is empty.
     *
     * @return True if the variant is equal to {@link #NONE}, otherwise false.
     */
    public boolean isNone() {
        return false;
    }

    /**
     * Get the string value if this is a string variant.
     *
     * @return The string value, or empty if this is not a string variant.
     */
    public Optional<java.lang.String> getString() {
        return Optional.empty();
    }

    /**
     * Get the boolean value if this is a boolean variant.
     *
     * @return The boolean value, or empty if this is not a boolean variant.
     */
    public Optional<Boolean> getBoolean() {
        return Optional.empty();
    }

    /**
     * Get the integer value as a long if this is an integer variant.
     *
     * @return The integer value, or empty if this is not an integer variant.
     */
    public Optional<Long> getInteger() {
        return Optional.empty();
    }

    /**
     * Get the float value as a double if this is a float variant.
     *
     * @return The float value, or empty if this is not a float variant.
     */
    public Optional<Double> getFloat() {
        return Optional.empty();
    }

    /**
     * Return a string representation of this variant value.
     *
     * All variant types can be converted to their appropriate string representation using this method. The string
     * equivalent will maintain as much precision as possible and is suitable for writing a variant value to a text-only
     * format, such as a CSV.
     *
     * @return String equivalent of this value.
     */
    public abstract java.lang.String toString();

    // Private to prevent extending with unbounded variant types.
    private Variant() {
    }

    /**
     * A variant that represents an empty value.
     */
    public static final Variant NONE = new Variant() {
        @Override
        public boolean isNone() {
            return true;
        }

        @Override
        public java.lang.String toString() {
            return "";
        }
    };

    /**
     * A variant representing a boolean true or false value.
     */
    public static final class Bool extends Variant {
        /**
         * The boxed value of {@code true}.
         */
        public static final Bool TRUE = new Bool();

        /**
         * The boxed value of {@code false}.
         */
        public static final Bool FALSE = new Bool();

        @Override
        public Optional<Boolean> getBoolean() {
            return Optional.of(this == TRUE);
        }

        @Override
        public java.lang.String toString() {
            return Boolean.toString(this == TRUE);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            if (other instanceof Boolean) {
                return ((Boolean) other) ? this == TRUE : this == FALSE;
            }

            return false;
        }

        private Bool() {
        }
    }

    /**
     * A variant containing a string.
     */
    public static final class String extends Variant {
        private final java.lang.String value;

        /**
         * Create a new string variant.
         *
         * @param value The string value.
         */
        public String(java.lang.String value) {
            this.value = value;
        }

        @Override
        public Optional<java.lang.String> getString() {
            return Optional.of(value);
        }

        @Override
        public java.lang.String toString() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            if (value.equals(other)) {
                return true;
            }

            if (other instanceof String) {
                return value.equals(((String) other).value);
            }

            return false;
        }
    }

    /**
     * A variant containing an integer.
     */
    public static final class Int extends Variant {
        private final long value;

        /**
         * Create a new integer variant.
         *
         * @param value The integer value.
         */
        public Int(long value) {
            this.value = value;
        }

        @Override
        public Optional<Long> getInteger() {
            return Optional.of(value);
        }

        @Override
        public java.lang.String toString() {
            return Long.toString(value);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            if (other instanceof Number) {
                return value == ((Number) other).longValue();
            }

            if (other instanceof Int) {
                return value == ((Int) other).value;
            }

            return false;
        }
    }

    /**
     * A variant containing a floating-point number. Stored as a double-width float.
     */
    public static final class Float extends Variant {
        private final double value;

        /**
         * Create a new float variant.
         *
         * @param value The float value.
         */
        public Float(double value) {
            this.value = value;
        }

        @Override
        public Optional<Double> getFloat() {
            return Optional.of(value);
        }

        @Override
        public java.lang.String toString() {
            return Double.toString(value);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            if (other instanceof Number) {
                return value == ((Number) other).doubleValue();
            }

            if (other instanceof Float) {
                return value == ((Float) other).value;
            }

            return false;
        }
    }
}
