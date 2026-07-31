package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;

import java.io.InputStream;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Supplier;

/**
 * JSONFactory is the core factory class for creating JSON readers and writers,
 * as well as managing global configuration for fastjson2.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class JSONFactory {
    public static final class Conf {
        static final Properties DEFAULT_PROPERTIES;

        static {
            Properties properties = new Properties();

            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            final String resourceFile = "fastjson2.properties";

            InputStream inputStream = cl != null
                    ? cl.getResourceAsStream(resourceFile)
                    : ClassLoader.getSystemResourceAsStream(resourceFile);
            if (inputStream != null) {
                try {
                    properties.load(inputStream);
                } catch (java.io.IOException ignored) {
                } finally {
                    IOUtils.close(inputStream);
                }
            }
            DEFAULT_PROPERTIES = properties;
        }

        public static String getProperty(String key) {
            return DEFAULT_PROPERTIES.getProperty(key);
        }
    }

    public static final String CREATOR;

    public static String getProperty(String key) {
        return Conf.getProperty(key);
    }

    static long defaultReaderFeatures;
    static String defaultReaderFormat;
    static ZoneId defaultReaderZoneId;

    static long defaultWriterFeatures;
    static String defaultWriterFormat;
    static ZoneId defaultWriterZoneId;
    static boolean defaultWriterAlphabetic;
    static boolean defaultSkipTransient;
    static final boolean disableReferenceDetect;
    static final boolean disableArrayMapping;
    static final boolean disableJSONB;
    static final boolean disableSmartMatch;

    static Supplier<Map> defaultObjectSupplier;
    static Supplier<List> defaultArraySupplier;

    static int defaultDecimalMaxScale = 2048;
    static int defaultMaxLevel;

    static final char[] CA = new char[]{
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '+', '/'
    };

    static final int[] DIGITS2 = new int[]{
            +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
            +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
            +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
            +0, +1, +2, +3, +4, +5, +6, +7, +8, +9, +0, +0, +0, +0, +0, +0,
            +0, 10, 11, 12, 13, 14, 15, +0, +0, +0, +0, +0, +0, +0, +0, +0,
            +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
            +0, 10, 11, 12, 13, 14, 15
    };

    static final CacheItem[] CACHE_ITEMS;

    static final NameCacheEntry[] NAME_CACHE = new NameCacheEntry[8192];
    static final NameCacheEntry2[] NAME_CACHE2 = new NameCacheEntry2[8192];

    static final float[] FLOAT_10_POW = {
            1.0e0f, 1.0e1f, 1.0e2f, 1.0e3f, 1.0e4f, 1.0e5f,
            1.0e6f, 1.0e7f, 1.0e8f, 1.0e9f, 1.0e10f
    };

    static final double[] DOUBLE_10_POW = {
            1.0e0, 1.0e1, 1.0e2, 1.0e3, 1.0e4,
            1.0e5, 1.0e6, 1.0e7, 1.0e8, 1.0e9,
            1.0e10, 1.0e11, 1.0e12, 1.0e13, 1.0e14,
            1.0e15, 1.0e16, 1.0e17, 1.0e18, 1.0e19,
            1.0e20, 1.0e21, 1.0e22
    };

    static final class NameCacheEntry {
        final String name;
        final long value;

        public NameCacheEntry(String name, long value) {
            this.name = name;
            this.value = value;
        }
    }

    static final class NameCacheEntry2 {
        final String name;
        final long value0;
        final long value1;

        public NameCacheEntry2(String name, long value0, long value1) {
            this.name = name;
            this.value0 = value0;
            this.value1 = value1;
        }
    }

    static {
        final CacheItem[] items = new CacheItem[16];
        for (int i = 0; i < items.length; i++) {
            items[i] = new CacheItem();
        }
        CACHE_ITEMS = items;
    }

    static final int CACHE_THRESHOLD = 1024 * 1024 * 8;
    static final java.util.concurrent.atomic.AtomicReferenceFieldUpdater<CacheItem, char[]> CHARS_UPDATER
            = java.util.concurrent.atomic.AtomicReferenceFieldUpdater.newUpdater(CacheItem.class, char[].class, "chars");
    static final java.util.concurrent.atomic.AtomicReferenceFieldUpdater<CacheItem, byte[]> BYTES_UPDATER
            = java.util.concurrent.atomic.AtomicReferenceFieldUpdater.newUpdater(CacheItem.class, byte[].class, "bytes");

    static final class CacheItem {
        volatile char[] chars;
        volatile byte[] bytes;
    }

    interface JSONReaderUTF8Creator {
        JSONReader create(JSONReader.Context ctx, String str, byte[] bytes, int offset, int length);
    }

    interface JSONReaderUTF16Creator {
        JSONReader create(JSONReader.Context ctx, String str, char[] chars, int offset, int length);
    }

    static {
        Properties properties = Conf.DEFAULT_PROPERTIES;
        {
            String property = System.getProperty("fastjson2.creator");
            if (property != null) {
                property = property.trim();
            }

            if (property == null || property.isEmpty()) {
                property = properties.getProperty("fastjson2.creator");
                if (property != null) {
                    property = property.trim();
                }
            }

            CREATOR = property == null ? "asm" : property;
        }
        {
            boolean disableReferenceDetect0 = false,
                    disableArrayMapping0 = false,
                    disableJSONB0 = false,
                    disableSmartMatch0 = false;
            String features = System.getProperty("fastjson2.features");
            if (features == null) {
                features = getProperty("fastjson2.features");
            }
            if (features != null) {
                for (String feature : features.split(",")) {
                    switch (feature) {
                        case "disableReferenceDetect":
                            disableReferenceDetect0 = true;
                            break;
                        case "disableArrayMapping":
                            disableArrayMapping0 = true;
                            break;
                        case "disableJSONB":
                            disableJSONB0 = true;
                            break;
                        case "disableSmartMatch":
                            disableSmartMatch0 = true;
                            break;
                        default:
                            break;
                    }
                }
            }

            disableReferenceDetect = disableReferenceDetect0;
            disableArrayMapping = disableArrayMapping0;
            disableJSONB = disableJSONB0;
            disableSmartMatch = disableSmartMatch0;
        }

        defaultWriterAlphabetic = getPropertyBool(properties, "fastjson2.writer.alphabetic", true);
        defaultSkipTransient = getPropertyBool(properties, "fastjson2.writer.skipTransient", true);
        defaultMaxLevel = getPropertyInt(properties, "fastjson2.writer.maxLevel", 2048);
    }

    private static boolean getPropertyBool(Properties properties, String name, boolean defaultValue) {
        boolean propertyValue = defaultValue;

        String property = System.getProperty(name);
        if (property != null) {
            property = property.trim();
            if (property.isEmpty()) {
                property = properties.getProperty(name);
                if (property != null) {
                    property = property.trim();
                }
            }
            if (defaultValue) {
                if ("false".equals(property)) {
                    propertyValue = false;
                }
            } else {
                if ("true".equals(property)) {
                    propertyValue = true;
                }
            }
        }

        return propertyValue;
    }

    private static int getPropertyInt(Properties properties, String name, int defaultValue) {
        int propertyValue = defaultValue;

        String property = System.getProperty(name);
        if (property != null) {
            property = property.trim();
            if (property.isEmpty()) {
                property = properties.getProperty(name);
                if (property != null) {
                    property = property.trim();
                }
            }
        }
        try {
            propertyValue = Integer.parseInt(property);
        } catch (NumberFormatException ignored) {
            // ignore
        }

        return propertyValue;
    }

    public static int getDefaultMaxLevel() {
        return defaultMaxLevel;
    }

    public static void setDefaultMaxLevel(int maxLevel) {
        if (maxLevel <= 0) {
            throw new IllegalArgumentException("maxLevel must be positive, maxLevel " + maxLevel);
        }
        JSONFactory.defaultMaxLevel = maxLevel;
    }

    static final byte[] NIBBLES;

    static {
        byte[] ns = new byte[256];
        Arrays.fill(ns, (byte) -1);
        ns['0'] = 0;
        ns['1'] = 1;
        ns['2'] = 2;
        ns['3'] = 3;
        ns['4'] = 4;
        ns['5'] = 5;
        ns['6'] = 6;
        ns['7'] = 7;
        ns['8'] = 8;
        ns['9'] = 9;
        ns['A'] = 10;
        ns['B'] = 11;
        ns['C'] = 12;
        ns['D'] = 13;
        ns['E'] = 14;
        ns['F'] = 15;
        ns['a'] = 10;
        ns['b'] = 11;
        ns['c'] = 12;
        ns['d'] = 13;
        ns['e'] = 14;
        ns['f'] = 15;
        NIBBLES = ns;
    }

    /**
     * Sets the default object supplier used when creating JSON objects.
     *
     * @param objectSupplier the supplier for creating Map instances
     */
    public static void setDefaultObjectSupplier(Supplier<Map> objectSupplier) {
        defaultObjectSupplier = objectSupplier;
    }

    /**
     * Sets the default array supplier used when creating JSON arrays.
     *
     * @param arraySupplier the supplier for creating List instances
     */
    public static void setDefaultArraySupplier(Supplier<List> arraySupplier) {
        defaultArraySupplier = arraySupplier;
    }

    /**
     * Gets the default object supplier used when creating JSON objects.
     *
     * @return the supplier for creating Map instances
     */
    public static Supplier<Map> getDefaultObjectSupplier() {
        return defaultObjectSupplier;
    }

    /**
     * Gets the default array supplier used when creating JSON arrays.
     *
     * @return the supplier for creating List instances
     */
    public static Supplier<List> getDefaultArraySupplier() {
        return defaultArraySupplier;
    }

    /**
     * Creates a new JSON writer context with default settings.
     *
     * @return a new JSONWriter.Context instance
     */
    public static JSONWriter.Context createWriteContext() {
        return new JSONWriter.Context();
    }

    /**
     * Creates a new JSON writer context with the specified features.
     *
     * @param features the features to enable
     * @return a new JSONWriter.Context instance
     */
    public static JSONWriter.Context createWriteContext(JSONWriter.Feature... features) {
        return new JSONWriter.Context(features);
    }

    /**
     * Creates a new JSON reader context with default settings.
     *
     * @return a new JSONReader.Context instance
     */
    public static JSONReader.Context createReadContext() {
        return new JSONReader.Context();
    }

    /**
     * Creates a new JSON reader context with the specified features.
     *
     * @param features the features to enable
     * @return a new JSONReader.Context instance
     */
    public static JSONReader.Context createReadContext(long features) {
        JSONReader.Context context = new JSONReader.Context();
        context.features = features;
        return context;
    }

    /**
     * Creates a new JSON reader context with the specified features.
     *
     * @param features the features to enable
     * @return a new JSONReader.Context instance
     */
    public static JSONReader.Context createReadContext(JSONReader.Feature... features) {
        return new JSONReader.Context(features);
    }

    public static JSONReader.Context createReadContext(Supplier<Map> objectSupplier, JSONReader.Feature... features) {
        JSONReader.Context context = new JSONReader.Context(features);
        context.setObjectSupplier(objectSupplier);
        return context;
    }

    public static JSONReader.Context createReadContext(
            Supplier<Map> objectSupplier,
            Supplier<List> arraySupplier,
            JSONReader.Feature... features
    ) {
        JSONReader.Context context = new JSONReader.Context(features);
        context.setObjectSupplier(objectSupplier);
        context.setArraySupplier(arraySupplier);
        return context;
    }

    /**
     * Gets the default reader features.
     *
     * @return the default reader features as a long value
     */
    public static long getDefaultReaderFeatures() {
        return defaultReaderFeatures;
    }

    /**
     * Gets the default reader zone ID.
     *
     * @return the default ZoneId for readers
     */
    public static ZoneId getDefaultReaderZoneId() {
        return defaultReaderZoneId;
    }

    /**
     * Gets the default reader format string.
     *
     * @return the default format string for readers
     */
    public static String getDefaultReaderFormat() {
        return defaultReaderFormat;
    }

    /**
     * Gets the default writer features.
     *
     * @return the default writer features as a long value
     */
    public static long getDefaultWriterFeatures() {
        return defaultWriterFeatures;
    }

    /**
     * Gets the default writer zone ID.
     *
     * @return the default ZoneId for writers
     */
    public static ZoneId getDefaultWriterZoneId() {
        return defaultWriterZoneId;
    }

    /**
     * Gets the default writer format string.
     *
     * @return the default format string for writers
     */
    public static String getDefaultWriterFormat() {
        return defaultWriterFormat;
    }

    /**
     * Checks if the default writer uses alphabetic ordering.
     *
     * @return true if alphabetic ordering is enabled, false otherwise
     */
    public static boolean isDefaultWriterAlphabetic() {
        return defaultWriterAlphabetic;
    }

    /**
     * Sets whether the default writer should use alphabetic ordering.
     *
     * @param defaultWriterAlphabetic true to enable alphabetic ordering, false to disable
     */
    public static void setDefaultWriterAlphabetic(boolean defaultWriterAlphabetic) {
        JSONFactory.defaultWriterAlphabetic = defaultWriterAlphabetic;
    }

    /**
     * Checks if reference detection is disabled.
     *
     * @return true if reference detection is disabled, false otherwise
     */
    public static boolean isDisableReferenceDetect() {
        return disableReferenceDetect;
    }

    /**
     * Checks if JSONB format is disabled.
     *
     * @return true if JSONB is disabled, false otherwise
     */
    public static boolean isDisableJSONB() {
        return disableJSONB;
    }

    /**
     * Checks if array mapping is disabled.
     *
     * @return true if array mapping is disabled, false otherwise
     */
    public static boolean isDisableArrayMapping() {
        return disableArrayMapping;
    }

    /**
     * Checks if smart matching is disabled.
     *
     * @return true if smart matching is disabled, false otherwise
     */
    public static boolean isDisableSmartMatch() {
        return disableSmartMatch;
    }

    /**
     * Checks if transient fields are skipped by default.
     *
     * @return true if transient fields are skipped, false otherwise
     */
    public static boolean isDefaultSkipTransient() {
        return defaultSkipTransient;
    }

    /**
     * Sets whether transient fields should be skipped by default.
     *
     * @param skipTransient true to skip transient fields, false to include them
     */
    public static void setDefaultSkipTransient(boolean skipTransient) {
        JSONFactory.defaultSkipTransient = skipTransient;
    }
}
