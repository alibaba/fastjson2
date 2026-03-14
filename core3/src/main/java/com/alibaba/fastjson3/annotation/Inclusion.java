package com.alibaba.fastjson3.annotation;

/**
 * Strategy for controlling which property values are included during serialization.
 * Applied via {@link JSONType#inclusion()} or {@link JSONField#inclusion()}.
 *
 * <p>Resolved at ObjectWriter creation time — zero overhead on the hot path for
 * properties that use the default strategy.</p>
 */
public enum Inclusion {
    /**
     * Default behavior: include all non-null values.
     * Null values are omitted unless {@code WriteFeature.WriteNulls} is enabled.
     */
    DEFAULT,

    /**
     * Include all values including null (equivalent to {@code WriteFeature.WriteNulls}).
     */
    ALWAYS,

    /**
     * Exclude null values (same as DEFAULT).
     */
    NON_NULL,

    /**
     * Exclude null and "empty" values.
     * Empty means: null, empty String, empty Collection, empty Map, empty array.
     */
    NON_EMPTY
}
