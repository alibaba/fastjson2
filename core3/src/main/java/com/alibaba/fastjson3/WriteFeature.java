package com.alibaba.fastjson3;

/**
 * Features controlling JSON serialization behavior.
 * Each feature is a bit in a long bitmask for O(1) checking.
 */
public enum WriteFeature {
    /**
     * Use field-based access instead of getter/setter
     */
    FieldBased,

    /**
     * Pretty print JSON output
     */
    PrettyFormat,

    /**
     * Write null fields (by default nulls are omitted)
     */
    WriteNulls,

    /**
     * Write null list as empty array []
     */
    WriteNullListAsEmpty,

    /**
     * Write null string as empty string ""
     */
    WriteNullStringAsEmpty,

    /**
     * Write null number as 0
     */
    WriteNullNumberAsZero,

    /**
     * Write null boolean as false
     */
    WriteNullBooleanAsFalse,

    /**
     * Write enum using name() instead of ordinal
     */
    WriteEnumsUsingName,

    /**
     * Write enum using toString()
     */
    WriteEnumUsingToString,

    /**
     * Write class name for polymorphic types
     */
    WriteClassName,

    /**
     * Write Map keys sorted
     */
    SortMapEntriesByKeys,

    /**
     * Escape non-ASCII characters
     */
    EscapeNoneAscii,

    /**
     * Write BigDecimal as plain string (no scientific notation)
     */
    WriteBigDecimalAsPlain,

    /**
     * Write long as string (for JavaScript compatibility)
     */
    WriteLongAsString,

    /**
     * Write byte[] as Base64 encoded string
     */
    WriteByteArrayAsBase64,

    /**
     * Write bean as array (ordered fields)
     */
    BeanToArray,

    /**
     * Detect circular references
     */
    ReferenceDetection,

    /**
     * Browser compatible mode (escape special chars)
     */
    BrowserCompatible,

    /**
     * Write non-string values as string
     */
    WriteNonStringValueAsString,

    /**
     * Optimize output for ASCII content
     */
    OptimizedForAscii;

    public final long mask;

    WriteFeature() {
        this.mask = 1L << ordinal();
    }

    public static long of(WriteFeature... features) {
        long flags = 0;
        for (WriteFeature f : features) {
            flags |= f.mask;
        }
        return flags;
    }
}
