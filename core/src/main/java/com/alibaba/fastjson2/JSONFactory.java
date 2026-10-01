package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.ExtraProcessor;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.introspect.PropertyAccessorFactory;
import com.alibaba.fastjson2.introspect.PropertyAccessorFactoryUnsafe;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.InputStream;
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

        /**
         * Returns a property loaded from the classpath {@code fastjson2.properties} resource.
         * <details><summary>中文</summary>返回从类路径 fastjson2.properties 加载的属性。</details>
         *
         * @param key the property name
         * @return the configured value, or null if absent
         */
        public static String getProperty(String key) {
            return DEFAULT_PROPERTIES.getProperty(key);
        }
    }
    static volatile Throwable initErrorLast;

    public static final String CREATOR;

    public static final String PROPERTY_DENY_PROPERTY = "fastjson2.parser.deny";
    public static final String PROPERTY_AUTO_TYPE_ACCEPT = "fastjson2.autoTypeAccept";
    public static final String PROPERTY_AUTO_TYPE_HANDLER = "fastjson2.autoTypeHandler";
    public static final String PROPERTY_AUTO_TYPE_BEFORE_HANDLER = "fastjson2.autoTypeBeforeHandler";

    static boolean useJacksonAnnotation;
    static boolean useGsonAnnotation;

    /**
     * Looks up a value loaded from the classpath {@code fastjson2.properties} resource.
     * This method does not consult JVM system properties.
     * <details><summary>中文</summary>查询类路径 fastjson2.properties 中加载的配置，不读取 JVM 系统属性。</details>
     *
     * @param key the property name
     * @return the configured value, or null if absent
     */
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
    static final boolean disableAutoType;
    static final boolean disableSmartMatch;

    static Supplier<Map> defaultObjectSupplier;
    static Supplier<List> defaultArraySupplier;

    static final NameCacheEntry[] NAME_CACHE = new NameCacheEntry[8192];
    static final NameCacheEntry2[] NAME_CACHE2 = new NameCacheEntry2[8192];

    static int defaultDecimalMaxScale = 2048;
    static int defaultMaxLevel;
    public static final PropertyAccessorFactory PROPERTY_ACCESSOR_FACTORY;

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
                    disableAutoType0 = false,
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

        useJacksonAnnotation = getPropertyBool(properties, "fastjson2.useJacksonAnnotation", true);
        useGsonAnnotation = getPropertyBool(properties, "fastjson2.useGsonAnnotation", true);
        defaultWriterAlphabetic = getPropertyBool(properties, "fastjson2.writer.alphabetic", true);
        defaultSkipTransient = getPropertyBool(properties, "fastjson2.writer.skipTransient", true);
        defaultMaxLevel = getPropertyInt(properties, "fastjson2.writer.maxLevel", 2048);
        PropertyAccessorFactory propertyAccessorFactory = null;
        if (JDKUtils.JVM_VERSION >= 11) {
            try {
                String factoryClassNameJDK11 = "com.alibaba.fastjson2.reflect.PropertyAccessorFactoryMethodHandle";
                Class<?> classV = Conf.class.getClassLoader().loadClass(factoryClassNameJDK11);
                propertyAccessorFactory = (PropertyAccessorFactory) classV.newInstance();
            } catch (Exception ignored) {
                // ignore
            }
        }
        if (propertyAccessorFactory == null) {
            propertyAccessorFactory = JDKUtils.UNSAFE != null ? new PropertyAccessorFactoryUnsafe() : new PropertyAccessorFactory();
        }
        PROPERTY_ACCESSOR_FACTORY = propertyAccessorFactory;
    }

    private static boolean getPropertyBool(Properties properties, String name, boolean defaultValue) {
        boolean propertyValue = defaultValue;

        String property = System.getProperty(name);
        if (property != null) {
            property = property.trim();
        }
        if (property == null || property.isEmpty()) {
            property = properties.getProperty(name);
            if (property != null) {
                property = property.trim();
            }
        }
        if (property != null) {
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
        }
        if (property == null || property.isEmpty()) {
            property = properties.getProperty(name);
            if (property != null) {
                property = property.trim();
            }
        }
        try {
            propertyValue = Integer.parseInt(property);
        } catch (NumberFormatException ignored) {
            // ignore
        }

        return propertyValue;
    }

    /**
     * Returns whether supported Jackson annotations are recognized during introspection.
     * <details><summary>中文</summary>返回类型分析时是否识别受支持的 Jackson 注解。</details>
     *
     * @return whether Jackson annotation support is enabled
     */
    public static boolean isUseJacksonAnnotation() {
        return useJacksonAnnotation;
    }

    /**
     * Returns whether supported Gson annotations are recognized during introspection.
     * <details><summary>中文</summary>返回类型分析时是否识别受支持的 Gson 注解。</details>
     *
     * @return whether Gson annotation support is enabled
     */
    public static boolean isUseGsonAnnotation() {
        return useGsonAnnotation;
    }

    /**
     * Sets Jackson annotation support for subsequent introspection; cached codecs are not rebuilt.
     * <details><summary>中文</summary>设置后续类型分析的 Jackson 注解支持，不重新构建已缓存的编解码器。</details>
     *
     * @param useJacksonAnnotation whether to recognize supported Jackson annotations
     */
    public static void setUseJacksonAnnotation(boolean useJacksonAnnotation) {
        JSONFactory.useJacksonAnnotation = useJacksonAnnotation;
    }

    /**
     * Sets Gson annotation support for subsequent introspection; cached codecs are not rebuilt.
     * <details><summary>中文</summary>设置后续类型分析的 Gson 注解支持，不重新构建已缓存的编解码器。</details>
     *
     * @param useGsonAnnotation whether to recognize supported Gson annotations
     */
    public static void setUseGsonAnnotation(boolean useGsonAnnotation) {
        JSONFactory.useGsonAnnotation = useGsonAnnotation;
    }

    private static volatile boolean jsonFieldDefaultValueCompatMode;

    /**
     * Returns whether string defaults for {@link java.util.Date} fields use compatibility handling.
     * <details><summary>中文</summary>返回 Date 字段的字符串默认值是否使用兼容处理。</details>
     *
     * @return whether JSONField default-value compatibility is enabled
     */
    public static boolean isJSONFieldDefaultValueCompatMode() {
        return jsonFieldDefaultValueCompatMode;
    }

    /**
     * Controls compatibility handling of string defaults when Date field readers are created.
     * Existing cached readers are not rebuilt.
     * <details><summary>中文</summary>控制创建 Date 字段读取器时字符串默认值的兼容处理，不重建已缓存读取器。</details>
     *
     * @param compatMode whether to enable compatibility handling
     */
    public static void setJSONFieldDefaultValueCompatMode(boolean compatMode) {
        jsonFieldDefaultValueCompatMode = compatMode;
    }

    /**
     * Returns the nesting limit used when creating writer contexts.
     * <details><summary>中文</summary>返回创建写入上下文时使用的默认嵌套深度限制。</details>
     *
     * @return the default maximum nesting level for writers
     */
    public static int getDefaultMaxLevel() {
        return defaultMaxLevel;
    }

    /**
     * Sets the nesting limit for subsequently created writer contexts.
     * Existing contexts retain their current limits.
     * <details><summary>中文</summary>设置后续创建的写入上下文的嵌套深度限制，已有上下文不受影响。</details>
     *
     * @param maxLevel the positive maximum nesting level
     * @throws IllegalArgumentException if maxLevel is not positive
     */
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
     * Creates a reader context with exactly the supplied feature mask.
     * Unlike the feature-varargs overload, this replaces the default reader feature mask.
     * <details><summary>中文</summary>使用指定的完整特性位掩码创建读取上下文；该重载替换默认特性，而非追加。</details>
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

    /**
     * Creates a reader context with supported filter roles and additional features.
     * Recognized roles are {@link JSONReader.AutoTypeBeforeHandler} and {@link ExtraProcessor};
     * other filter types have no effect.
     * <details><summary>中文</summary>
     * 创建读取上下文并追加特性；识别自动类型前置处理器和额外属性处理器，其他过滤器类型不生效。
     * </details>
     *
     * @param filter the filter, or null
     * @param features features to enable in addition to defaults
     * @return a new reader context
     */
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

    /**
     * Creates a reader context with the supplied provider and additional features.
     * <details><summary>中文</summary>使用指定提供器创建读取上下文，并在默认特性上追加指定特性。</details>
     *
     * @param provider the provider, or null to use {@link #getDefaultObjectReaderProvider()}
     * @param features features to enable in addition to defaults
     * @return a new reader context
     */
    public static JSONReader.Context createReadContext(ObjectReaderProvider provider, JSONReader.Feature... features) {
        if (provider == null) {
            provider = getDefaultObjectReaderProvider();
        }

        JSONReader.Context context = new JSONReader.Context(provider);
        context.config(features);
        return context;
    }

    /**
     * Creates a reader context using an external JSONB symbol table.
     * <details><summary>中文</summary>使用外部 JSONB 符号表创建读取上下文。</details>
     *
     * @param symbolTable the external symbol table, or null
     * @return a new reader context with default features
     */
    public static JSONReader.Context createReadContext(SymbolTable symbolTable) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        return new JSONReader.Context(provider, symbolTable);
    }

    /**
     * Creates a reader context using an external JSONB symbol table and additional features.
     * <details><summary>中文</summary>使用外部 JSONB 符号表创建读取上下文，并追加指定特性。</details>
     *
     * @param symbolTable the external symbol table, or null
     * @param features features to enable in addition to defaults
     * @return a new reader context
     */
    public static JSONReader.Context createReadContext(SymbolTable symbolTable, JSONReader.Feature... features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider, symbolTable);
        context.config(features);
        return context;
    }

    /**
     * Creates a reader context with a supplier for untyped object values.
     * <details><summary>中文</summary>创建读取上下文，使用指定供应器创建未指定类型的对象值。</details>
     *
     * @param objectSupplier a supplier of new maps, or null to use built-in object creation
     * @param features features to enable in addition to defaults
     * @return a new reader context
     */
    public static JSONReader.Context createReadContext(Supplier<Map> objectSupplier, JSONReader.Feature... features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider);
        context.setObjectSupplier(objectSupplier);
        context.config(features);
        return context;
    }

    /**
     * Creates a reader context with suppliers for untyped object and array values.
     * <details><summary>中文</summary>创建读取上下文，使用指定供应器创建未指定类型的对象值和数组值。</details>
     *
     * @param objectSupplier a supplier of new maps, or null to use built-in object creation
     * @param arraySupplier a supplier of new lists, or null to use built-in array creation
     * @param features features to enable in addition to defaults
     * @return a new reader context
     */
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

    /**
     * Obtains a reader from the effective default provider.
     * Only {@link JSONReader.Feature#FieldBased} selects the reader here; other flags
     * must be supplied to the reading operation or its context.
     * <details><summary>中文</summary>
     * 从当前默认提供器获取读取器；这里只使用 FieldBased 选择读取器，其他特性需传给读取操作或上下文。
     * </details>
     *
     * @param type the target type
     * @param features the reader feature mask
     * @return the reader for the requested type and field-access mode
     */
    public static ObjectReader getObjectReader(Type type, long features) {
        return getDefaultObjectReaderProvider()
                .getObjectReader(type, JSONReader.Feature.FieldBased.isEnabled(features));
    }

    /**
     * Obtains a writer from the default provider.
     * Only {@link JSONWriter.Feature#FieldBased} selects the writer here; other flags
     * must be supplied to the writing operation or its context.
     * <details><summary>中文</summary>
     * 从默认提供器获取写入器；这里只使用 FieldBased 选择写入器，其他特性需传给写入操作或上下文。
     * </details>
     *
     * @param type the type to serialize
     * @param features the writer feature mask
     * @return the writer for the requested type and field-access mode
     */
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
     * Gets the current thread's reader provider override, or the shared default provider.
     * <details><summary>中文</summary>返回当前线程指定的读取器提供器；未指定时返回共享默认提供器。</details>
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
     * Gets the current thread's JSONPath compiler override, or the shared default compiler.
     * <details><summary>中文</summary>返回当前线程指定的 JSONPath 编译器；未指定时返回共享默认编译器。</details>
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
     * Sets the object reader creator override for the current thread; null clears the override.
     * <details><summary>中文</summary>设置当前线程的读取器创建器覆盖值；null 清除覆盖值。</details>
     *
     * @param creator the ObjectReaderCreator to set
     */
    public static void setContextReaderCreator(ObjectReaderCreator creator) {
        readerCreatorLocal.set(creator);
    }

    /**
     * Sets the current thread's reader provider override; null restores the shared default.
     * <details><summary>中文</summary>设置当前线程的读取器提供器；null 恢复使用共享默认提供器。</details>
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
     * Sets the current thread's JSONPath compiler override; null restores the shared default.
     * <details><summary>中文</summary>设置当前线程的 JSONPath 编译器；null 恢复使用共享默认编译器。</details>
     *
     * @param compiler the JSONPathCompiler to set
     */
    public static void setContextJSONPathCompiler(JSONPathCompiler compiler) {
        jsonPathCompilerLocal.set(compiler);
    }

    /**
     * Sets the object writer creator override for the current thread; null clears the override.
     * <details><summary>中文</summary>设置当前线程的写入器创建器覆盖值；null 清除覆盖值。</details>
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
        /**
         * Specializes a parsed path for access to instances of the supplied class.
         * <details><summary>中文</summary>针对指定类的实例访问，将已解析的路径编译为专用路径。</details>
         *
         * @param objectClass the root object's class
         * @param path the parsed path
         * @return the specialized path, or the original path when no specialization is needed
         */
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
     * Returns the startup configuration that disables reference detection.
     * This value does not change when {@link #setDisableReferenceDetect(boolean)} updates providers.
     * <details><summary>中文</summary>返回禁用引用检测的启动配置；通过设置方法更新提供器不会改变此值。</details>
     *
     * @return true if reference detection is disabled, false otherwise
     */
    public static boolean isDisableReferenceDetect() {
        return disableReferenceDetect;
    }

    /**
     * Returns the startup configuration that disables auto type support.
     * This value does not change when {@link #setDisableAutoType(boolean)} updates providers.
     * <details><summary>中文</summary>返回禁用自动类型支持的启动配置；通过设置方法更新提供器不会改变此值。</details>
     *
     * @return true if auto type is disabled, false otherwise
     */
    public static boolean isDisableAutoType() {
        return disableAutoType;
    }

    /**
     * Returns the startup configuration that disables JSONB format.
     * This value does not change when {@link #setDisableJSONB(boolean)} updates providers.
     * <details><summary>中文</summary>返回禁用JSONB 格式的启动配置；通过设置方法更新提供器不会改变此值。</details>
     *
     * @return true if JSONB is disabled, false otherwise
     */
    public static boolean isDisableJSONB() {
        return disableJSONB;
    }

    /**
     * Returns the startup configuration that disables array mapping.
     * This value does not change when {@link #setDisableArrayMapping(boolean)} updates providers.
     * <details><summary>中文</summary>返回禁用数组映射的启动配置；通过设置方法更新提供器不会改变此值。</details>
     *
     * @return true if array mapping is disabled, false otherwise
     */
    public static boolean isDisableArrayMapping() {
        return disableArrayMapping;
    }

    /**
     * Updates the shared default reader and writer providers to control reference detection.
     * Does not change the startup value returned by {@link #isDisableReferenceDetect()}.
     * <details><summary>中文</summary>更新共享默认提供器中的引用检测配置；不改变查询方法返回的启动配置值。</details>
     *
     * @param disableReferenceDetect true to disable reference detection, false to enable
     */
    public static void setDisableReferenceDetect(boolean disableReferenceDetect) {
        defaultObjectWriterProvider.setDisableReferenceDetect(disableReferenceDetect);
        defaultObjectReaderProvider.setDisableReferenceDetect(disableReferenceDetect);
    }

    /**
     * Updates the shared default reader and writer providers to control array mapping.
     * Does not change the startup value returned by {@link #isDisableArrayMapping()}.
     * <details><summary>中文</summary>更新共享默认提供器中的数组映射配置；不改变查询方法返回的启动配置值。</details>
     *
     * @param disableArrayMapping true to disable array mapping, false to enable
     */
    public static void setDisableArrayMapping(boolean disableArrayMapping) {
        defaultObjectWriterProvider.setDisableArrayMapping(disableArrayMapping);
        defaultObjectReaderProvider.setDisableArrayMapping(disableArrayMapping);
    }

    /**
     * Updates the shared default reader and writer providers to control JSONB format.
     * Does not change the startup value returned by {@link #isDisableJSONB()}.
     * <details><summary>中文</summary>更新共享默认提供器中的JSONB 格式配置；不改变查询方法返回的启动配置值。</details>
     *
     * @param disableJSONB true to disable JSONB, false to enable
     */
    public static void setDisableJSONB(boolean disableJSONB) {
        defaultObjectWriterProvider.setDisableJSONB(disableJSONB);
        defaultObjectReaderProvider.setDisableJSONB(disableJSONB);
    }

    /**
     * Updates the shared default reader and writer providers to control auto type support.
     * Does not change the startup value returned by {@link #isDisableAutoType()}.
     * <details><summary>中文</summary>更新共享默认提供器中的自动类型支持配置；不改变查询方法返回的启动配置值。</details>
     *
     * @param disableAutoType true to disable auto type, false to enable
     */
    public static void setDisableAutoType(boolean disableAutoType) {
        defaultObjectWriterProvider.setDisableAutoType(disableAutoType);
        defaultObjectReaderProvider.setDisableAutoType(disableAutoType);
    }

    /**
     * Returns the startup configuration that disables smart matching.
     * This value does not change when {@link #setDisableSmartMatch(boolean)} updates the provider.
     * <details><summary>中文</summary>返回禁用智能匹配的启动配置；通过设置方法更新提供器不会改变此值。</details>
     *
     * @return true if smart matching is disabled, false otherwise
     */
    public static boolean isDisableSmartMatch() {
        return disableSmartMatch;
    }

    /**
     * Updates the shared default reader provider to control smart matching.
     * Does not change the startup value returned by {@link #isDisableSmartMatch()}.
     * <details><summary>中文</summary>更新共享默认读取器提供器中的智能匹配配置；不改变查询方法返回的启动配置值。</details>
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
