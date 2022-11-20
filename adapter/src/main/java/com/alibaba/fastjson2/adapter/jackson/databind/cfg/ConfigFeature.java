package com.alibaba.fastjson2.adapter.jackson.databind.cfg;

public interface ConfigFeature {
    /**
     * Accessor for checking whether this feature is enabled by default.
     */
    boolean enabledByDefault();

    /**
     * Returns bit mask for this feature instance
     */
    int getMask();

    /**
     * Convenience method for checking whether feature is enabled in given bitmask
     *
     * @since 2.6
     */
    boolean enabledIn(int flags);
}
