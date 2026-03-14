package com.alibaba.fastjson3;

/**
 * Features controlling JSON deserialization behavior.
 * Each feature is a bit in a long bitmask for O(1) checking.
 */
public enum ReadFeature {
    /**
     * Use field-based access instead of getter/setter
     */
    FieldBased,

    /**
     * Allow single quotes in JSON strings
     */
    AllowSingleQuotes,

    /**
     * Allow unquoted field names
     */
    AllowUnquotedFieldNames,

    /**
     * Allow JSON comments (// and /* *​/)
     */
    AllowComments,

    /**
     * Use BigDecimal for floating point numbers
     */
    UseBigDecimalForFloats,

    /**
     * Use BigDecimal for double values
     */
    UseBigDecimalForDoubles,

    /**
     * Trim whitespace from string values
     */
    TrimString,

    /**
     * Throw on unknown properties during deserialization
     */
    ErrorOnUnknownProperties,

    /**
     * Throw when null value for primitive type
     */
    ErrorOnNullForPrimitives,

    /**
     * Support smart match (case-insensitive, underscore-insensitive)
     */
    SupportSmartMatch,

    /**
     * Support auto type detection for polymorphism
     */
    SupportAutoType,

    /**
     * Initialize string fields as empty string instead of null
     */
    InitStringFieldAsEmpty,

    /**
     * Return null on parsing error instead of throwing
     */
    NullOnError,

    /**
     * Support mapping JSON array to Java bean
     */
    SupportArrayToBean,

    /**
     * Treat empty string as null
     */
    EmptyStringAsNull,

    /**
     * Treat duplicate keys as array values
     */
    DuplicateKeyValueAsArray,

    /**
     * Base64 encoded string as byte array
     */
    Base64StringAsByteArray;

    public final long mask;

    ReadFeature() {
        this.mask = 1L << ordinal();
    }

    public static long of(ReadFeature... features) {
        long flags = 0;
        for (ReadFeature f : features) {
            flags |= f.mask;
        }
        return flags;
    }
}
