package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.ExtraProcessor;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.internal.Conf;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Type;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * JSONFactory is the core factory class for creating JSON readers and writers,
 * as well as managing global configuration for fastjson2.
 *
 * @author wenshao
 * @since 2.0.59
 */
public final class JSONFactory {
    static volatile Throwable initErrorLast;

    public static final String CREATOR;

    public static final String PROPERTY_DENY_PROPERTY = "fastjson2.parser.deny";
    public static final String PROPERTY_AUTO_TYPE_ACCEPT = "fastjson2.autoTypeAccept";
    public static final String PROPERTY_AUTO_TYPE_HANDLER = "fastjson2.autoTypeHandler";
    public static final String PROPERTY_AUTO_TYPE_BEFORE_HANDLER = "fastjson2.autoTypeBeforeHandler";

    static boolean useJacksonAnnotation;
    static boolean useGsonAnnotation;

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
    static final boolean disableAutoType;
    static final boolean disableSmartMatch;

    static Supplier<Map> defaultObjectSupplier;
    static Supplier<List> defaultArraySupplier;

    static final NameCacheEntry[] NAME_CACHE = new NameCacheEntry[8192];
    static final NameCacheEntry2[] NAME_CACHE2 = new NameCacheEntry2[8192];

    static int defaultDecimalMaxScale = 2048;
    static int defaultMaxLevel;

    interface JSONReaderUTF8Creator {
        JSONReader create(JSONReader.Context ctx, String str, byte[] bytes, int offset, int length);
    }

    interface JSONReaderUTF16Creator {
        JSONReader create(JSONReader.Context ctx, String str, char[] chars, int offset, int length);
    }

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

    static {
        {
            String property = System.getProperty("fastjson2.creator");
            if (property != null) {
                property = property.trim();
            }

            if (property == null || property.isEmpty()) {
                property = Conf.getProperty("fastjson2.creator");
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
                    disableAutoType0 = false,
                    disableSmartMatch0 = false;
            String features = System.getProperty("fastjson2.features");
            if (features == null) {
                features = Conf.getProperty("fastjson2.features");
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
                        case "disableAutoType":
                            disableAutoType0 = true;
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
            disableAutoType = disableAutoType0;
            disableSmartMatch = disableSmartMatch0;
        }

        useJacksonAnnotation = Conf.getPropertyBool("fastjson2.useJacksonAnnotation", true);
        useGsonAnnotation = Conf.getPropertyBool("fastjson2.useGsonAnnotation", true);
        defaultWriterAlphabetic = Conf.getPropertyBool("fastjson2.writer.alphabetic", true);
        defaultSkipTransient = Conf.getPropertyBool("fastjson2.writer.skipTransient", true);
        defaultMaxLevel = Conf.getPropertyInt("fastjson2.writer.maxLevel", 2048);
    }

    public static boolean isUseJacksonAnnotation() {
        return useJacksonAnnotation;
    }

    public static boolean isUseGsonAnnotation() {
        return useGsonAnnotation;
    }

    public static void setUseJacksonAnnotation(boolean useJacksonAnnotation) {
        JSONFactory.useJacksonAnnotation = useJacksonAnnotation;
    }

    public static void setUseGsonAnnotation(boolean useGsonAnnotation) {
        JSONFactory.useGsonAnnotation = useGsonAnnotation;
    }

    private static volatile boolean jsonFieldDefaultValueCompatMode;

    public static boolean isJSONFieldDefaultValueCompatMode() {
        return jsonFieldDefaultValueCompatMode;
    }

    public static void setJSONFieldDefaultValueCompatMode(boolean compatMode) {
        jsonFieldDefaultValueCompatMode = compatMode;
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

    static final CacheItem[] CACHE_ITEMS;

    static {
        final CacheItem[] items = new CacheItem[16];
        for (int i = 0; i < items.length; i++) {
            items[i] = new CacheItem();
        }
        CACHE_ITEMS = items;
    }

    static final int CACHE_THRESHOLD = 1024 * 1024 * 8;
    static final AtomicReferenceFieldUpdater<CacheItem, char[]> CHARS_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(CacheItem.class, char[].class, "chars");
    static final AtomicReferenceFieldUpdater<CacheItem, byte[]> BYTES_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(CacheItem.class, byte[].class, "bytes");

    static final class CacheItem {
        volatile char[] chars;
        volatile byte[] bytes;
    }

    static final ObjectWriterProvider defaultObjectWriterProvider = new ObjectWriterProvider();
    static final ObjectReaderProvider defaultObjectReaderProvider = new ObjectReaderProvider();

    static final JSONPathCompiler defaultJSONPathCompiler;

    static {
        JSONPathCompilerReflect compiler = null;
        switch (JSONFactory.CREATOR) {
            case "reflect":
            case "lambda":
                compiler = JSONPathCompilerReflect.INSTANCE;
                break;
            default:
                try {
                    if (!JDKUtils.ANDROID && !JDKUtils.GRAAL) {
                        compiler = JSONPathCompilerReflectASM.INSTANCE;
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
                if (compiler == null) {
                    compiler = JSONPathCompilerReflect.INSTANCE;
                }
                break;
        }
        defaultJSONPathCompiler = compiler;
    }

    static final ThreadLocal<ObjectReaderCreator> readerCreatorLocal = new ThreadLocal<>();
    static final ThreadLocal<ObjectReaderProvider> readerProviderLocal = new ThreadLocal<>();
    static final ThreadLocal<ObjectWriterCreator> writerCreatorLocal = new ThreadLocal<>();

    static final ThreadLocal<JSONPathCompiler> jsonPathCompilerLocal = new ThreadLocal<>();

    static final ObjectReader<JSONArray> ARRAY_READER = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(JSONArray.class);
    static final ObjectReader<JSONObject> OBJECT_READER = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(JSONObject.class);

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
     * @since 2.0.15
     */
    public static void setDefaultObjectSupplier(Supplier<Map> objectSupplier) {
        defaultObjectSupplier = objectSupplier;
    }

    /**
     * Sets the default array supplier used when creating JSON arrays.
     *
     * @param arraySupplier the supplier for creating List instances
     * @since 2.0.15
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
        return new JSONWriter.Context(defaultObjectWriterProvider);
    }

    /**
     * Creates a new JSON writer context with the specified provider and features.
     *
     * @param provider the object writer provider
     * @param features the features to enable
     * @return a new JSONWriter.Context instance
     */
    public static JSONWriter.Context createWriteContext(ObjectWriterProvider provider, JSONWriter.Feature... features) {
        JSONWriter.Context context = new JSONWriter.Context(provider);
        context.config(features);
        return context;
    }

    /**
     * Creates a new JSON writer context with the specified features.
     *
     * @param features the features to enable
     * @return a new JSONWriter.Context instance
     */
    public static JSONWriter.Context createWriteContext(JSONWriter.Feature... features) {
        return new JSONWriter.Context(defaultObjectWriterProvider, features);
    }

    /**
     * Creates a new JSON reader context with default settings.
     *
     * @return a new JSONReader.Context instance
     */
    public static JSONReader.Context createReadContext() {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        return new JSONReader.Context(provider);
    }

    /**
     * Creates a new JSON reader context with the specified features.
     *
     * @param features the features to enable
     * @return a new JSONReader.Context instance
     */
    public static JSONReader.Context createReadContext(long features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        return new JSONReader.Context(provider, features);
    }

    /**
     * Creates a new JSON reader context with the specified features.
     *
     * @param features the features to enable
     * @return a new JSONReader.Context instance
     */
    public static JSONReader.Context createReadContext(JSONReader.Feature... features) {
        JSONReader.Context context = new JSONReader.Context(
                JSONFactory.getDefaultObjectReaderProvider()
        );
        for (int i = 0; i < features.length; i++) {
            context.features |= features[i].mask;
        }
        return context;
    }

    public static JSONReader.Context createReadContext(Filter filter, JSONReader.Feature... features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider);

        if (filter instanceof JSONReader.AutoTypeBeforeHandler) {
            context.autoTypeBeforeHandler = (JSONReader.AutoTypeBeforeHandler) filter;
        }

        if (filter instanceof ExtraProcessor) {
            context.extraProcessor = (ExtraProcessor) filter;
        }

        for (int i = 0; i < features.length; i++) {
            context.features |= features[i].mask;
        }
        return context;
    }

    public static JSONReader.Context createReadContext(ObjectReaderProvider provider, JSONReader.Feature... features) {
        if (provider == null) {
            provider = getDefaultObjectReaderProvider();
        }

        JSONReader.Context context = new JSONReader.Context(provider);
        context.config(features);
        return context;
    }

    public static JSONReader.Context createReadContext(SymbolTable symbolTable) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        return new JSONReader.Context(provider, symbolTable);
    }

    public static JSONReader.Context createReadContext(SymbolTable symbolTable, JSONReader.Feature... features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider, symbolTable);
        context.config(features);
        return context;
    }

    public static JSONReader.Context createReadContext(Supplier<Map> objectSupplier, JSONReader.Feature... features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider);
        context.setObjectSupplier(objectSupplier);
        context.config(features);
        return context;
    }

    public static JSONReader.Context createReadContext(
            Supplier<Map> objectSupplier,
            Supplier<List> arraySupplier,
            JSONReader.Feature... features
    ) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider);
        context.setObjectSupplier(objectSupplier);
        context.setArraySupplier(arraySupplier);
        context.config(features);
        return context;
    }

    public static ObjectReader getObjectReader(Type type, long features) {
        return getDefaultObjectReaderProvider()
                .getObjectReader(type, JSONReader.Feature.FieldBased.isEnabled(features));
    }

    public static ObjectWriter getObjectWriter(Type type, long features) {
        return getDefaultObjectWriterProvider()
                .getObjectWriter(type, TypeUtils.getClass(type), JSONWriter.Feature.FieldBased.isEnabled(features));
    }

    /**
     * Gets the default object writer provider.
     *
     * @return the default ObjectWriterProvider instance
     */
    public static ObjectWriterProvider getDefaultObjectWriterProvider() {
        return defaultObjectWriterProvider;
    }

    /**
     * Gets the default object reader provider.
     *
     * @return the default ObjectReaderProvider instance
     */
    public static ObjectReaderProvider getDefaultObjectReaderProvider() {
        ObjectReaderProvider providerLocal = readerProviderLocal.get();
        if (providerLocal != null) {
            return providerLocal;
        }

        return defaultObjectReaderProvider;
    }

    /**
     * Gets the default JSONPath compiler.
     *
     * @return the default JSONPathCompiler instance
     */
    public static JSONPathCompiler getDefaultJSONPathCompiler() {
        JSONPathCompiler compilerLocal = jsonPathCompilerLocal.get();
        if (compilerLocal != null) {
            return compilerLocal;
        }

        return defaultJSONPathCompiler;
    }

    /**
     * Sets the object reader creator for the current thread context.
     *
     * @param creator the ObjectReaderCreator to set
     */
    public static void setContextReaderCreator(ObjectReaderCreator creator) {
        readerCreatorLocal.set(creator);
    }

    /**
     * Sets the object reader provider for the current thread context.
     *
     * @param creator the ObjectReaderProvider to set
     */
    public static void setContextObjectReaderProvider(ObjectReaderProvider creator) {
        readerProviderLocal.set(creator);
    }

    /**
     * Gets the object reader creator for the current thread context.
     *
     * @return the ObjectReaderCreator for the current thread, or null if not set
     */
    public static ObjectReaderCreator getContextReaderCreator() {
        return readerCreatorLocal.get();
    }

    /**
     * Sets the JSONPath compiler for the current thread context.
     *
     * @param compiler the JSONPathCompiler to set
     */
    public static void setContextJSONPathCompiler(JSONPathCompiler compiler) {
        jsonPathCompilerLocal.set(compiler);
    }

    /**
     * Sets the object writer creator for the current thread context.
     *
     * @param creator the ObjectWriterCreator to set
     */
    public static void setContextWriterCreator(ObjectWriterCreator creator) {
        writerCreatorLocal.set(creator);
    }

    /**
     * Gets the object writer creator for the current thread context.
     *
     * @return the ObjectWriterCreator for the current thread, or null if not set
     */
    public static ObjectWriterCreator getContextWriterCreator() {
        return writerCreatorLocal.get();
    }

    public interface JSONPathCompiler {
        JSONPath compile(Class objectClass, JSONPath path);
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
        defaultObjectWriterProvider.setAlphabetic(defaultWriterAlphabetic);
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
     * Checks if auto type support is disabled.
     *
     * @return true if auto type is disabled, false otherwise
     */
    public static boolean isDisableAutoType() {
        return disableAutoType;
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
     * Sets whether reference detection should be disabled.
     *
     * @param disableReferenceDetect true to disable reference detection, false to enable
     */
    public static void setDisableReferenceDetect(boolean disableReferenceDetect) {
        defaultObjectWriterProvider.setDisableReferenceDetect(disableReferenceDetect);
        defaultObjectReaderProvider.setDisableReferenceDetect(disableReferenceDetect);
    }

    /**
     * Sets whether array mapping should be disabled.
     *
     * @param disableArrayMapping true to disable array mapping, false to enable
     */
    public static void setDisableArrayMapping(boolean disableArrayMapping) {
        defaultObjectWriterProvider.setDisableArrayMapping(disableArrayMapping);
        defaultObjectReaderProvider.setDisableArrayMapping(disableArrayMapping);
    }

    /**
     * Sets whether JSONB format should be disabled.
     *
     * @param disableJSONB true to disable JSONB, false to enable
     */
    public static void setDisableJSONB(boolean disableJSONB) {
        defaultObjectWriterProvider.setDisableJSONB(disableJSONB);
        defaultObjectReaderProvider.setDisableJSONB(disableJSONB);
    }

    /**
     * Sets whether auto type support should be disabled.
     *
     * @param disableAutoType true to disable auto type, false to enable
     */
    public static void setDisableAutoType(boolean disableAutoType) {
        defaultObjectWriterProvider.setDisableAutoType(disableAutoType);
        defaultObjectReaderProvider.setDisableAutoType(disableAutoType);
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
     * Sets whether smart matching should be disabled.
     *
     * @param disableSmartMatch true to disable smart matching, false to enable
     */
    public static void setDisableSmartMatch(boolean disableSmartMatch) {
        defaultObjectReaderProvider.setDisableSmartMatch(disableSmartMatch);
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
        defaultObjectWriterProvider.setSkipTransient(skipTransient);
    }
}
