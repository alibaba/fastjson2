package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.internal.trove.map.hash.TLongIntHashMap;
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
import java.time.*;
import java.util.*;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONWriter.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.util.IOUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;

/**
 * x90          # type_char int
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
    static void dump(byte[] jsonbBytes) {
        System.out.println(
                JSONB.toJSONString(jsonbBytes, true)
        );
    }

    static void dump(byte[] jsonbBytes, SymbolTable symbolTable) {
        JSONBDump dump = new JSONBDump(jsonbBytes, symbolTable, true);
        String str = dump.toString();
        System.out.println(str);
    }

    interface Constants {
        byte BC_CHAR = -112;                    // 0x90
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

        byte BC_INT64_SHORT_MIN = -64;  // 0xc0
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
        byte BC_INT32_BYTE_ZERO = 56;   // 0x38
        byte BC_INT32_BYTE_MAX = 63;    // 0x3f

        byte BC_INT32_SHORT_MIN = 64; // 0x40
        byte BC_INT32_SHORT_ZERO = 68;
        byte BC_INT32_SHORT_MAX = 71; // 0x47
        byte BC_INT32 = 72; // 0x48

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

    static byte[] toBytes(byte i) {
        try (JSONWriter jsonWriter = JSONWriter.ofJSONB()) {
            jsonWriter.writeInt8(i);
            return jsonWriter.getBytes();
        }
    }

    static byte[] toBytes(short i) {
        try (JSONWriter jsonWriter = JSONWriter.ofJSONB()) {
            jsonWriter.writeInt16(i);
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

    /**
     * @param jsonbBytes
     * @param context
     * @return
     * @since 2.0.46
     */
    static Object parse(byte[] jsonbBytes, JSONReader.Context context) {
        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            Object object = reader.readAnyObject();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static Object parse(byte[] jsonbBytes, JSONReader.Feature... features) {
        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                new JSONReader.Context(getDefaultObjectReaderProvider(), features),
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            Object object = reader.readAnyObject();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static Object parse(InputStream in, JSONReader.Context context) {
        try (JSONReaderJSONB reader = new JSONReaderJSONB(context, in)) {
            Object object = reader.readAny();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static Object parse(byte[] jsonbBytes, SymbolTable symbolTable, JSONReader.Feature... features) {
        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                new JSONReader.Context(getDefaultObjectReaderProvider(), symbolTable, features),
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            Object object = reader.readAnyObject();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static JSONObject parseObject(byte[] jsonbBytes) {
        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider()),
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            JSONObject object = (JSONObject) reader.readObject();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static JSONObject parseObject(byte[] jsonbBytes, JSONReader.Feature... features) {
        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider(), features),
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            JSONObject object = (JSONObject) reader.readObject();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static JSONObject parseObject(InputStream in, JSONReader.Context context) {
        try (JSONReaderJSONB reader = new JSONReaderJSONB(context, in)) {
            JSONObject object = (JSONObject) reader.readObject();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static JSONArray parseArray(byte[] jsonbBytes) {
        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider()),
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            JSONArray array = (JSONArray) reader.readArray();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            return array;
        }
    }

    static JSONArray parseArray(InputStream in, JSONReader.Context context) {
        try (JSONReaderJSONB reader = new JSONReaderJSONB(context, in)) {
            JSONArray array = (JSONArray) reader.readArray();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            return array;
        }
    }

    static <T> List<T> parseArray(byte[] jsonbBytes, Type type) {
        if (jsonbBytes == null || jsonbBytes.length == 0) {
            return null;
        }

        Type paramType = new ParameterizedTypeImpl(
                new Type[]{type}, null, List.class
        );

        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider()),
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
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

        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider(), features),
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
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

        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider()),
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
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

        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider(), features),
                jsonbBytes,
                0, jsonbBytes.length)
        ) {
            List<T> list = reader.readList(types);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            return list;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, Class<T> objectClass) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        try (JSONReaderJSONB jsonReader = new JSONReaderJSONB(
                new JSONReader.Context(provider),
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            Object object;
            if (objectClass == Object.class) {
                object = jsonReader.readAny();
            } else {
                ObjectReader objectReader = provider.getObjectReader(
                        objectClass,
                        (defaultReaderFeatures & JSONReader.Feature.FieldBased.mask) != 0
                );
                object = objectReader.readJSONBObject(jsonReader, objectClass, null, 0);
            }

            if (jsonReader.resolveTasks != null) {
                jsonReader.handleResolveTasks(object);
            }
            return (T) object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, Type objectType) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(objectType);

        try (JSONReaderJSONB jsonReader = new JSONReaderJSONB(
                new JSONReader.Context(provider),
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            T object = (T) objectReader.readJSONBObject(jsonReader, objectType, null, 0);
            if (jsonReader.resolveTasks != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, Type... types) {
        return parseObject(jsonbBytes, new MultiType(types));
    }

    static <T> T parseObject(byte[] jsonbBytes, Type objectType, SymbolTable symbolTable) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(objectType);

        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                new JSONReader.Context(provider, symbolTable),
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            T object = (T) objectReader.readJSONBObject(reader, objectType, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(
            byte[] jsonbBytes,
            Type objectType,
            SymbolTable symbolTable,
            JSONReader.Feature... features
    ) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider, symbolTable, features);
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = provider.getObjectReader(objectType, fieldBased);

        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            T object = (T) objectReader.readJSONBObject(reader, objectType, null, 0);
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
            JSONReader.Feature... features
    ) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider, filter, features);

        try (JSONReaderJSONB jsonReader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            for (int i = 0; i < features.length; i++) {
                context.features |= features[i].mask;
            }

            Object object;
            if (objectClass == Object.class) {
                object = jsonReader.readAnyObject();
            } else {
                boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
                ObjectReader objectReader = provider.getObjectReader(objectClass, fieldBased);
                object = objectReader.readJSONBObject(jsonReader, objectClass, null, 0);
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
        JSONReader.Context context = new JSONReader.Context(provider, symbolTable, filters, features);

        try (JSONReaderJSONB jsonReader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            for (int i = 0; i < features.length; i++) {
                context.features |= features[i].mask;
            }

            Object object;
            if (objectType == Object.class) {
                object = jsonReader.readAnyObject();
            } else {
                boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
                ObjectReader objectReader = provider.getObjectReader(objectType, fieldBased);
                object = objectReader.readJSONBObject(jsonReader, objectType, null, 0);
            }

            if (jsonReader.resolveTasks != null) {
                jsonReader.handleResolveTasks(object);
            }
            return (T) object;
        }
    }

    /**
     * @since 2.0.30
     */
    static <T> T copy(T object, JSONWriter.Feature... features) {
        return JSON.copy(object, features);
    }

    static <T> T parseObject(byte[] jsonbBytes, TypeReference typeReference, JSONReader.Feature... features) {
        return parseObject(jsonbBytes, typeReference.getType(), features);
    }

    /**
     * @since 2.0.30
     */
    static <T> T parseObject(
            InputStream in,
            Class objectClass,
            JSONReader.Feature... features
    ) throws IOException {
        return parseObject(in, objectClass, JSONFactory.createReadContext(features));
    }

    /**
     * @since 2.0.30
     */
    static <T> T parseObject(
            InputStream in,
            Type objectType,
            JSONReader.Feature... features
    ) throws IOException {
        return parseObject(in, objectType, JSONFactory.createReadContext(features));
    }

    /**
     * @since 2.0.30
     */
    static <T> T parseObject(
            InputStream in,
            Type objectType,
            JSONReader.Context context
    ) {
        try (JSONReaderJSONB jsonReader = new JSONReaderJSONB(context, in)
        ) {
            Object object;
            if (objectType == Object.class) {
                object = jsonReader.readAny();
            } else {
                ObjectReader objectReader = context.getObjectReader(objectType);
                object = objectReader.readJSONBObject(jsonReader, objectType, null, 0);
            }

            if (jsonReader.resolveTasks != null) {
                jsonReader.handleResolveTasks(object);
            }
            return (T) object;
        }
    }

    /**
     * @since 2.0.30
     */
    static <T> T parseObject(
            InputStream in,
            Class objectClass,
            JSONReader.Context context
    ) {
        try (JSONReaderJSONB jsonReader = new JSONReaderJSONB(context, in)) {
            Object object;
            if (objectClass == Object.class) {
                object = jsonReader.readAny();
            } else {
                ObjectReader objectReader = context.getObjectReader(objectClass);
                object = objectReader.readJSONBObject(jsonReader, objectClass, null, 0);
            }

            if (jsonReader.resolveTasks != null) {
                jsonReader.handleResolveTasks(object);
            }
            return (T) object;
        }
    }

    static <T> T parseObject(
            InputStream in,
            int length,
            Type objectType,
            JSONReader.Context context
    ) throws IOException {
        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        final CacheItem cacheItem = CACHE_ITEMS[cacheIndex];
        byte[] bytes = BYTES_UPDATER.getAndSet(cacheItem, null);
        if (bytes == null) {
            bytes = new byte[8192];
        }

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
            BYTES_UPDATER.lazySet(cacheItem, bytes);
        }
    }

    static <T> T parseObject(
            InputStream in,
            int length,
            Type objectType,
            JSONReader.Feature... features
    ) throws IOException {
        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        final CacheItem cacheItem = CACHE_ITEMS[cacheIndex];
        byte[] bytes = BYTES_UPDATER.getAndSet(cacheItem, null);
        if (bytes == null) {
            bytes = new byte[8192];
        }
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
            BYTES_UPDATER.lazySet(cacheItem, bytes);
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, Class<T> objectClass, JSONReader.Feature... features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider, features);

        try (JSONReaderJSONB jsonReader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            Object object;
            if (objectClass == Object.class) {
                object = jsonReader.readAnyObject();
            } else {
                boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
                ObjectReader objectReader = provider.getObjectReader(objectClass, fieldBased);
                if ((context.features & JSONReader.Feature.SupportArrayToBean.mask) != 0
                        && jsonReader.isArray()
                        && objectReader instanceof ObjectReaderBean
                ) {
                    object = objectReader.readArrayMappingJSONBObject(jsonReader, objectClass, null, 0);
                } else {
                    object = objectReader.readJSONBObject(jsonReader, objectClass, null, 0);
                }
            }

            if (jsonReader.resolveTasks != null) {
                jsonReader.handleResolveTasks(object);
            }
            return (T) object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, Class<T> objectClass, JSONReader.Context context) {
        try (JSONReaderJSONB jsonReader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length)
        ) {
            Object object;
            if (objectClass == Object.class) {
                object = jsonReader.readAnyObject();
            } else {
                ObjectReader objectReader = context.provider.getObjectReader(
                        objectClass,
                        (context.features & JSONReader.Feature.FieldBased.mask) != 0
                );
                if ((context.features & JSONReader.Feature.SupportArrayToBean.mask) != 0
                        && jsonReader.isArray()
                        && objectReader instanceof ObjectReaderBean
                ) {
                    object = objectReader.readArrayMappingJSONBObject(jsonReader, objectClass, null, 0);
                } else {
                    object = objectReader.readJSONBObject(jsonReader, objectClass, null, 0);
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
        JSONReader.Context context = new JSONReader.Context(provider, features);

        ObjectReader objectReader = provider.getObjectReader(
                objectClass,
                (context.features & JSONReader.Feature.FieldBased.mask) != 0
        );

        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length)) {
            T object = (T) objectReader.readJSONBObject(reader, objectClass, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, int off, int len, Class<T> objectClass) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider);
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = provider.getObjectReader(objectClass, fieldBased);

        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                off,
                len)
        ) {
            T object = (T) objectReader.readJSONBObject(reader, objectClass, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, int off, int len, Type type) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider);
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = provider.getObjectReader(type, fieldBased);

        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                off,
                len)
        ) {
            T object = (T) objectReader.readJSONBObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(
            byte[] jsonbBytes,
            int off,
            int len,
            Class<T> objectClass,
            JSONReader.Feature... features
    ) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider, features);
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = provider.getObjectReader(objectClass, fieldBased);

        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                off,
                len)
        ) {
            T object = (T) objectReader.readJSONBObject(reader, objectClass, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(
            byte[] jsonbBytes,
            int off,
            int len,
            Type objectType,
            JSONReader.Context context
    ) {
        try (JSONReaderJSONB reader = new JSONReaderJSONB(context, jsonbBytes, off, len)) {
            boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader objectReader = context.provider.getObjectReader(objectType, fieldBased);

            T object = (T) objectReader.readJSONBObject(reader, objectType, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, int off, int len, Type objectType, JSONReader.Feature... features) {
        JSONReader.Context context = createReadContext(features);
        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                off,
                len)
        ) {
            ObjectReader objectReader = reader.getObjectReader(objectType);

            T object = (T) objectReader.readJSONBObject(reader, objectType, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, int off, int len, Class<T> objectClass, SymbolTable symbolTable) {
        JSONReader.Context context = createReadContext(symbolTable);
        ObjectReader objectReader = context.getObjectReader(objectClass);
        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                off,
                len)
        ) {
            T object = (T) objectReader.readJSONBObject(reader, objectClass, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(byte[] jsonbBytes, int off, int len, Type objectClass, SymbolTable symbolTable) {
        JSONReader.Context context = createReadContext(symbolTable);
        ObjectReader objectReader = context.getObjectReader(objectClass);

        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                off,
                len)
        ) {
            T object = (T) objectReader.readJSONBObject(reader, objectClass, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(
            byte[] jsonbBytes,
            int off,
            int len,
            Class<T> objectClass,
            SymbolTable symbolTable,
            JSONReader.Feature... features
    ) {
        JSONReader.Context context = createReadContext(symbolTable, features);
        ObjectReader objectReader = context.getObjectReader(objectClass);

        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                off,
                len)
        ) {
            T object = (T) objectReader.readJSONBObject(reader, objectClass, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    static <T> T parseObject(
            byte[] jsonbBytes,
            int off,
            int len,
            Type objectClass,
            SymbolTable symbolTable,
            JSONReader.Feature... features
    ) {
        JSONReader.Context context = createReadContext(symbolTable, features);
        ObjectReader objectReader = context.getObjectReader(objectClass);

        try (JSONReaderJSONB reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                off,
                len)
        ) {
            T object = (T) objectReader.readJSONBObject(reader, objectClass, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
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
        } else if (STRING_VALUE != null) {
            int coder = STRING_CODER.applyAsInt(str);
            if (coder == 0) {
                byte[] value = STRING_VALUE.apply(str);
                int strlen = value.length;
                if (strlen <= STR_ASCII_FIX_LEN) {
                    byte[] bytes = new byte[value.length + 1];
                    bytes[0] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
                    System.arraycopy(value, 0, bytes, 1, value.length);
                    return bytes;
                }
            }
        }

        try (JSONWriterJSONB writer = new JSONWriterJSONB(
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
        } else if (charset == StandardCharsets.UTF_8) {
            type = BC_STR_UTF8;
        } else if (charset == StandardCharsets.US_ASCII || charset == StandardCharsets.ISO_8859_1) {
            type = BC_STR_ASCII;
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
        int off = IO.writeInt32(bytes, 1, utf16.length);
        System.arraycopy(utf16, 0, bytes, off, utf16.length);
        return bytes;
    }

    static byte[] toBytes(Object object) {
        final JSONWriter.Context context = new JSONWriter.Context(defaultObjectWriterProvider);
        try (JSONWriterJSONB writer = new JSONWriterJSONB(
                context,
                null
        )) {
            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
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

        try (JSONWriterJSONB writer = new JSONWriterJSONB(context, null)) {
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
        JSONWriter.Context context = new JSONWriter.Context(defaultObjectWriterProvider);
        try (JSONWriterJSONB writer = new JSONWriterJSONB(
                context,
                symbolTable
        )) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.writeJSONB(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    static byte[] toBytes(Object object, SymbolTable symbolTable, JSONWriter.Feature... features) {
        return toBytes(object, new Context(), symbolTable, features);
    }

    static byte[] toBytes(Object object, JSONWriter.Context context, SymbolTable symbolTable, JSONWriter.Feature... features) {
        try (JSONWriterJSONB writer = new JSONWriterJSONB(context, symbolTable)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();

                boolean fieldBased = (context.features & JSONWriter.Feature.FieldBased.mask) != 0;

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

    static byte[] toBytes(Object object, SymbolTable symbolTable, Filter[] filters, JSONWriter.Feature... features) {
        JSONWriter.Context context = new JSONWriter.Context(defaultObjectWriterProvider, features);
        context.configFilter(filters);

        try (JSONWriterJSONB writer = new JSONWriterJSONB(
                context,
                symbolTable
        )) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();

                boolean fieldBased = (context.features & JSONWriter.Feature.FieldBased.mask) != 0;

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

    static byte[] toBytes(Object object, JSONWriter.Feature... features) {
        JSONWriter.Context context = new JSONWriter.Context(defaultObjectWriterProvider, features);
        try (JSONWriterJSONB writer = new JSONWriterJSONB(
                context,
                null
        )) {
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
        return new SymbolTable(names);
    }

    static String toJSONString(byte[] jsonbBytes) {
        return new JSONBDump(jsonbBytes, false)
                .toString();
    }

    /**
     * @since 2.0.28
     */
    static String toJSONString(byte[] jsonbBytes, boolean raw) {
        return new JSONBDump(jsonbBytes, raw)
                .toString();
    }

    static String toJSONString(byte[] jsonbBytes, SymbolTable symbolTable) {
        return toJSONString(jsonbBytes, symbolTable, false);
    }

    static String toJSONString(byte[] jsonbBytes, SymbolTable symbolTable, boolean raw) {
        return new JSONBDump(jsonbBytes, symbolTable, raw)
                .toString();
    }

    static int writeTo(
            OutputStream out,
            Object object,
            JSONWriter.Feature... features
    ) {
        try (JSONWriterJSONB writer = new JSONWriterJSONB(
                new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider),
                null)
        ) {
            writer.config(features);

            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.writeJSONB(writer, object, null, null, 0);
            }

            return writer.flushTo(out);
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

    static boolean isInt32(int type) {
        return type >= BC_INT32_NUM_MIN && type <= BC_INT32;
    }

    static boolean isInt32Num(int type) {
        return type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX;
    }

    static boolean isInt32Byte(int type) {
        return (type & 0xF0) == 0x30;
    }

    static boolean isInt32Short(int type) {
        return (type & 0xF8) == 0x40;
    }

    static boolean isInt64Num(int type) {
        return type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX;
    }

    static boolean isInt64Byte(int type) {
        return ((type - BC_INT64_BYTE_MIN) & 0xF0) == 0;
    }

    static boolean isInt64Short(int type) {
        return (type & 0xF8) == 0xC0;
    }

    static boolean isInt32ByteValue(int i) {
        return ((i + 2048) & ~0xFFF) != 0;
    }

    static boolean isInt32ByteValue1(int i) {
        return i >= INT32_BYTE_MIN && i <= INT32_BYTE_MAX;
    }

    interface IO {
        static int enumCapacity(Enum e, long features) {
            if ((features & (MASK_WRITE_ENUM_USING_TO_STRING | MASK_WRITE_ENUMS_USING_NAME)) != 0) {
                return stringCapacity((features & WriteEnumUsingToString.mask) != 0
                        ? e.toString()
                        : e.name());
            }
            return 5;
        }

        static int writeEnum(byte[] bytes, int off, Enum e, long features) {
            if ((features & (MASK_WRITE_ENUM_USING_TO_STRING | MASK_WRITE_ENUMS_USING_NAME)) != 0) {
                return writeString(bytes, off,
                        (features & WriteEnumUsingToString.mask) != 0
                                ? e.toString()
                                : e.name()
                );
            } else {
                return JSONB.IO.writeInt32(bytes, off, e.ordinal());
            }
        }

        static int writeBoolean(byte[] bytes, int off, Boolean value) {
            bytes[off] = value == null ? BC_NULL : value ? BC_TRUE : BC_FALSE;
            return off + 1;
        }

        static int writeBoolean(byte[] bytes, int off, boolean value) {
            bytes[off] = value ? BC_TRUE : BC_FALSE;
            return off + 1;
        }

        static int writeBoolean(byte[] bytes, int off, boolean[] values) {
            if (values == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            off = startArray(bytes, off, values.length);
            for (int i = 0; i < values.length; i++) {
                bytes[off + i] = values[i] ? BC_TRUE : BC_FALSE;
            }
            return off + values.length;
        }

        static int writeFloat(byte[] bytes, int off, Float value, long features) {
            float floatValue;
            if (value == null) {
                if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0) {
                    bytes[off] = BC_NULL;
                    return off + 1;
                }
                floatValue = 0;
            } else {
                floatValue = value;
            }
            return IO.writeFloat(bytes, off, floatValue);
        }

        static int writeFloat(byte[] bytes, int off, float[] values) {
            if (values == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            off = startArray(bytes, off, values.length);
            for (float value : values) {
                off = IO.writeFloat(bytes, off, value);
            }
            return off;
        }

        static int writeFloat(byte[] bytes, int off, float value) {
            int intValue = (int) value;
            if (intValue == value && ((intValue + 0x40000) & ~0x7ffff) == 0) {
                bytes[off] = BC_FLOAT_INT;
                return IO.writeInt32(bytes, off + 1, intValue);
            }

            bytes[off] = BC_FLOAT;
            IOUtils.putIntBE(bytes, off + 1, Float.floatToIntBits(value));
            return off + 5;
        }

        static int writeDouble(byte[] bytes, int off, Double value, long features) {
            if (value == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0
                        ? BC_NULL
                        : BC_DOUBLE_NUM_0;
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0 ? BC_NULL : BC_DOUBLE_NUM_0;
                return off + 1;
            }
            return IO.writeDouble(bytes, off, value);
        }

        static int writeDouble(byte[] bytes, int off, double value) {
            if (value == 0 || value == 1) {
                bytes[off] = value == 0 ? BC_DOUBLE_NUM_0 : BC_DOUBLE_NUM_1;
                return off + 1;
            }

            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                long longValue = (long) value;
                if (longValue == value) {
                    bytes[off] = BC_DOUBLE_LONG;
                    return IO.writeInt64(bytes, off + 1, longValue);
                }
            }

            bytes[off] = BC_DOUBLE;
            IOUtils.putLongBE(bytes, off + 1, Double.doubleToLongBits(value));
            return off + 9;
        }

        static int writeDouble(byte[] bytes, int off, double[] values) {
            if (values == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            off = startArray(bytes, off, values.length);
            for (double value : values) {
                off = writeDouble(bytes, off, value);
            }
            return off;
        }

        static int writeInt8(byte[] bytes, int off, Byte val, long features) {
            if (val == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0 ? BC_NULL : 0;
                return off + 1;
            }
            putShortLE(bytes, off, (short) ((val << 8) | (BC_INT8 & 0xFF)));
            return off + 2;
        }

        static int writeInt8(byte[] bytes, int off, byte val) {
            putShortLE(bytes, off, (short) ((val << 8) | (BC_INT8 & 0xFF)));
            return off + 2;
        }

        static int writeInt16(byte[] bytes, int off, Short val, long features) {
            if (val == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0 ? BC_NULL : 0;
                return off + 1;
            }
            bytes[off] = BC_INT16;
            putShortBE(bytes, off + 1, val);
            return off + 3;
        }

        static int writeInt16(byte[] bytes, int off, short val) {
            bytes[off] = BC_INT16;
            putShortBE(bytes, off + 1, val);
            return off + 3;
        }

        static int writeInt32(byte[] bytes, int off, Integer value, long features) {
            if (value == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0 ? BC_NULL : 0;
                return off + 1;
            }
            return IO.writeInt32(bytes, off, value);
        }

        static int writeSymbol(byte[] bytes, int off, String str, SymbolTable symbolTable) {
            if (str == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            int ordinal = symbolTable.getOrdinal(str);
            if (ordinal >= 0) {
                bytes[off] = BC_STR_ASCII;
                return writeInt32(bytes, off + 1, -ordinal);
            }
            return writeString(bytes, off, str);
        }

        static int writeSymbol(byte[] bytes, int off, int symbol) {
            bytes[off++] = BC_SYMBOL;

            if (symbol >= BC_INT32_NUM_MIN && symbol <= BC_INT32_NUM_MAX) {
                bytes[off++] = (byte) symbol;
            } else if (symbol >= INT32_BYTE_MIN && symbol <= INT32_BYTE_MAX) {
                putShortBE(bytes, off, (short) ((BC_INT32_BYTE_ZERO << 8) + symbol));
                off += 2;
            } else {
                off = JSONB.IO.writeInt32(bytes, off, symbol);
            }
            return off;
        }

        static int checkAndWriteTypeName(byte[] bytes, int off, Object object, Class<?> fieldClass, JSONWriter jsonWriter) {
            long features = jsonWriter.getFeatures();
            Class<?> objectClass;
            if ((features & WriteClassName.mask) == 0
                    || object == null
                    || (objectClass = object.getClass()) == fieldClass
                    || ((features & NotWriteHashMapArrayListClassName.mask) != 0 && (objectClass == HashMap.class || objectClass == ArrayList.class))
                    || ((features & NotWriteRootClassName.mask) != 0 && object == jsonWriter.rootObject)
            ) {
                return off;
            }

            return writeTypeName(bytes, off, TypeUtils.getTypeName(objectClass), jsonWriter);
        }

        static int writeTypeName(byte[] bytes, int off, String typeName, JSONWriter jsonWriter) {
            JSONWriterJSONB jsonWriterJSONB = (JSONWriterJSONB) jsonWriter;
            SymbolTable symbolTable = jsonWriter.symbolTable;
            bytes[off++] = BC_TYPED_ANY;

            long hash = Fnv.hashCode64(typeName);

            int symbol = -1;
            if (symbolTable != null) {
                symbol = symbolTable.getOrdinalByHashCode(hash);
                if (symbol == -1 && jsonWriterJSONB.symbols != null) {
                    symbol = jsonWriterJSONB.symbols.get(hash);
                }
            } else if (jsonWriterJSONB.symbols != null) {
                symbol = jsonWriterJSONB.symbols.get(hash);
            }

            if (symbol == -1) {
                if (jsonWriterJSONB.symbols == null) {
                    jsonWriterJSONB.symbols = new TLongIntHashMap();
                }
                jsonWriterJSONB.symbols.put(hash, symbol = jsonWriterJSONB.symbolIndex++);
            } else {
                return JSONB.IO.writeInt32(bytes, off, symbol);
            }

            off = writeString(bytes, off, typeName);
            return writeInt32(bytes, off, symbol);
        }

        static int writeInt32(byte[] bytes, int off, int value) {
            if (((value + 0x10) & ~0x3f) == 0) {
                bytes[off++] = (byte) value;
            } else if (((value + 0x800) & ~0xfff) == 0) {
                putShortBE(bytes, off, (short) ((BC_INT32_BYTE_ZERO << 8) + value));
                off += 2;
            } else if (((value + 0x40000) & ~0x7ffff) == 0) {
                bytes[off] = (byte) (BC_INT32_SHORT_ZERO + (value >> 16));
                putShortBE(bytes, off + 1, (short) value);
                off += 3;
            } else {
                bytes[off] = BC_INT32;
                putIntBE(bytes, off + 1, value);
                off += 5;
            }
            return off;
        }

        static int writeInt64(byte[] bytes, int off, Collection<Long> values, long features) {
            if (values == null) {
                bytes[off] = (features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL;
                return off + 1;
            }
            int size = values.size();
            off = startArray(bytes, off, size);
            for (Long value : values) {
                off = writeInt64(bytes, off, value, features);
            }
            return off;
        }

        static int writeInt64(byte[] bytes, int off, Long value, long features) {
            if (value == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0
                        ? BC_NULL
                        : (byte) (BC_INT64_NUM_MIN - INT64_NUM_LOW_VALUE);
                return off + 1;
            }
            return IO.writeInt64(bytes, off, value);
        }

        static int writeInt64(byte[] bytes, int off, long value) {
            if (value >= INT64_NUM_LOW_VALUE && value <= INT64_NUM_HIGH_VALUE) {
                bytes[off++] = (byte) (BC_INT64_NUM_MIN + (value - INT64_NUM_LOW_VALUE));
            } else if (((value + 0x800) & ~0xfffL) == 0) {
                putShortBE(bytes, off, (short) ((BC_INT64_BYTE_ZERO << 8) + value));
                off += 2;
            } else if (((value + 0x40000) & ~0x7ffffL) == 0) {
                bytes[off] = (byte) (BC_INT64_SHORT_ZERO + (value >> 16));
                putShortBE(bytes, off + 1, (short) value);
                off += 3;
            } else if ((((value + 0x80000000L) & ~0xffffffffL) == 0)) {
                bytes[off] = BC_INT64_INT;
                putIntBE(bytes, off + 1, (int) value);
                off += 5;
            } else {
                bytes[off] = BC_INT64;
                putLongBE(bytes, off + 1, value);
                off += 9;
            }
            return off;
        }

        static int startArray(byte[] bytes, int off, int size) {
            boolean tinyInt = size <= ARRAY_FIX_LEN;
            bytes[off++] = tinyInt ? (byte) (BC_ARRAY_FIX_MIN + size) : BC_ARRAY;
            if (!tinyInt) {
                off = writeInt32(bytes, off, size);
            }
            return off;
        }

        static int writeString(byte[] bytes, int off, Collection<String> strings, long features) {
            if (strings == null) {
                bytes[off] = (features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL;
                return off + 1;
            }
            int size = strings.size();
            off = startArray(bytes, off, size);
            for (String string : strings) {
                off = writeString(bytes, off, string);
            }
            return off;
        }

        static int writeString(byte[] bytes, int off, String[] strings, long features) {
            if (strings == null) {
                bytes[off] = (features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL;
                return off + 1;
            }
            int size = strings.length;
            off = startArray(bytes, off, size);
            for (String string : strings) {
                off = writeString(bytes, off, string);
            }
            return off;
        }

        static int writeString(byte[] bytes, int off, String str) {
            if (str == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            if (STRING_CODER != null && STRING_VALUE != null) {
                int coder = STRING_CODER.applyAsInt(str);
                byte[] value = STRING_VALUE.apply(str);
                if (coder == 0) {
                    return writeStringLatin1(bytes, off, value);
                } else {
                    return writeStringUTF16(bytes, off, value);
                }
            } else {
                return writeString(bytes, off, JDKUtils.getCharArray(str));
            }
        }

        static int writeStringUTF16(byte[] bytes, int off, byte[] value) {
            final int strlen = value.length;
            bytes[off] = JDKUtils.BIG_ENDIAN ? BC_STR_UTF16BE : BC_STR_UTF16LE;
            off = JSONB.IO.writeInt32(bytes, off + 1, strlen);
            System.arraycopy(value, 0, bytes, off, strlen);
            return off + strlen;
        }

        static int writeStringLatin1(byte[] bytes, int off, byte[] value) {
            int strlen = value.length;
            if (strlen <= STR_ASCII_FIX_LEN) {
                bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
            } else if (strlen <= INT32_BYTE_MAX) {
                off = putStringSizeSmall(bytes, off, strlen);
            } else {
                off = putStringSizeLarge(bytes, off, strlen);
            }
            System.arraycopy(value, 0, bytes, off, value.length);
            return off + strlen;
        }

        static int stringCapacity(Collection<String> strings) {
            if (strings == null) {
                return 1;
            }
            int size = stringCapacity(strings.getClass().getName()) + 7;
            for (String string : strings) {
                size += stringCapacity(string);
            }
            return size;
        }

        static int stringCapacity(String[] strings) {
            if (strings == null) {
                return 1;
            }
            int size = 6;
            for (String string : strings) {
                size += stringCapacity(string);
            }
            return size;
        }

        static int int64Capacity(Collection<Long> values) {
            if (values == null) {
                return 1;
            }
            return stringCapacity(values.getClass().getName())
                    + 7
                    + values.size() * 9;
        }

        static int stringCapacity(String str) {
            if (str == null) {
                return 0;
            }

            int strlen = str.length();
            if (STRING_CODER != null && STRING_VALUE != null) {
                return (strlen << STRING_CODER.applyAsInt(str)) + 6;
            }

            return strlen * 3 + 6;
        }

        static int putStringSizeSmall(byte[] bytes, int off, int val) {
            bytes[off] = BC_STR_ASCII;
            putShortBE(bytes, off + 1, (short) ((BC_INT32_BYTE_ZERO << 8) + val));
            return off + 3;
        }

        static int putStringSizeLarge(byte[] bytes, int off, int strlen) {
            if (strlen <= INT32_SHORT_MAX) {
                putIntBE(bytes, off, (BC_STR_ASCII << 24) + (BC_INT32_SHORT_ZERO << 16) + strlen);
                return off + 4;
            }

            putShortBE(bytes, off, (short) ((BC_STR_ASCII << 8) | BC_INT32));
            putIntBE(bytes, off + 2, strlen);
            return off + 6;
        }

        static int writeString(byte[] bytes, int off, char[] chars) {
            return writeString(bytes, off, chars, 0, chars.length);
        }

        static int writeString(byte[] bytes, int off, char[] chars, int coff, int strlen) {
            int start = off;
            boolean ascii = true;
            if (strlen < STR_ASCII_FIX_LEN) {
                bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
                for (int i = coff, end = coff + strlen; i < end; i++) {
                    char ch = chars[i];
                    if (ch > 0x00FF) {
                        ascii = false;
                        break;
                    }
                    bytes[off++] = (byte) ch;
                }

                if (ascii) {
                    return off;
                }

                off = start;
            } else {
                ascii = isLatin1(chars, coff, strlen);
            }

            if (ascii) {
                off = writeStringLatin1(bytes, off, chars, coff, strlen);
            } else {
                off = writeUTF8(bytes, off, chars, coff, strlen);
            }
            return off;
        }

        static int writeStringLatin1(byte[] bytes, int off, char[] chars, int coff, int strlen) {
            if (strlen <= STR_ASCII_FIX_LEN) {
                bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
            } else {
                bytes[off] = BC_STR_ASCII;
                if (strlen <= INT32_BYTE_MAX) {
                    putShortBE(bytes, off + 1, (short) ((BC_INT32_BYTE_ZERO << 8) + strlen));
                    off += 3;
                } else {
                    off = JSONB.IO.writeInt32(bytes, off + 1, strlen);
                }
            }
            for (int i = 0; i < strlen; i++) {
                bytes[off++] = (byte) chars[coff + i];
            }
            return off;
        }

        static int writeUTF8(byte[] bytes, int off, char[] chars, int coff, int strlen) {
            int maxSize = strlen * 3;
            int lenByteCnt = sizeOfInt(maxSize);
            int result = IOUtils.encodeUTF8(chars, coff, strlen, bytes, off + lenByteCnt + 1);

            int utf8len = result - off - lenByteCnt - 1;
            int utf8lenByteCnt = sizeOfInt(utf8len);
            if (lenByteCnt != utf8lenByteCnt) {
                System.arraycopy(bytes, off + lenByteCnt + 1, bytes, off + utf8lenByteCnt + 1, utf8len);
            }
            bytes[off] = BC_STR_UTF8;
            return JSONB.IO.writeInt32(bytes, off + 1, utf8len) + utf8len;
        }

        static int sizeOfInt(int i) {
            if (i >= BC_INT32_NUM_MIN && i <= BC_INT32_NUM_MAX) {
                return 1;
            }

            if (i >= INT32_BYTE_MIN && i <= INT32_BYTE_MAX) {
                return 2;
            }

            if (i >= INT32_SHORT_MIN && i <= INT32_SHORT_MAX) {
                return 3;
            }

            return 5;
        }

        static int writeUUID(byte[] bytes, int off, UUID value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            putShortLE(bytes, off, (short) ((BC_BINARY & 0xFF) | ((BC_INT32_NUM_16 & 0xFF) << 8)));
            putLongBE(bytes, off + 2, value.getMostSignificantBits());
            putLongBE(bytes, off + 10, value.getLeastSignificantBits());
            return off + 18;
        }

        static int writeInstant(byte[] bytes, int off, Instant value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            bytes[off] = BC_TIMESTAMP;
            off = JSONB.IO.writeInt64(bytes, off + 1, value.getEpochSecond());
            return JSONB.IO.writeInt32(bytes, off, value.getNano());
        }

        static int writeLocalDate(byte[] bytes, int off, LocalDate value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            bytes[off] = BC_LOCAL_DATE;
            int year = value.getYear();
            putIntBE(bytes, off + 1, (year << 16) | (value.getMonthValue() << 8) | value.getDayOfMonth());
            return off + 5;
        }

        static int writeLocalTime(byte[] bytes, int off, LocalTime value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            putIntBE(bytes,
                    off,
                    (BC_LOCAL_TIME << 24) | (value.getHour() << 16) | (value.getMinute() << 8) | value.getSecond());
            return JSONB.IO.writeInt32(bytes, off + 4, value.getNano());
        }

        static int writeLocalDateTime(byte[] bytes, int off, LocalDateTime value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            putIntBE(bytes,
                    off,
                    (BC_LOCAL_DATETIME << 24) | (value.getYear() << 8) | value.getMonthValue());
            putIntBE(bytes,
                    off + 4,
                    (value.getDayOfMonth() << 24)
                            | (value.getHour() << 16)
                            | (value.getMinute() << 8)
                            | value.getSecond());
            return writeInt32(bytes, off + 8, value.getNano());
        }

        static int writeOffsetDateTime(byte[] bytes, int off, OffsetDateTime value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            putIntBE(bytes,
                    off,
                    (BC_TIMESTAMP_WITH_TIMEZONE << 24) | (value.getYear() << 8) | value.getMonthValue());
            putIntBE(bytes,
                    off + 4,
                    (value.getDayOfMonth() << 24)
                            | (value.getHour() << 16)
                            | (value.getMinute() << 8)
                            | value.getSecond());

            off = writeInt32(bytes, off + 8, value.getNano());

            String zoneIdStr = value.getOffset().getId();
            int strlen = zoneIdStr.length();
            bytes[off] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
            zoneIdStr.getBytes(0, strlen, bytes, off + 1);
            return off + strlen + 1;
        }

        static int writeOffsetTime(byte[] bytes, int off, OffsetTime value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }

            int year = 1970, month = 1, dayOfMonth = 1;
            putIntBE(bytes,
                    off,
                    (BC_TIMESTAMP_WITH_TIMEZONE << 24) | (year << 8) | month);
            putIntBE(bytes,
                    off + 4,
                    (dayOfMonth << 24)
                            | (value.getHour() << 16)
                            | (value.getMinute() << 8)
                            | value.getSecond());

            off = writeInt32(bytes, off + 8, value.getNano());

            String zoneIdStr = value.getOffset().getId();
            int strlen = zoneIdStr.length();
            bytes[off] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
            zoneIdStr.getBytes(0, strlen, bytes, off + 1);
            return off + strlen + 1;
        }

        static int writeReference(byte[] bytes, int off, String path, JSONWriter jsonWriter) {
            if (jsonWriter.lastReference == path) {
                path = "#-1";
            } else {
                jsonWriter.lastReference = path;
            }
            bytes[off] = BC_REFERENCE;
            return writeString(bytes, off + 1, path);
        }

        static int writeNameRaw(byte[] bytes, int off, byte[] name, long nameHash, JSONWriter jsonWriter) {
            SymbolTable symbolTable = jsonWriter.symbolTable;
            JSONWriterJSONB jsonWriterJSONB = (JSONWriterJSONB) jsonWriter;
            int symbol;
            if (symbolTable == null
                    || (symbol = symbolTable.getOrdinalByHashCode(nameHash)) == -1
            ) {
                if ((jsonWriter.context.features & WriteNameAsSymbol.mask) == 0) {
                    System.arraycopy(name, 0, bytes, off, name.length);
                    return off + name.length;
                }

                boolean symbolExists = false;
                if (jsonWriterJSONB.symbols != null) {
                    if ((symbol = jsonWriterJSONB.symbols.putIfAbsent(nameHash, jsonWriterJSONB.symbolIndex)) != jsonWriterJSONB.symbolIndex) {
                        symbolExists = true;
                    } else {
                        jsonWriterJSONB.symbolIndex++;
                    }
                } else {
                    (jsonWriterJSONB.symbols = new TLongIntHashMap())
                            .put(nameHash, symbol = jsonWriterJSONB.symbolIndex++);
                }

                if (!symbolExists) {
                    bytes[off++] = BC_SYMBOL;
                    System.arraycopy(name, 0, bytes, off, name.length);
                    off += name.length;

                    if (symbol >= BC_INT32_NUM_MIN && symbol <= BC_INT32_NUM_MAX) {
                        bytes[off++] = (byte) symbol;
                    } else {
                        off = JSONB.IO.writeInt32(bytes, off, symbol);
                    }
                    return off;
                }
                symbol = -symbol;
            }

            bytes[off++] = BC_SYMBOL;
            int intValue = -symbol;
            if (intValue >= BC_INT32_NUM_MIN && intValue <= BC_INT32_NUM_MAX) {
                bytes[off++] = (byte) intValue;
            } else {
                off = JSONB.IO.writeInt32(bytes, off, intValue);
            }
            return off;
        }
    }
}
