package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;

public final class JSONFactory {
    public static final String CREATOR;

    public static final String PROPERTY_DENY_PROPERTY = "fastjson2.parser.deny";
    public static final String PROPERTY_AUTO_TYPE_ACCEPT = "fastjson2.autoTypeAccept";
    public static final String PROPERTY_AUTO_TYPE_HANDLER = "fastjson2.autoTypeHandler";
    public static final String PROPERTY_AUTO_TYPE_BEFORE_HANDLER = "fastjson2.autoTypeBeforeHandler";

    public static final boolean MIXED_HASH_ALGORITHM;

    protected static boolean useJacksonAnnotation;

    public static String getProperty(String key) {
        return DEFAULT_PROPERTIES.getProperty(key);
    }

    static long defaultReaderFeatures;
    static long defaultWriterFeatures;

    static Supplier<Map> defaultObjectSupplier;
    static Supplier<List> defaultArraySupplier;

    static final NameCacheEntry[] NAME_CACHE = new NameCacheEntry[8192];
    static final NameCacheEntry2[] NAME_CACHE2 = new NameCacheEntry2[8192];

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

    static final long INFLATED = Long.MIN_VALUE;

    static final double[] SMALL_10_POW = {
            1.0e0,
            1.0e1, 1.0e2, 1.0e3, 1.0e4, 1.0e5,
            1.0e6, 1.0e7, 1.0e8, 1.0e9, 1.0e10,
            1.0e11, 1.0e12, 1.0e13, 1.0e14, 1.0e15
    };

    static final float[] FLOAT_10_POW = {
            1.0e0f, 1.0e1f, 1.0e2f, 1.0e3f, 1.0e4f, 1.0e5f,
            1.0e6f, 1.0e7f, 1.0e8f, 1.0e9f, 1.0e10f
    };

    static final double[] DOUBLE_10_POW = {
            1.0e0, 1.0e1, 1.0e2, 1.0e3, 1.0e4, 1.0e5,
            1.0e6, 1.0e7, 1.0e8, 1.0e9, 1.0e10, 1.0e11,
            1.0e12, 1.0e13, 1.0e14, 1.0e15, 1.0e16, 1.0e17,
            1.0e18, 1.0e19, 1.0e20, 1.0e21, 1.0e22
    };

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

            if (property != null && "mixed".equals(property)) {
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

            useJacksonAnnotation = property == null || !property.equals("false");
        }
    }

    public static boolean isUseJacksonAnnotation() {
        return useJacksonAnnotation;
    }

    public static void setUseJacksonAnnotation(boolean useJacksonAnnotation) {
        JSONFactory.useJacksonAnnotation = useJacksonAnnotation;
    }

    static final int CACHE_SIZE = 4;
    private static final int CACHE_THRESHOLD = 1024 * 1024;
    private static final byte[][] BYTE_ARRAY_CACHE = new byte[CACHE_SIZE][];
    private static final char[][] CHAR_ARRAY_CACHE = new char[CACHE_SIZE][];

    static char[] allocateCharArray(int cacheIndex) {
        char[] chars;
        synchronized (CHAR_ARRAY_CACHE) {
            chars = CHAR_ARRAY_CACHE[cacheIndex];
            if (chars != null) {
                CHAR_ARRAY_CACHE[cacheIndex] = null;
            }
        }
        if (chars == null) {
            chars = new char[8192];
        }
        return chars;
    }

    static void releaseCharArray(int cacheIndex, char[] chars) {
        if (chars == null || chars.length > CACHE_THRESHOLD) {
            return;
        }
        synchronized (CHAR_ARRAY_CACHE) {
            CHAR_ARRAY_CACHE[cacheIndex] = chars;
        }
    }

    static byte[] allocateByteArray(int cacheIndex) {
        byte[] bytes;
        synchronized (BYTE_ARRAY_CACHE) {
            bytes = BYTE_ARRAY_CACHE[cacheIndex];
            if (bytes != null) {
                BYTE_ARRAY_CACHE[cacheIndex] = null;
            }
        }
        if (bytes == null) {
            bytes = new byte[8192];
        }
        return bytes;
    }

    static void releaseByteArray(int cacheIndex, byte[] chars) {
        if (chars == null || chars.length > CACHE_THRESHOLD) {
            return;
        }
        synchronized (BYTE_ARRAY_CACHE) {
            BYTE_ARRAY_CACHE[cacheIndex] = chars;
        }
    }

    static final class SymbolTableImpl
            implements SymbolTable {
        private final String[] names;
        private final long hashCode64;
        private final short[] mapping;

        private final long[] hashCodes;
        private final long[] hashCodesOrigin;

        SymbolTableImpl(String... input) {
            Set<String> set = new TreeSet<>();
            for (String name : input) {
                set.add(name);
            }
            names = new String[set.size()];
            Iterator<String> it = set.iterator();

            for (int i = 0; i < names.length; i++) {
                if (it.hasNext()) {
                    names[i] = it.next();
                }
            }

            long[] hashCodes = new long[names.length];
            for (int i = 0; i < names.length; i++) {
                long hashCode = Fnv.hashCode64(names[i]);
                hashCodes[i] = hashCode;
            }
            this.hashCodesOrigin = hashCodes;

            this.hashCodes = Arrays.copyOf(hashCodes, hashCodes.length);
            Arrays.sort(this.hashCodes);

            mapping = new short[this.hashCodes.length];
            for (int i = 0; i < hashCodes.length; i++) {
                long hashCode = hashCodes[i];
                int index = Arrays.binarySearch(this.hashCodes, hashCode);
                mapping[index] = (short) i;
            }

            long hashCode64 = Fnv.MAGIC_HASH_CODE;
            for (long hashCode : hashCodes) {
                hashCode64 ^= hashCode;
                hashCode64 *= Fnv.MAGIC_PRIME;
            }
            this.hashCode64 = hashCode64;
        }

        @Override
        public int size() {
            return names.length;
        }

        @Override
        public long hashCode64() {
            return hashCode64;
        }

        @Override
        public String getNameByHashCode(long hashCode) {
            int m = Arrays.binarySearch(hashCodes, hashCode);
            if (m < 0) {
                return null;
            }

            int index = this.mapping[m];
            return names[index];
        }

        @Override
        public int getOrdinalByHashCode(long hashCode) {
            int m = Arrays.binarySearch(hashCodes, hashCode);
            if (m < 0) {
                return -1;
            }

            return this.mapping[m] + 1;
        }

        @Override
        public int getOrdinal(String name) {
            long hashCode = Fnv.hashCode64(name);
            int m = Arrays.binarySearch(hashCodes, hashCode);
            if (m < 0) {
                return -1;
            }

            return this.mapping[m] + 1;
        }

        @Override
        public String getName(int ordinal) {
            return names[ordinal - 1];
        }

        @Override
        public long getHashCode(int ordinal) {
            return hashCodesOrigin[ordinal - 1];
        }
    }

    static final Properties DEFAULT_PROPERTIES;

    static ObjectWriterProvider defaultObjectWriterProvider = new ObjectWriterProvider();
    static ObjectReaderProvider defaultObjectReaderProvider = new ObjectReaderProvider();

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
                    compiler = JSONPathCompilerReflectASM.INSTANCE;
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
