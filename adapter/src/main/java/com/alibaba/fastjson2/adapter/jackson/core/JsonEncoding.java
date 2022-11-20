/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.alibaba.fastjson2.adapter.jackson.core;

public enum JsonEncoding {
    UTF8("UTF-8", false, 8), // N/A for big-endian, really
    UTF16_BE("UTF-16BE", true, 16),
    UTF16_LE("UTF-16LE", false, 16),
    UTF32_BE("UTF-32BE", true, 32),
    UTF32_LE("UTF-32LE", false, 32);

    private final String javaName;
    private final boolean bigEndian;
    private final int bits;

    JsonEncoding(String javaName, boolean bigEndian, int bits) {
        this.javaName = javaName;
        this.bigEndian = bigEndian;
        this.bits = bits;
    }

    /**
     * Method for accessing encoding name that JDK will support.
     *
     * @return Matching encoding name that JDK will support.
     */
    public String getJavaName() {
        return javaName;
    }

    /**
     * Whether encoding is big-endian (if encoding supports such
     * notion). If no such distinction is made (as is the case for
     * {@link #UTF8}), return value is undefined.
     *
     * @return True for big-endian encodings; false for little-endian
     * (or if not applicable)
     */
    public boolean isBigEndian() {
        return bigEndian;
    }

    public int bits() {
        return bits;
    }
}
