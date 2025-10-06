package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderImplList;
import com.alibaba.fastjson2.reader.ObjectReaderImplMap;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.NameCacheEntry;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.function.Supplier;

public final class JSONFactory {
    static long defaultReaderFeatures;
    static String defaultReaderFormat;
    static ZoneId defaultReaderZoneId;

    static long defaultWriterFeatures;
    static String defaultWriterFormat;
    static ZoneId defaultWriterZoneId;

    static Supplier<Map> defaultObjectSupplier;
    static Supplier<List> defaultArraySupplier;

    static final NameCacheEntry[] NAME_CACHE = new NameCacheEntry[8192];
    static final NameCacheEntry2[] NAME_CACHE2 = new NameCacheEntry2[8192];

    static Class JSON_OBJECT_CLASS_1x;
    static Supplier JSON_OBJECT_1x_SUPPLIER;
    static Function JSON_OBJECT_1x_INNER_MAP;
    static Function JSON_OBJECT_1x_BUILDER;
    static Class JSON_ARRAY_CLASS_1x;
    static Supplier JSON_ARRAY_1x_SUPPLIER;
    static volatile boolean JSON_REFLECT_1x_ERROR;

    static int defaultDecimalMaxScale = 2048;

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

    static final Double DOUBLE_ZERO = Double.valueOf(0);

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

    public static final ObjectWriterProvider defaultObjectWriterProvider = new ObjectWriterProvider();
    public static final ObjectReaderProvider defaultObjectReaderProvider = new ObjectReaderProvider();

    static final ObjectReader<JSONArray> ARRAY_READER = ObjectReaderImplList.JSON_ARRAY_READER;
    static final ObjectReader<JSONObject> OBJECT_READER = ObjectReaderImplMap.INSTANCE_OBJECT;

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
        return new JSONReader.Context(defaultObjectReaderProvider);
    }

    public static JSONReader.Context createReadContext(long features) {
        return new JSONReader.Context(defaultObjectReaderProvider, features);
    }

    public static JSONReader.Context createReadContext(JSONReader.Feature... features) {
        return new JSONReader.Context(defaultObjectReaderProvider, features);
    }

    public static JSONReader.Context createReadContext(Filter filter, JSONReader.Feature... features) {
        JSONReader.Context context = new JSONReader.Context(defaultObjectReaderProvider, features);
        context.config(filter);
        return context;
    }

    public static JSONReader.Context createReadContext(Filter[] filters, JSONReader.Feature... features) {
        JSONReader.Context context = new JSONReader.Context(defaultObjectReaderProvider, features);
        context.config(filters);
        return context;
    }

    public static JSONReader.Context createReadContext(ObjectReaderProvider provider, JSONReader.Feature... features) {
        if (provider == null) {
            provider = defaultObjectReaderProvider;
        }

        JSONReader.Context context = new JSONReader.Context(provider);
        context.config(features);
        return context;
    }

    public static JSONReader.Context createReadContext(SymbolTable symbolTable) {
        return new JSONReader.Context(defaultObjectReaderProvider, symbolTable);
    }

    public static JSONReader.Context createReadContext(SymbolTable symbolTable, JSONReader.Feature... features) {
        JSONReader.Context context = new JSONReader.Context(defaultObjectReaderProvider, symbolTable);
        context.config(features);
        return context;
    }

    public static JSONReader.Context createReadContext(Supplier<Map> objectSupplier, JSONReader.Feature... features) {
        JSONReader.Context context = new JSONReader.Context(defaultObjectReaderProvider);
        context.setObjectSupplier(objectSupplier);
        context.config(features);
        return context;
    }

    public static JSONReader.Context createReadContext(
            Supplier<Map> objectSupplier,
            Supplier<List> arraySupplier,
            JSONReader.Feature... features) {
        JSONReader.Context context = new JSONReader.Context(defaultObjectReaderProvider);
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

    public static ObjectWriterProvider getDefaultObjectWriterProvider() {
        return defaultObjectWriterProvider;
    }

    public static ObjectReaderProvider getDefaultObjectReaderProvider() {
        return defaultObjectReaderProvider;
    }

    public static Class getClassJSONObject1x() {
        if (JSON_OBJECT_CLASS_1x == null && !JSON_REFLECT_1x_ERROR) {
            try {
                JSON_OBJECT_CLASS_1x = com.alibaba.fastjson.JSONObject.class;
            } catch (Throwable ignored) {
                JSON_REFLECT_1x_ERROR = true;
            }
        }

        return JSON_OBJECT_CLASS_1x;
    }

    public static Class getClassJSONArray1x() {
        if (JSON_ARRAY_CLASS_1x == null && !JSON_REFLECT_1x_ERROR) {
            try {
                JSON_ARRAY_CLASS_1x = com.alibaba.fastjson.JSONArray.class;
            } catch (Throwable ignored) {
                JSON_REFLECT_1x_ERROR = true;
            }
        }

        return JSON_ARRAY_CLASS_1x;
    }

    public static Function getBuilderJSONObject1x() {
        if (JSON_OBJECT_1x_BUILDER == null && !JSON_REFLECT_1x_ERROR) {
            Class classJSONObject1x = getClassJSONObject1x();
            if (classJSONObject1x != null) {
                try {
                    JSON_OBJECT_1x_BUILDER = new FJ1OjbectBuilder();
                } catch (Throwable e) {
                    JSON_REFLECT_1x_ERROR = true;
                    throw new JSONException("create JSONObject1 error");
                }
            }
        }

        return JSON_OBJECT_1x_BUILDER;
    }

    public static Function getInnerMap() {
        if (JSON_OBJECT_1x_INNER_MAP == null && !JSON_REFLECT_1x_ERROR) {
            Class classJSONObject1x = getClassJSONObject1x();
            if (classJSONObject1x != null) {
                try {
                    JSON_OBJECT_1x_INNER_MAP = new FJ1ObjectInnerSupplier();
                } catch (Throwable e) {
                    JSON_REFLECT_1x_ERROR = true;
                    throw new JSONException("create getInnerMap error");
                }
            }
        }

        return JSON_OBJECT_1x_INNER_MAP;
    }

    public static Map createJSONObject1(Map map) {
        return new com.alibaba.fastjson.JSONObject(map);
    }

    private static final class FJ1ObjectInnerSupplier
            implements Function {
        @Override
        public Object apply(Object o) {
            return o instanceof com.alibaba.fastjson.JSONObject
                    ? ((com.alibaba.fastjson.JSONObject) o).getInnerMap()
                    : null;
        }
    }

    private static final class FJ1OjbectBuilder
            implements Function {
        @Override
        public Object apply(Object o) {
            return new com.alibaba.fastjson.JSONObject((Map) o);
        }
    }

    public static long getDefaultReaderFeatures() {
        return defaultReaderFeatures;
    }
}
