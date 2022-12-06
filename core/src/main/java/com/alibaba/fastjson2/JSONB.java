package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderBean;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.*;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static com.alibaba.fastjson2.JSONFactory.CACHE_SIZE;
import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE_SUPPORT;

/**
 * x92          # type_char int
 * x91          # binary len_int32 bytes
 * x92          # type [str] symbol_int32 jsonb
 * x93          # reference
 * <p>
 * x94 - xa3    # array_0 - array_15
 * xa4          # array len_int32 item*
 * <p>
 * xa5          # object_end
 * xa6          # object_start
 * <p>
 * xa7          # local time b0 b1 b2
 * xa8          # local datetime b0 b1 b2 b3 b4 b5 b6
 * xa9          # local date b0 b1 b2 b3
 * xab          # timestamp millis b0 b1 b2 b3 b4 b5 b6 b7
 * xac          # timestamp seconds b0 b1 b2 b3
 * xad          # timestamp minutes b0 b1 b2 b3
 * xae          # timestamp b0 b1 b2 b3 b4 b5 b6 b7 nano_int32
 * <p>
 * xaf          # null
 * xb0          # boolean false
 * xb1          # boolean true
 * xb2          # double 0
 * xb3          # double 1
 * xb4          # double_long
 * xb5          # double
 * xb6          # float_int
 * xb7          # float
 * xb8          # decimal_long
 * xb9          # decimal
 * xba          # bigint_long
 * xbb          # bigint
 * xbc          # short
 * xbd          # byte
 * xbe          # long
 * xbf          # long encoded as 32-bit int
 * xc0 - xc7    # three-octet compact long (-x40000 to x3ffff)
 * xc8 - xd7    # two-octet compact long (-x800 to x7ff, xd0 is 0)
 * xd8 - xef    # one-octet compact long (-x8 to xf, xe0 is 0)
 * <p>
 * xf0 - xff    # one-octet compact int
 * x00 - x2f    # one-octet compact int
 * <p>
 * x30 - x3f    # two-octet compact int (-x800 to x7ff)
 * x40 - x47    # three-octet compact int (-x40000 to x3ffff)
 * x48          # 32-bit signed integer ('I')
 * <p>
 * x49 - x78    # ascii string length 0-47
 * x79          # ascii-8 string variable-length
 * x7a          # utf-8 string variable-length
 * x7b          # utf-16 string variable-length
 * x7c          # utf-16LE string variable-length
 * x7d          # utf-16BE string variable-length
 * x7e          # gb18030 string variable-length
 * x7f          # symbol
 */
public interface JSONB {
    interface Constants {
        byte BC_CHAR = -112;                    // 0x92
        byte BC_BINARY = -111;                  // 0x91
        byte BC_TYPED_ANY = -110;               // 0x92
        byte BC_REFERENCE = -109;               // 0x93

        int ARRAY_FIX_LEN = 15;
        byte BC_ARRAY_FIX_0 = -108;             // 0x94
        byte BC_ARRAY_FIX_MIN = BC_ARRAY_FIX_0;
        byte BC_ARRAY_FIX_MAX = BC_ARRAY_FIX_MIN + ARRAY_FIX_LEN; // -105
        byte BC_ARRAY = -92;                    // 0xa4 len_int32 item*

        byte BC_OBJECT_END = -91;               // 0xa5
        byte BC_OBJECT = -90;                   // 0xa6

        byte BC_LOCAL_TIME = -89;               // 0xa7 b0 b1 b2 nano_int32
        byte BC_LOCAL_DATETIME = -88;           // 0xa8 b0 b1 b2 b3 b4 b5 b6 nano_int32
        byte BC_LOCAL_DATE = -87;               // 0xa9 b0 b1 b2 b3
        byte BC_TIMESTAMP_WITH_TIMEZONE = -86;  // 0xaa b0 b1 b2 b3 b4 b5 b6 b7 str_zone
        byte BC_TIMESTAMP_MILLIS = -85;         // 0xab b0 b1 b2 b3 b4 b5 b6 b7
        byte BC_TIMESTAMP_SECONDS = -84;        // 0xac b0 b1 b2 b3
        byte BC_TIMESTAMP_MINUTES = -83;        // 0xad b0 b1 b2 b3
        byte BC_TIMESTAMP = -82;                // 0xae millis_8 + nano_int32

        byte BC_NULL = -81;             // 0xaf
        byte BC_FALSE = -80;            // 0xb0
        byte BC_TRUE = -79;             // 0xb1
        byte BC_DOUBLE_NUM_0 = -78;     // 0xb2
        byte BC_DOUBLE_NUM_1 = -77;     // 0xb3
        byte BC_DOUBLE_LONG = -76;      // 0xb4
        byte BC_DOUBLE = -75;           // 0xb5
        byte BC_FLOAT_INT = -74;        // 0xb6
        byte BC_FLOAT = -73;            // 0xb7
        byte BC_DECIMAL_LONG = -72;     // 0xb8
        byte BC_DECIMAL = -71;          // 0xb9
        byte BC_BIGINT_LONG = -70;      // 0xba
        byte BC_BIGINT = -69;           // 0xbb
        byte BC_INT16 = -68;            // 0xbc b0 b1
        byte BC_INT8 = -67;             // 0xbd b0
        byte BC_INT64 = -66;            // 0xbe b0 b1 b2 b3 b4 b5 b6 b7
        byte BC_INT64_INT = -65;        // 0xbf b0 b1 b2 b3

        int INT64_SHORT_MIN = -0x40000; // -262144
        int INT64_SHORT_MAX = 0x3ffff;  // 262143

        int INT64_BYTE_MIN = -0x800;    // -2048
        int INT64_BYTE_MAX = 0x7ff;     // 2047

        byte BC_INT64_SHORT_MIN = -64;
        byte BC_INT64_SHORT_ZERO = -60; //
        byte BC_INT64_SHORT_MAX = -57;  // 0xc7

        byte BC_INT64_BYTE_MIN = -56;   // 0xc8
        byte BC_INT64_BYTE_ZERO = -48;
        byte BC_INT64_BYTE_MAX = -41;   // 0xd7

        byte BC_INT64_NUM_MIN = -40;    // 0xd8 -8
        byte BC_INT64_NUM_MAX = -17;    // 0xef 15

        int INT64_NUM_LOW_VALUE = -8;  // -8
        int INT64_NUM_HIGH_VALUE = 15; // 15

        byte BC_INT32_NUM_0 = 0;
        byte BC_INT32_NUM_1 = 1;
        byte BC_INT32_NUM_16 = 16;

        byte BC_INT32_NUM_MIN = -16; // 0xf0
        byte BC_INT32_NUM_MAX = 47;  // 0x2f

        byte BC_INT32_BYTE_MIN = 48;    // 0x30
        byte BC_INT32_BYTE_ZERO = 56;
        byte BC_INT32_BYTE_MAX = 63;

        byte BC_INT32_SHORT_MIN = 64;
        byte BC_INT32_SHORT_ZERO = 68;
        byte BC_INT32_SHORT_MAX = 71;
        byte BC_INT32 = 72;

        int INT32_BYTE_MIN = -0x800; // -2048
        int INT32_BYTE_MAX = 0x7ff;  // 2047

        int INT32_SHORT_MIN = -0x40000; // -262144
        int INT32_SHORT_MAX = 0x3ffff;  // 262143

        byte BC_STR_ASCII_FIX_0 = 73;
        byte BC_STR_ASCII_FIX_1 = 74;
        byte BC_STR_ASCII_FIX_4 = 77;
        byte BC_STR_ASCII_FIX_5 = 78;

        byte BC_STR_ASCII_FIX_32 = 105;
        byte BC_STR_ASCII_FIX_36 = 109;

        int STR_ASCII_FIX_LEN = 47;

        byte BC_STR_ASCII_FIX_MIN = 73; // 0x49
        byte BC_STR_ASCII_FIX_MAX = BC_STR_ASCII_FIX_MIN + STR_ASCII_FIX_LEN; // 120 0x78
        byte BC_STR_ASCII = 121;
        byte BC_STR_UTF8 = 122;
        byte BC_STR_UTF16 = 123;
        byte BC_STR_UTF16LE = 124;
        byte BC_STR_UTF16BE = 125;
        byte BC_STR_GB18030 = 126;
        byte BC_SYMBOL = 127;
    }

    static byte[] toBytes(boolean v) {
        return new byte[]{v ? BC_TRUE : BC_FALSE};
    }

    static byte[] toBytes(int i) {
        if (i >= BC_INT32_NUM_MIN && i <= BC_INT32_NUM_MAX) {
            return new byte[]{(byte) i};
        }

        try (JSONWriter jsonWriter = JSONWriter.ofJSONB()) {
            jsonWriter.writeInt32(i);
            return jsonWriter.getBytes();
        }
    }

    static byte[] toBytes(long i) {
        if (i >= INT64_NUM_LOW_VALUE && i <= INT64_NUM_HIGH_VALUE) {
            return new byte[]{
                    (byte) (BC_INT64_NUM_MIN + (i - INT64_NUM_LOW_VALUE))
            };
        }

        try (JSONWriter jsonWriter = JSONWriter.ofJSONB()) {
            jsonWriter.writeInt64(i);
            return jsonWriter.getBytes();
        }
    }

    static int writeInt(byte[] bytes, int off, int i) {
        if (i >= BC_INT32_NUM_MIN && i <= BC_INT32_NUM_MAX) {
            bytes[off++] = (byte) i;
            return 1;
        }

        if (i >= INT32_BYTE_MIN && i <= INT32_BYTE_MAX) {
            bytes[off++] = (byte) (BC_INT32_BYTE_ZERO + (i >> 8));
            bytes[off++] = (byte) i;
            return 2;
        }

        if (i >= INT32_SHORT_MIN && i <= INT32_SHORT_MAX) {
            bytes[off++] = (byte) (BC_INT32_SHORT_ZERO + (i >> 16));
            bytes[off++] = (byte) (i >> 8);
            bytes[off++] = (byte) i;
            return 3;
        }

        bytes[off++] = BC_INT32;
        bytes[off++] = (byte) (i >>> 24);
        bytes[off++] = (byte) (i >>> 16);
        bytes[off++] = (byte) (i >>> 8);
        bytes[off++] = (byte) i;
        return 5;
    }

    static Object parse(byte[] jsonbBytes, JSONReader.Feature... features) {
        JSONReader reader = JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length);
        reader.getContext().config(features);
        ObjectReader objectReader = reader.getObjectReader(Object.class);

        Object object = objectReader.readJSONBObject(reader, null, null, 0);
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static JSONObject parseObject(byte[] jsonbBytes) {
        JSONReader.Context context = new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider());
        JSONReader reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);

        JSONObject object = (JSONObject) reader.readObject();
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static JSONObject parseObject(byte[] jsonbBytes, JSONReader.Feature... features) {
        JSONReader.Context context = new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider());
        context.config(features);

        JSONReader reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);

        JSONObject object = (JSONObject) reader.readObject();
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static JSONArray parseArray(byte[] jsonbBytes) {
        JSONReader.Context context = new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider());
        JSONReader reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);
        JSONArray array = (JSONArray) reader.readArray();
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(array);
        }
        return array;
    }

    static <T> List<T> parseArray(byte[] jsonbBytes, Type type) {
        if (jsonbBytes == null || jsonbBytes.length == 0) {
            return null;
        }

        Type paramType = new ParameterizedTypeImpl(
                new Type[]{type}, null, List.class
        );

        try (JSONReader reader = JSONReader.ofJSONB(jsonbBytes)) {
            List<T> list = reader.read(paramType);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            return list;
        }
    }

    static <T> List<T> parseArray(byte[] jsonbBytes, Type type, JSONReader.Feature... features) {
        if (jsonbBytes == null || jsonbBytes.length == 0) {
            return null;
        }

        Type paramType = new ParameterizedTypeImpl(
                new Type[]{type}, null, List.class
        );

        try (JSONReader reader = JSONReader.ofJSONB(jsonbBytes, features)) {
            reader.context.config(features);
            List<T> list = reader.read(paramType);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            return list;
        }
    }

    static <T> List<T> parseArray(byte[] jsonbBytes, Type... types) {
        if (jsonbBytes == null || jsonbBytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.ofJSONB(jsonbBytes)) {
            List<T> list = reader.readList(types);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            return list;
        }
    }

    static <T> List<T> parseArray(byte[] jsonbBytes, Type[] types, JSONReader.Feature... features) {
        if (jsonbBytes == null || jsonbBytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.ofJSONB(jsonbBytes, features)) {
            reader.context.config(features);
            List<T> list = reader.readList(types);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            return list;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, Class<T> objectClass) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context ctx = new JSONReader.Context(provider);
        try (JSONReader jsonReader = UNSAFE_SUPPORT
                ? new JSONReaderJSONBUF(
                ctx,
                jsonbBytes,
                0,
                jsonbBytes.length)
                : new JSONReaderJSONB(
                ctx,
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            Object object;
            if (objectClass == Object.class) {
                object = jsonReader.readAny();
            } else {
                ObjectReader objectReader = provider.getObjectReader(objectClass);
                object = objectReader.readJSONBObject(jsonReader, null, null, 0);
            }

            if (jsonReader.resolveTasks != null) {
                jsonReader.handleResolveTasks(object);
            }
            return (T) object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, Type objectType) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context ctx = new JSONReader.Context(provider);
        JSONReader jsonReader = new JSONReaderJSONB(
                ctx,
                jsonbBytes,
                0,
                jsonbBytes.length);

        ObjectReader objectReader = provider.getObjectReader(objectType);

        T object = (T) objectReader.readJSONBObject(jsonReader, null, null, 0);
        if (jsonReader.resolveTasks != null) {
            jsonReader.handleResolveTasks(object);
        }
        return object;
    }

    static <T> T parseObject(byte[] jsonbBytes, Type... types) {
        return parseObject(jsonbBytes, new MultiType(types));
    }

    static <T> T parseObject(byte[] jsonbBytes, Type objectType, SymbolTable symbolTable) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context ctx = new JSONReader.Context(provider, symbolTable);
        JSONReader reader = new JSONReaderJSONB(
                ctx,
                jsonbBytes,
                0,
                jsonbBytes.length);

        ObjectReader objectReader = provider.getObjectReader(objectType);

        T object = (T) objectReader.readJSONBObject(reader, null, null, 0);
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static <T> T parseObject(byte[] jsonbBytes,
                             Type objectType,
                             SymbolTable symbolTable,
                             JSONReader.Feature... features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context ctx = new JSONReader.Context(provider, symbolTable);

        try (JSONReader reader = UNSAFE_SUPPORT
                ? new JSONReaderJSONBUF(
                ctx,
                jsonbBytes,
                0,
                jsonbBytes.length)
                : new JSONReaderJSONB(
                ctx,
                jsonbBytes,
                0,
                jsonbBytes.length)) {
            for (JSONReader.Feature feature : features) {
                ctx.features |= feature.mask;
            }

            boolean fieldBased = (ctx.features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader objectReader = provider.getObjectReader(objectType, fieldBased);

            T object = (T) objectReader.readJSONBObject(reader, null, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(
            byte[] jsonbBytes,
            Class<T> objectClass,
            Filter filter,
            JSONReader.Feature... features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider);
        context.config(filter, features);

        try (JSONReader jsonReader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            for (JSONReader.Feature feature : features) {
                context.features |= feature.mask;
            }

            Object object;
            if (objectClass == Object.class) {
                ObjectReader autoTypeObjectReader;
                byte type = jsonReader.getType();
                if (type == BC_TYPED_ANY) {
                    autoTypeObjectReader = jsonReader.checkAutoType(Object.class, 0, 0);
                    object = autoTypeObjectReader.readJSONBObject(jsonReader, null, null, context.features);
                } else {
                    object = jsonReader.readAny();
                }
            } else {
                boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
                ObjectReader objectReader = provider.getObjectReader(objectClass, fieldBased);
                object = objectReader.readJSONBObject(jsonReader, null, null, 0);
            }

            if (jsonReader.resolveTasks != null) {
                jsonReader.handleResolveTasks(object);
            }
            return (T) object;
        }
    }

    static <T> T parseObject(
            byte[] jsonbBytes,
            Type objectType,
            SymbolTable symbolTable,
            Filter[] filters,
            JSONReader.Feature... features) {
        if (jsonbBytes == null || jsonbBytes.length == 0) {
            return null;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider, symbolTable);
        context.config(filters, features);

        try (JSONReader jsonReader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            for (JSONReader.Feature feature : features) {
                context.features |= feature.mask;
            }

            Object object;
            if (objectType == Object.class) {
                ObjectReader autoTypeObjectReader;
                byte type = jsonReader.getType();
                if (type == BC_TYPED_ANY) {
                    autoTypeObjectReader = jsonReader.checkAutoType(Object.class, 0, 0);
                    object = autoTypeObjectReader.readJSONBObject(jsonReader, null, null, context.features);
                } else {
                    object = jsonReader.readAny();
                }
            } else {
                boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
                ObjectReader objectReader = provider.getObjectReader(objectType, fieldBased);
                object = objectReader.readJSONBObject(jsonReader, null, null, 0);
            }

            if (jsonReader.resolveTasks != null) {
                jsonReader.handleResolveTasks(object);
            }
            return (T) object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, TypeReference typeReference, JSONReader.Feature... features) {
        return parseObject(jsonbBytes, typeReference.getType(), features);
    }

    static <T> T parseObject(
            InputStream in,
            int length,
            Type objectType,
            JSONReader.Context context
    ) throws IOException {
        int cachedIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_SIZE - 1);
        byte[] bytes = JSONFactory.allocateByteArray(cachedIndex);
        try {
            if (bytes.length < length) {
                bytes = new byte[length];
            }
            int read = in.read(bytes, 0, length);
            if (read != length) {
                throw new IllegalArgumentException("deserialize failed. expected read length: " + length + " but actual read: " + read);
            }

            return parseObject(bytes, 0, length, objectType, context);
        } finally {
            JSONFactory.releaseByteArray(cachedIndex, bytes);
        }
    }

    static <T> T parseObject(
            InputStream in,
            int length,
            Type objectType,
            JSONReader.Feature... features
    ) throws IOException {
        int cachedIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_SIZE - 1);
        byte[] bytes = JSONFactory.allocateByteArray(cachedIndex);
        try {
            if (bytes.length < length) {
                bytes = new byte[length];
            }
            int read = in.read(bytes, 0, length);
            if (read != length) {
                throw new IllegalArgumentException("deserialize failed. expected read length: " + length + " but actual read: " + read);
            }

            return parseObject(bytes, 0, length, objectType, features);
        } finally {
            JSONFactory.releaseByteArray(cachedIndex, bytes);
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, Class<T> objectClass, JSONReader.Feature... features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context ctx = new JSONReader.Context(provider);

        try (JSONReader jsonReader = UNSAFE_SUPPORT
                ? new JSONReaderJSONBUF(
                ctx,
                jsonbBytes,
                0,
                jsonbBytes.length)
                : new JSONReaderJSONB(
                ctx,
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            for (JSONReader.Feature feature : features) {
                ctx.features |= feature.mask;
            }

            Object object;
            if (objectClass == Object.class) {
                ObjectReader autoTypeObjectReader;
                byte type = jsonReader.getType();
                if (type == BC_TYPED_ANY) {
                    autoTypeObjectReader = jsonReader.checkAutoType(Object.class, 0, 0);
                    object = autoTypeObjectReader.readJSONBObject(jsonReader, null, null, ctx.features);
                } else {
                    object = jsonReader.readAny();
                }
            } else {
                boolean fieldBased = (ctx.features & JSONReader.Feature.FieldBased.mask) != 0;
                ObjectReader objectReader = provider.getObjectReader(objectClass, fieldBased);
                if ((ctx.features & JSONReader.Feature.SupportArrayToBean.mask) != 0
                        && jsonReader.isArray()
                        && objectReader instanceof ObjectReaderBean
                ) {
                    object = objectReader.readArrayMappingJSONBObject(jsonReader, null, null, 0);
                } else {
                    object = objectReader.readJSONBObject(jsonReader, null, null, 0);
                }
            }

            if (jsonReader.resolveTasks != null) {
                jsonReader.handleResolveTasks(object);
            }
            return (T) object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, Class<T> objectClass, JSONReader.Context context) {
        try (JSONReader jsonReader = UNSAFE_SUPPORT
                ? new JSONReaderJSONBUF(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length)
                : new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            Object object;
            if (objectClass == Object.class) {
                ObjectReader autoTypeObjectReader;
                byte type = jsonReader.getType();
                if (type == BC_TYPED_ANY) {
                    autoTypeObjectReader = jsonReader.checkAutoType(Object.class, 0, 0);
                    object = autoTypeObjectReader.readJSONBObject(jsonReader, null, null, context.features);
                } else {
                    object = jsonReader.readAny();
                }
            } else {
                boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
                ObjectReader objectReader = context.provider.getObjectReader(objectClass, fieldBased);
                if ((context.features & JSONReader.Feature.SupportArrayToBean.mask) != 0
                        && jsonReader.isArray()
                        && objectReader instanceof ObjectReaderBean
                ) {
                    object = objectReader.readArrayMappingJSONBObject(jsonReader, null, null, 0);
                } else {
                    object = objectReader.readJSONBObject(jsonReader, null, null, 0);
                }
            }

            if (jsonReader.resolveTasks != null) {
                jsonReader.handleResolveTasks(object);
            }
            return (T) object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, Type objectClass, JSONReader.Feature... features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context ctx = new JSONReader.Context(provider);
        JSONReader reader = new JSONReaderJSONB(
                ctx,
                jsonbBytes,
                0,
                jsonbBytes.length);

        for (JSONReader.Feature feature : features) {
            ctx.features |= feature.mask;
        }

        boolean fieldBased = (ctx.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = provider.getObjectReader(objectClass, fieldBased);

        T object = (T) objectReader.readJSONBObject(reader, null, null, 0);
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static <T> T parseObject(byte[] jsonbBytes, int off, int len, Class<T> objectClass) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();

        JSONReader.Context ctx = new JSONReader.Context(provider);

        JSONReader reader = new JSONReaderJSONB(
                ctx,
                jsonbBytes,
                off,
                len);

        boolean fieldBased = (ctx.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = provider.getObjectReader(objectClass, fieldBased);

        T object = (T) objectReader.readJSONBObject(reader, null, null, 0);
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static <T> T parseObject(byte[] jsonbBytes, int off, int len, Type objectClass) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();

        JSONReader.Context ctx = new JSONReader.Context(provider);

        JSONReader reader = new JSONReaderJSONB(
                ctx,
                jsonbBytes,
                off,
                len);

        boolean fieldBased = (ctx.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = provider.getObjectReader(objectClass, fieldBased);

        T object = (T) objectReader.readJSONBObject(reader, null, null, 0);
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static <T> T parseObject(byte[] jsonbBytes,
                             int off,
                             int len,
                             Class<T> objectClass,
                             JSONReader.Feature... features
    ) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();

        JSONReader.Context ctx = new JSONReader.Context(provider);

        for (JSONReader.Feature feature : features) {
            ctx.features |= feature.mask;
        }

        JSONReader reader = new JSONReaderJSONB(
                ctx,
                jsonbBytes,
                off,
                len);

        boolean fieldBased = (ctx.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = provider.getObjectReader(objectClass, fieldBased);

        T object = (T) objectReader.readJSONBObject(reader, null, null, 0);
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static <T> T parseObject(
            byte[] jsonbBytes,
            int off,
            int len,
            Type objectType,
            JSONReader.Context ctx
    ) {
        try (JSONReader reader = new JSONReaderJSONB(ctx, jsonbBytes, off, len)) {
            boolean fieldBased = (ctx.features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader objectReader = ctx.provider.getObjectReader(objectType, fieldBased);

            T object = (T) objectReader.readJSONBObject(reader, null, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, int off, int len, Type objectType, JSONReader.Feature... features) {
        try (JSONReader reader = JSONReader.ofJSONB(jsonbBytes, off, len)) {
            reader.getContext().config(features);
            ObjectReader objectReader = reader.getObjectReader(objectType);

            T object = (T) objectReader.readJSONBObject(reader, null, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, int off, int len, Class<T> objectClass, SymbolTable symbolTable) {
        JSONReader reader = JSONReader.ofJSONB(jsonbBytes, off, len, symbolTable);
        ObjectReader objectReader = reader.getObjectReader(objectClass);

        T object = (T) objectReader.readJSONBObject(reader, null, null, 0);
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static <T> T parseObject(byte[] jsonbBytes, int off, int len, Type objectClass, SymbolTable symbolTable) {
        JSONReader reader = JSONReader.ofJSONB(jsonbBytes, off, len, symbolTable);
        ObjectReader objectReader = reader.getObjectReader(objectClass);

        T object = (T) objectReader.readJSONBObject(reader, null, null, 0);
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static <T> T parseObject(byte[] jsonbBytes,
                             int off,
                             int len,
                             Class<T> objectClass,
                             SymbolTable symbolTable,
                             JSONReader.Feature... features) {
        JSONReader reader = JSONReader.ofJSONB(jsonbBytes, off, len, symbolTable);
        reader.getContext().config(features);
        ObjectReader objectReader = reader.getObjectReader(objectClass);

        T object = (T) objectReader.readJSONBObject(reader, null, null, 0);
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static <T> T parseObject(byte[] jsonbBytes,
                             int off,
                             int len,
                             Type objectClass,
                             SymbolTable symbolTable,
                             JSONReader.Feature... features) {
        JSONReader reader = JSONReader.ofJSONB(jsonbBytes, off, len, symbolTable);
        reader.getContext().config(features);
        ObjectReader objectReader = reader.getObjectReader(objectClass);

        T object = (T) objectReader.readJSONBObject(reader, null, null, 0);
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static byte[] toBytes(String str) {
        if (str == null) {
            return new byte[]{BC_NULL};
        }

        if (JVM_VERSION == 8) {
            char[] chars = JDKUtils.getCharArray(str);
            int strlen = chars.length;
            if (strlen <= STR_ASCII_FIX_LEN) {
                boolean ascii = true;
                for (int i = 0; i < strlen; ++i) {
                    if (chars[i] > 0x007F) {
                        ascii = false;
                        break;
                    }
                }

                if (ascii) {
                    byte[] bytes = new byte[chars.length + 1];
                    bytes[0] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
                    for (int i = 0; i < strlen; ++i) {
                        bytes[i + 1] = (byte) chars[i];
                    }
                    return bytes;
                }
            }
        } else if (UNSAFE_SUPPORT) {
            int coder = UnsafeUtils.getStringCoder(str);
            if (coder == 0) {
                byte[] value = UnsafeUtils.getStringValue(str);
                int strlen = value.length;
                if (strlen <= STR_ASCII_FIX_LEN) {
                    byte[] bytes = new byte[value.length + 1];
                    bytes[0] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
                    System.arraycopy(value, 0, bytes, 1, value.length);
                    return bytes;
                }
            }
        }

        try (JSONWriter writer = new JSONWriterJSONB(
                new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider),
                null
        )) {
            writer.writeString(str);
            return writer.getBytes();
        }
    }

    static byte[] toBytes(String str, Charset charset) {
        if (str == null) {
            return new byte[]{BC_NULL};
        }

        final byte type;
        if (charset == StandardCharsets.UTF_16) {
            type = BC_STR_UTF16;
        } else if (charset == StandardCharsets.UTF_16BE) {
            type = BC_STR_UTF16BE;
        } else if (charset == StandardCharsets.UTF_16LE) {
            type = BC_STR_UTF16LE;
        } else if (charset != null && "GB18030".equals(charset.name())) { // GraalVM support
            type = BC_STR_GB18030;
        } else {
            return toBytes(str);
        }

        byte[] utf16 = str.getBytes(charset);
        int byteslen = 2 + utf16.length;
        if (utf16.length <= BC_INT32_NUM_MAX) {
            byteslen += 0;
        } else if (utf16.length <= INT32_BYTE_MAX) {
            byteslen += 1;
        } else if (utf16.length <= INT32_SHORT_MAX) {
            byteslen += 2;
        } else {
            byteslen += 4;
        }

        byte[] bytes = new byte[byteslen];
        bytes[0] = type;
        int off = 1;
        off += writeInt(bytes, off, utf16.length);
        System.arraycopy(utf16, 0, bytes, off, utf16.length);
        return bytes;
    }

    static byte[] toBytes(Object object) {
        try (JSONWriter writer = new JSONWriterJSONB(
                new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider),
                null
        )) {
            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                JSONWriter.Context context = writer.context;
                boolean fieldBased = (context.features & JSONWriter.Feature.FieldBased.mask) != 0;
                ObjectWriter objectWriter = context.provider.getObjectWriter(valueClass, valueClass, fieldBased);
                objectWriter.writeJSONB(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    static byte[] toBytes(Object object, JSONWriter.Context context) {
        if (context == null) {
            context = JSONFactory.createWriteContext();
        }

        try (JSONWriter writer = new JSONWriterJSONB(context, null)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                boolean fieldBased = (context.features & JSONWriter.Feature.FieldBased.mask) != 0;

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.provider.getObjectWriter(valueClass, valueClass, fieldBased);
                if ((context.features & JSONWriter.Feature.BeanToArray.mask) != 0) {
                    objectWriter.writeArrayMappingJSONB(writer, object, null, null, 0);
                } else {
                    objectWriter.writeJSONB(writer, object, null, null, 0);
                }
            }
            return writer.getBytes();
        }
    }

    static byte[] toBytes(Object object, SymbolTable symbolTable) {
        try (JSONWriter writer = new JSONWriterJSONB(
                new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider),
                symbolTable
        )) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.writeJSONB(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    static byte[] toBytes(Object object, SymbolTable symbolTable, JSONWriter.Feature... features) {
        try (JSONWriter writer = new JSONWriterJSONB(
                new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features),
                symbolTable
        )) {
            JSONWriter.Context ctx = writer.context;

            ctx.config(features);

            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();

                boolean fieldBased = (ctx.features & JSONWriter.Feature.FieldBased.mask) != 0;

                ObjectWriter objectWriter = ctx.provider.getObjectWriter(valueClass, valueClass, fieldBased);
                if ((ctx.features & JSONWriter.Feature.BeanToArray.mask) != 0) {
                    objectWriter.writeArrayMappingJSONB(writer, object, null, null, 0);
                } else {
                    objectWriter.writeJSONB(writer, object, null, null, 0);
                }
            }
            return writer.getBytes();
        }
    }

    static byte[] toBytes(Object object, SymbolTable symbolTable, Filter[] filters, JSONWriter.Feature... features) {
        try (JSONWriter writer = new JSONWriterJSONB(
                new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features),
                symbolTable
        )) {
            JSONWriter.Context ctx = writer.context;

            ctx.config(features);
            ctx.configFilter(filters);

            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();

                boolean fieldBased = (ctx.features & JSONWriter.Feature.FieldBased.mask) != 0;

                ObjectWriter objectWriter = ctx.provider.getObjectWriter(valueClass, valueClass, fieldBased);
                if ((ctx.features & JSONWriter.Feature.BeanToArray.mask) != 0) {
                    objectWriter.writeArrayMappingJSONB(writer, object, null, null, 0);
                } else {
                    objectWriter.writeJSONB(writer, object, null, null, 0);
                }
            }
            return writer.getBytes();
        }
    }

    static byte[] toBytes(Object object, JSONWriter.Feature... features) {
        try (JSONWriter writer = new JSONWriterJSONB(
                new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features),
                null
        )) {
            JSONWriter.Context context = writer.context;

            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                boolean fieldBased = (context.features & JSONWriter.Feature.FieldBased.mask) != 0;

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.provider.getObjectWriter(valueClass, valueClass, fieldBased);
                if ((context.features & JSONWriter.Feature.BeanToArray.mask) != 0) {
                    objectWriter.writeArrayMappingJSONB(writer, object, null, null, 0);
                } else {
                    objectWriter.writeJSONB(writer, object, null, null, 0);
                }
            }
            return writer.getBytes();
        }
    }

    static SymbolTable symbolTable(String... names) {
        return new JSONFactory.SymbolTableImpl(names);
    }

    static String toJSONString(byte[] jsonbBytes) {
        return new JSONBDump(jsonbBytes, false)
                .toString();
    }

    static String toJSONString(byte[] jsonbBytes, SymbolTable symbolTable) {
        return new JSONBDump(jsonbBytes, symbolTable, false)
                .toString();
    }

    static int writeTo(
            OutputStream out,
            Object object,
            JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.ofJSONB()) {
            writer.config(features);

            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.writeJSONB(writer, object, null, null, 0);
            }

            int len = writer.flushTo(out);
            return len;
        } catch (IOException e) {
            throw new JSONException("writeJSONString error", e);
        }
    }

    static byte[] fromJSONString(String str) {
        return JSONB.toBytes(JSON.parse(str));
    }

    static byte[] fromJSONBytes(byte[] jsonUtf8Bytes) {
        JSONReader reader = JSONReader.of(jsonUtf8Bytes);
        ObjectReader objectReader = reader.getObjectReader(Object.class);
        Object object = objectReader.readObject(reader, null, null, 0);
        return JSONB.toBytes(object);
    }

    static String typeName(byte type) {
        switch (type) {
            case BC_OBJECT:
                return "OBJECT " + Integer.toString(type);
            case BC_OBJECT_END:
                return "OBJECT_END " + Integer.toString(type);
            case BC_REFERENCE:
                return "REFERENCE " + Integer.toString(type);
            case BC_SYMBOL:
                return "SYMBOL " + Integer.toString(type);
            case BC_NULL:
                return "NULL " + Integer.toString(type);
            case BC_TRUE:
                return "TRUE " + Integer.toString(type);
            case BC_FALSE:
                return "FALSE " + Integer.toString(type);
            case BC_STR_UTF8:
                return "STR_UTF8 " + Integer.toString(type);
            case BC_STR_UTF16:
                return "STR_UTF16 " + Integer.toString(type);
            case BC_STR_UTF16LE:
                return "STR_UTF16LE " + Integer.toString(type);
            case BC_STR_UTF16BE:
                return "STR_UTF16BE " + Integer.toString(type);
            case BC_INT8:
                return "INT8 " + Integer.toString(type);
            case BC_INT16:
                return "INT16 " + Integer.toString(type);
            case BC_INT32:
                return "INT32 " + Integer.toString(type);
            case BC_INT64:
            case BC_INT64_INT:
                return "INT64 " + Integer.toString(type);
            case BC_FLOAT:
            case BC_FLOAT_INT:
                return "FLOAT " + Integer.toString(type);
            case BC_DOUBLE:
            case BC_DOUBLE_LONG:
            case BC_DOUBLE_NUM_0:
            case BC_DOUBLE_NUM_1:
                return "DOUBLE " + Integer.toString(type);
            case BC_BIGINT:
            case BC_BIGINT_LONG:
                return "BIGINT " + Integer.toString(type);
            case BC_DECIMAL:
            case BC_DECIMAL_LONG:
                return "DECIMAL " + Integer.toString(type);
            case Constants.BC_LOCAL_TIME:
                return "LOCAL_TIME " + Integer.toString(type);
            case BC_BINARY:
                return "BINARY " + Integer.toString(type);
            case Constants.BC_LOCAL_DATETIME:
                return "LOCAL_DATETIME " + Integer.toString(type);
            case BC_TIMESTAMP:
                return "TIMESTAMP " + Integer.toString(type);
            case BC_TIMESTAMP_MINUTES:
                return "TIMESTAMP_MINUTES " + Integer.toString(type);
            case BC_TIMESTAMP_SECONDS:
                return "TIMESTAMP_SECONDS " + Integer.toString(type);
            case BC_TIMESTAMP_MILLIS:
                return "TIMESTAMP_MILLIS " + Integer.toString(type);
            case BC_TIMESTAMP_WITH_TIMEZONE:
                return "TIMESTAMP_WITH_TIMEZONE " + Integer.toString(type);
            case Constants.BC_LOCAL_DATE:
                return "LOCAL_DATE " + Integer.toString(type);
            case BC_TYPED_ANY:
                return "TYPED_ANY " + Integer.toString(type);
            default:
                if (type >= BC_ARRAY_FIX_MIN && type <= BC_ARRAY) {
                    return "ARRAY " + Integer.toString(type);
                }

                if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII) {
                    return "STR_ASCII " + Integer.toString(type);
                }

                if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
                    return "INT32 " + Integer.toString(type);
                }

                if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
                    return "INT32 " + Integer.toString(type);
                }

                if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
                    return "INT32 " + Integer.toString(type);
                }

                if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
                    return "INT64 " + Integer.toString(type);
                }

                if (type >= BC_INT64_BYTE_MIN && type <= BC_INT64_BYTE_MAX) {
                    return "INT64 " + Integer.toString(type);
                }

                if (type >= BC_INT64_SHORT_MIN && type <= BC_INT64_SHORT_MAX) {
                    return "INT64 " + Integer.toString(type);
                }

                return Integer.toString(type);
        }
    }
}
