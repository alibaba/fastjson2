package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;
import static com.alibaba.fastjson2.util.JDKUtils.VECTOR_BIT_LENGTH;

public final class JSONFactory {
    static volatile Throwable initErrorLast;

    public static final String CREATOR;

    public static final String PROPERTY_DENY_PROPERTY = "fastjson2.parser.deny";
    public static final String PROPERTY_AUTO_TYPE_ACCEPT = "fastjson2.autoTypeAccept";
    public static final String PROPERTY_AUTO_TYPE_HANDLER = "fastjson2.autoTypeHandler";
    public static final String PROPERTY_AUTO_TYPE_BEFORE_HANDLER = "fastjson2.autoTypeBeforeHandler";

    public static final boolean MIXED_HASH_ALGORITHM;

    static boolean useJacksonAnnotation;

    public static String getProperty(String key) {
        return DEFAULT_PROPERTIES.getProperty(key);
    }

    static long defaultReaderFeatures;
    static String defaultReaderFormat;

    static long defaultWriterFeatures;
    static String defaultWriterFormat;

    static Supplier<Map> defaultObjectSupplier;
    static Supplier<List> defaultArraySupplier;

    static final NameCacheEntry[] NAME_CACHE = new NameCacheEntry[8192];
    static final NameCacheEntry2[] NAME_CACHE2 = new NameCacheEntry2[8192];

    static final Function<JSONWriter.Context, JSONWriter> INCUBATOR_VECTOR_WRITER_CREATOR_UTF8;
    static final Function<JSONWriter.Context, JSONWriter> INCUBATOR_VECTOR_WRITER_CREATOR_UTF16;
    static final JSONReaderUTF8Creator INCUBATOR_VECTOR_READER_CREATOR_ASCII;
    static final JSONReaderUTF8Creator INCUBATOR_VECTOR_READER_CREATOR_UTF8;
    static final JSONReaderUTF16Creator INCUBATOR_VECTOR_READER_CREATOR_UTF16;

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

    static final BigDecimal LOW = BigDecimal.valueOf(-9007199254740991L);
    static final BigDecimal HIGH = BigDecimal.valueOf(9007199254740991L);
    static final BigInteger LOW_BIGINT = BigInteger.valueOf(-9007199254740991L);
    static final BigInteger HIGH_BIGINT = BigInteger.valueOf(9007199254740991L);

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

    static final Double DOUBLE_ZERO = (double) 0;

    static {
        Properties properties = new Properties();

        InputStream inputStream = AccessController.doPrivileged((PrivilegedAction<InputStream>) () -> {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            final String resourceFile = "fastjson2.properties";

            if (cl != null) {
                return cl.getResourceAsStream(resourceFile);
            } else {
                return ClassLoader.getSystemResourceAsStream(resourceFile);
            }
        });
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } catch (java.io.IOException ignored) {
            } finally {
                IOUtils.close(inputStream);
            }
        }
        DEFAULT_PROPERTIES = properties;

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
            String property = System.getProperty("fastjson2.hash-algorithm");
            if (property != null) {
                property = property.trim();
            }

            if (property == null || property.isEmpty()) {
                property = properties.getProperty("fastjson2.hash-algorithm");
                if (property != null) {
                    property = property.trim();
                }
            }

            if ("mixed".equals(property)) {
                MIXED_HASH_ALGORITHM = true;
            } else {
                MIXED_HASH_ALGORITHM = JVM_VERSION > 8;
            }
        }

        {
            String property = System.getProperty("fastjson2.useJacksonAnnotation");
            if (property != null) {
                property = property.trim();
            }

            if (property == null || property.isEmpty()) {
                property = properties.getProperty("fastjson2.useJacksonAnnotation");
                if (property != null) {
                    property = property.trim();
                }
            }

            useJacksonAnnotation = !"false".equals(property);
        }

        boolean readerVector = false;
        {
            String property = System.getProperty("fastjson2.readerVector");
            if (property != null) {
                property = property.trim();
                if (property == null || property.isEmpty()) {
                    property = properties.getProperty("fastjson2.readerVector");
                    if (property != null) {
                        property = property.trim();
                    }
                }
                readerVector = !"false".equals(property);
            }
        }

        Function<JSONWriter.Context, JSONWriter> incubatorVectorCreatorUTF8 = null;
        Function<JSONWriter.Context, JSONWriter> incubatorVectorCreatorUTF16 = null;
        JSONReaderUTF8Creator readerCreatorASCII = null;
        JSONReaderUTF8Creator readerCreatorUTF8 = null;
        JSONReaderUTF16Creator readerCreatorUTF16 = null;
        if (JDKUtils.VECTOR_SUPPORT) {
            if (VECTOR_BIT_LENGTH >= 64) {
                try {
                    Class<?> factoryClass = Class.forName("com.alibaba.fastjson2.JSONWriterUTF8Vector$Factory");
                    incubatorVectorCreatorUTF8 = (Function<JSONWriter.Context, JSONWriter>) factoryClass.newInstance();
                } catch (Throwable e) {
                    initErrorLast = e;
                }

                try {
                    Class<?> factoryClass = Class.forName("com.alibaba.fastjson2.JSONWriterUTF16Vector$Factory");
                    incubatorVectorCreatorUTF16 = (Function<JSONWriter.Context, JSONWriter>) factoryClass.newInstance();
                } catch (Throwable e) {
                    initErrorLast = e;
                }

                if (readerVector) {
                    try {
                        Class<?> factoryClass = Class.forName("com.alibaba.fastjson2.JSONReaderASCIIVector$Factory");
                        readerCreatorASCII = (JSONReaderUTF8Creator) factoryClass.newInstance();
                    } catch (Throwable e) {
                        initErrorLast = e;
                    }

                    try {
                        Class<?> factoryClass = Class.forName("com.alibaba.fastjson2.JSONReaderUTF8Vector$Factory");
                        readerCreatorUTF8 = (JSONReaderUTF8Creator) factoryClass.newInstance();
                    } catch (Throwable e) {
                        initErrorLast = e;
                    }
                }
            }

            if (VECTOR_BIT_LENGTH >= 128 && readerVector) {
                try {
                    Class<?> factoryClass = Class.forName("com.alibaba.fastjson2.JSONReaderUTF16Vector$Factory");
                    readerCreatorUTF16 = (JSONReaderUTF16Creator) factoryClass.newInstance();
                } catch (Throwable e) {
                    initErrorLast = e;
                }
            }
        }
        INCUBATOR_VECTOR_WRITER_CREATOR_UTF8 = incubatorVectorCreatorUTF8;
        INCUBATOR_VECTOR_WRITER_CREATOR_UTF16 = incubatorVectorCreatorUTF16;
        INCUBATOR_VECTOR_READER_CREATOR_ASCII = readerCreatorASCII;
        INCUBATOR_VECTOR_READER_CREATOR_UTF8 = readerCreatorUTF8;
        INCUBATOR_VECTOR_READER_CREATOR_UTF16 = readerCreatorUTF16;
    }

    public static boolean isUseJacksonAnnotation() {
        return useJacksonAnnotation;
    }

    public static void setUseJacksonAnnotation(boolean useJacksonAnnotation) {
        JSONFactory.useJacksonAnnotation = useJacksonAnnotation;
    }

    static final CacheItem[] CACHE_ITEMS;

    static {
        final CacheItem[] items = new CacheItem[16];
        for (int i = 0; i < items.length; i++) {
            items[i] = new CacheItem();
        }
        CACHE_ITEMS = items;
    }

    static final int CACHE_THRESHOLD = 1024 * 1024;
    static final AtomicReferenceFieldUpdater<CacheItem, char[]> CHARS_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(CacheItem.class, char[].class, "chars");
    static final AtomicReferenceFieldUpdater<CacheItem, byte[]> BYTES_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(CacheItem.class, byte[].class, "bytes");

    static final class CacheItem {
        volatile char[] chars;
        volatile byte[] bytes;
    }

    static final Properties DEFAULT_PROPERTIES;

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

    static final char[] UUID_LOOKUP;
    static final byte[] UUID_VALUES;

    static {
        UUID_LOOKUP = new char[256];
        UUID_VALUES = new byte['f' + 1 - '0'];
        for (int i = 0; i < 256; i++) {
            int hi = (i >> 4) & 15;
            int lo = i & 15;
            UUID_LOOKUP[i] = (char) (((hi < 10 ? '0' + hi : 'a' + hi - 10) << 8) + (lo < 10 ? '0' + lo : 'a' + lo - 10));
        }
        for (char c = '0'; c <= '9'; c++) {
            UUID_VALUES[c - '0'] = (byte) (c - '0');
        }
        for (char c = 'a'; c <= 'f'; c++) {
            UUID_VALUES[c - '0'] = (byte) (c - 'a' + 10);
        }
        for (char c = 'A'; c <= 'F'; c++) {
            UUID_VALUES[c - '0'] = (byte) (c - 'A' + 10);
        }
    }

    /**
     * @param objectSupplier
     * @since 2.0.15
     */
    public static void setDefaultObjectSupplier(Supplier<Map> objectSupplier) {
        defaultObjectSupplier = objectSupplier;
    }

    /**
     * @param arraySupplier
     * @since 2.0.15
     */
    public static void setDefaultArraySupplier(Supplier<List> arraySupplier) {
        defaultArraySupplier = arraySupplier;
    }

    public static Supplier<Map> getDefaultObjectSupplier() {
        return defaultObjectSupplier;
    }

    public static Supplier<List> getDefaultArraySupplier() {
        return defaultArraySupplier;
    }

    public static JSONWriter.Context createWriteContext() {
        return new JSONWriter.Context(defaultObjectWriterProvider);
    }

    public static JSONWriter.Context createWriteContext(ObjectWriterProvider provider, JSONWriter.Feature... features) {
        JSONWriter.Context context = new JSONWriter.Context(provider);
        context.config(features);
        return context;
    }

    public static JSONWriter.Context createWriteContext(JSONWriter.Feature... features) {
        return new JSONWriter.Context(defaultObjectWriterProvider, features);
    }

    public static JSONReader.Context createReadContext() {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        return new JSONReader.Context(provider);
    }

    public static JSONReader.Context createReadContext(long features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        return new JSONReader.Context(provider, features);
    }

    public static JSONReader.Context createReadContext(JSONReader.Feature... features) {
        JSONReader.Context context = new JSONReader.Context(
                JSONFactory.getDefaultObjectReaderProvider()
        );
        context.config(features);
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
            JSONReader.Feature... features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider);
        context.setObjectSupplier(objectSupplier);
        context.setArraySupplier(arraySupplier);
        context.config(features);
        return context;
    }

    public static ObjectWriterProvider getDefaultObjectWriterProvider() {
        return defaultObjectWriterProvider;
    }

    public static ObjectReaderProvider getDefaultObjectReaderProvider() {
        ObjectReaderProvider providerLocal = readerProviderLocal.get();
        if (providerLocal != null) {
            return providerLocal;
        }

        return defaultObjectReaderProvider;
    }

    public static JSONPathCompiler getDefaultJSONPathCompiler() {
        JSONPathCompiler compilerLocal = jsonPathCompilerLocal.get();
        if (compilerLocal != null) {
            return compilerLocal;
        }

        return defaultJSONPathCompiler;
    }

    public static void setContextReaderCreator(ObjectReaderCreator creator) {
        readerCreatorLocal.set(creator);
    }

    public static void setContextObjectReaderProvider(ObjectReaderProvider creator) {
        readerProviderLocal.set(creator);
    }

    public static ObjectReaderCreator getContextReaderCreator() {
        return readerCreatorLocal.get();
    }

    public static void setContextJSONPathCompiler(JSONPathCompiler compiler) {
        jsonPathCompilerLocal.set(compiler);
    }

    public static void setContextWriterCreator(ObjectWriterCreator creator) {
        writerCreatorLocal.set(creator);
    }

    public static ObjectWriterCreator getContextWriterCreator() {
        return writerCreatorLocal.get();
    }

    public interface JSONPathCompiler {
        JSONPath compile(Class objectClass, JSONPath path);
    }
}
