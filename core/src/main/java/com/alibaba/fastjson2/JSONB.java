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
 * This is the main entry point for using fastjson2 binary format (JSONB) API.
 * JSONB is a high-performance binary serialization format that is more compact and faster than JSON text format.
 *
 * <p>JSONB binary format specification:
 * <pre>
 * x90          # type_char int
 * x91          # binary len_int32 bytes
 * x92          # type [str] symbol_int32 jsonb
 * x93          # reference
 *
 * x94 - xa3    # array_0 - array_15
 * xa4          # array len_int32 item*
 *
 * xa5          # object_end
 * xa6          # object_start
 *
 * xa7          # local time b0 b1 b2
 * xa8          # local datetime b0 b1 b2 b3 b4 b5 b6
 * xa9          # local date b0 b1 b2 b3
 * xab          # timestamp millis b0 b1 b2 b3 b4 b5 b6 b7
 * xac          # timestamp seconds b0 b1 b2 b3
 * xad          # timestamp minutes b0 b1 b2 b3
 * xae          # timestamp b0 b1 b2 b3 b4 b5 b6 b7 nano_int32
 *
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
 * xd8 - xef    # one-octet compact long (-x8 to xf, e0 is 0)
 *
 * xf0 - xff    # one-octet compact int
 * x00 - x2f    # one-octet compact int
 *
 * x30 - x3f    # two-octet compact int (-x800 to x7ff)
 * x40 - x47    # three-octet compact int (-x40000 to x3ffff)
 * x48          # 32-bit signed integer ('I')
 *
 * x49 - x78    # ascii string length 0-47
 * x79          # ascii-8 string variable-length
 * x7a          # utf-8 string variable-length
 * x7b          # utf-16 string variable-length
 * x7c          # utf-16LE string variable-length
 * x7d          # utf-16BE string variable-length
 * x7e          # gb18030 string variable-length
 * x7f          # symbol
 * </pre>
 *
 * <p>Example usage:
 * <pre>
 * // 1. Convert object to JSONB bytes
 * User user = new User(1L, "John", 30);
 * byte[] jsonbBytes = JSONB.toBytes(user);
 *
 * // 2. Parse JSONB bytes to object
 * User parsedUser = JSONB.parseObject(jsonbBytes, User.class);
 *
 * // 3. Convert primitive values to JSONB bytes
 * byte[] intBytes = JSONB.toBytes(123);
 * byte[] strBytes = JSONB.toBytes("Hello World");
 * byte[] boolBytes = JSONB.toBytes(true);
 *
 * // 4. Parse JSONB bytes with features
 * User user = JSONB.parseObject(jsonbBytes, User.class, JSONReader.Feature.FieldBased);
 *
 * // 5. Convert JSONB bytes to JSON string for debugging
 * String jsonString = JSONB.toJSONString(jsonbBytes);
 *
 * // 6. Parse JSONB bytes to JSONObject
 * JSONObject jsonObject = JSONB.parseObject(jsonbBytes);
 *
 * // 7. Parse JSONB bytes to JSONArray
 * JSONArray jsonArray = JSONB.parseArray(jsonbBytes);
 *
 * // 8. Parse JSONB bytes to List
 * List&lt;User&gt; userList = JSONB.parseArray(jsonbBytes, User.class);
 * </pre>
 *
 * @since 2.0.0
 */
public interface JSONB {
    /**
     * Dumps the JSONB bytes to standard output for debugging purposes
     *
     * @param jsonbBytes the JSONB bytes to dump
     */
    static void dump(byte[] jsonbBytes) {
        System.out.println(
                JSONB.toJSONString(jsonbBytes, true)
        );
    }

    /**
     * Dumps the JSONB bytes to standard output for debugging purposes with a symbol table
     *
     * @param jsonbBytes the JSONB bytes to dump
     * @param symbolTable the symbol table to use
     */
    static void dump(byte[] jsonbBytes, SymbolTable symbolTable) {
        JSONBDump dump = new JSONBDump(jsonbBytes, symbolTable, true);
        String str = dump.toString();
        System.out.println(str);
    }

    /**
     * Constants for JSONB binary format specification
     *
     * @since 2.0.0
     */
    interface Constants {
        /** Binary character type int */
        byte BC_CHAR = -112;                    // 0x90
        /** Binary data with length */
        byte BC_BINARY = -111;                  // 0x91
        /** Typed object with symbol */
        byte BC_TYPED_ANY = -110;               // 0x92
        /** Reference to previously serialized object */
        byte BC_REFERENCE = -109;               // 0x93

        /** Fixed array length */
        int ARRAY_FIX_LEN = 15;
        /** Fixed array with 0 elements */
        byte BC_ARRAY_FIX_0 = -108;             // 0x94
        /** Minimum fixed array marker */
        byte BC_ARRAY_FIX_MIN = BC_ARRAY_FIX_0;
        /** Maximum fixed array marker */
        byte BC_ARRAY_FIX_MAX = BC_ARRAY_FIX_MIN + ARRAY_FIX_LEN; // -105
        /** Variable length array */
        byte BC_ARRAY = -92;                    // 0xa4 len_int32 item*

        /** Object end marker */
        byte BC_OBJECT_END = -91;               // 0xa5
        /** Object start marker */
        byte BC_OBJECT = -90;                   // 0xa6

        /** Local time */
        byte BC_LOCAL_TIME = -89;               // 0xa7 b0 b1 b2 nano_int32
        /** Local datetime */
        byte BC_LOCAL_DATETIME = -88;           // 0xa8 b0 b1 b2 b3 b4 b5 b6 nano_int32
        /** Local date */
        byte BC_LOCAL_DATE = -87;               // 0xa9 b0 b1 b2 b3
        /** Timestamp with timezone */
        byte BC_TIMESTAMP_WITH_TIMEZONE = -86;  // 0xaa b0 b1 b2 b3 b4 b5 b6 b7 str_zone
        /** Timestamp in milliseconds */
        byte BC_TIMESTAMP_MILLIS = -85;         // 0xab b0 b1 b2 b3 b4 b5 b6 b7
        /** Timestamp in seconds */
        byte BC_TIMESTAMP_SECONDS = -84;        // 0xac b0 b1 b2 b3
        /** Timestamp in minutes */
        byte BC_TIMESTAMP_MINUTES = -83;        // 0xad b0 b1 b2 b3
        /** Timestamp */
        byte BC_TIMESTAMP = -82;                // 0xae millis_8 + nano_int32

        /** Null value */
        byte BC_NULL = -81;             // 0xaf
        /** Boolean false */
        byte BC_FALSE = -80;            // 0xb0
        /** Boolean true */
        byte BC_TRUE = -79;             // 0xb1
        /** Double 0 */
        byte BC_DOUBLE_NUM_0 = -78;     // 0xb2
        /** Double 1 */
        byte BC_DOUBLE_NUM_1 = -77;     // 0xb3
        /** Double as long */
        byte BC_DOUBLE_LONG = -76;      // 0xb4
        /** Double */
        byte BC_DOUBLE = -75;           // 0xb5
        /** Float as int */
        byte BC_FLOAT_INT = -74;        // 0xb6
        /** Float */
        byte BC_FLOAT = -73;            // 0xb7
        /** Decimal as long */
        byte BC_DECIMAL_LONG = -72;     // 0xb8
        /** Decimal */
        byte BC_DECIMAL = -71;          // 0xb9
        /** BigInteger as long */
        byte BC_BIGINT_LONG = -70;      // 0xba
        /** BigInteger */
        byte BC_BIGINT = -69;           // 0xbb
        /** Short */
        byte BC_INT16 = -68;            // 0xbc b0 b1
        /** Byte */
        byte BC_INT8 = -67;             // 0xbd b0
        /** Long */
        byte BC_INT64 = -66;            // 0xbe b0 b1 b2 b3 b4 b5 b6 b7
        /** Long as int */
        byte BC_INT64_INT = -65;        // 0xbf b0 b1 b2 b3

        /** Minimum 3-byte compact long */
        int INT64_SHORT_MIN = -0x40000; // -262144
        /** Maximum 3-byte compact long */
        int INT64_SHORT_MAX = 0x3ffff;  // 262143

        /** Minimum 2-byte compact long */
        int INT64_BYTE_MIN = -0x800;    // -2048
        /** Maximum 2-byte compact long */
        int INT64_BYTE_MAX = 0x7ff;     // 2047

        /** Minimum 3-byte compact long marker */
        byte BC_INT64_SHORT_MIN = -64;  // 0xc0
        /** Zero 3-byte compact long marker */
        byte BC_INT64_SHORT_ZERO = -60; //
        /** Maximum 3-byte compact long marker */
        byte BC_INT64_SHORT_MAX = -57;  // 0xc7

        /** Minimum 2-byte compact long marker */
        byte BC_INT64_BYTE_MIN = -56;   // 0xc8
        /** Zero 2-byte compact long marker */
        byte BC_INT64_BYTE_ZERO = -48;
        /** Maximum 2-byte compact long marker */
        byte BC_INT64_BYTE_MAX = -41;   // 0xd7

        /** Minimum 1-byte compact long marker */
        byte BC_INT64_NUM_MIN = -40;    // 0xd8 -8
        /** Maximum 1-byte compact long marker */
        byte BC_INT64_NUM_MAX = -17;    // 0xef 15

        /** Minimum 1-byte compact long value */
        int INT64_NUM_LOW_VALUE = -8;  // -8
        /** Maximum 1-byte compact long value */
        int INT64_NUM_HIGH_VALUE = 15; // 15

        /** Integer 0 */
        byte BC_INT32_NUM_0 = 0;
        /** Integer 1 */
        byte BC_INT32_NUM_1 = 1;
        /** Integer 16 */
        byte BC_INT32_NUM_16 = 16;

        /** Minimum 1-byte compact int */
        byte BC_INT32_NUM_MIN = -16; // 0xf0
        /** Maximum 1-byte compact int */
        byte BC_INT32_NUM_MAX = 47;  // 0x2f

        /** Minimum 2-byte compact int marker */
        byte BC_INT32_BYTE_MIN = 48;    // 0x30
        /** Zero 2-byte compact int marker */
        byte BC_INT32_BYTE_ZERO = 56;   // 0x38
        /** Maximum 2-byte compact int marker */
        byte BC_INT32_BYTE_MAX = 63;    // 0x3f

        /** Minimum 3-byte compact int marker */
        byte BC_INT32_SHORT_MIN = 64; // 0x40
        /** Zero 3-byte compact int marker */
        byte BC_INT32_SHORT_ZERO = 68;
        /** Maximum 3-byte compact int marker */
        byte BC_INT32_SHORT_MAX = 71; // 0x47
        /** 32-bit signed integer */
        byte BC_INT32 = 72; // 0x48

        /** Minimum 2-byte compact int value */
        int INT32_BYTE_MIN = -0x800; // -2048
        /** Maximum 2-byte compact int value */
        int INT32_BYTE_MAX = 0x7ff;  // 2047

        /** Minimum 3-byte compact int value */
        int INT32_SHORT_MIN = -0x40000; // -262144
        /** Maximum 3-byte compact int value */
        int INT32_SHORT_MAX = 0x3ffff;  // 262143

        /** ASCII string with 0 characters */
        byte BC_STR_ASCII_FIX_0 = 73;
        /** ASCII string with 1 character */
        byte BC_STR_ASCII_FIX_1 = 74;
        /** ASCII string with 4 characters */
        byte BC_STR_ASCII_FIX_4 = 77;
        /** ASCII string with 5 characters */
        byte BC_STR_ASCII_FIX_5 = 78;

        /** ASCII string with 32 characters */
        byte BC_STR_ASCII_FIX_32 = 105;
        /** ASCII string with 36 characters */
        byte BC_STR_ASCII_FIX_36 = 109;

        /** Fixed ASCII string length */
        int STR_ASCII_FIX_LEN = 47;

        /** Minimum fixed ASCII string marker */
        byte BC_STR_ASCII_FIX_MIN = 73; // 0x49
        /** Maximum fixed ASCII string marker */
        byte BC_STR_ASCII_FIX_MAX = BC_STR_ASCII_FIX_MIN + STR_ASCII_FIX_LEN; // 120 0x78
        /** Variable length ASCII string */
        byte BC_STR_ASCII = 121;
        /** UTF-8 string */
        byte BC_STR_UTF8 = 122;
        /** UTF-16 string */
        byte BC_STR_UTF16 = 123;
        /** UTF-16LE string */
        byte BC_STR_UTF16LE = 124;
        /** UTF-16BE string */
        byte BC_STR_UTF16BE = 125;
        /** GB18030 string */
        byte BC_STR_GB18030 = 126;
        /** Symbol */
        byte BC_SYMBOL = 127;
    }

    /**
     * Converts a boolean value to JSONB bytes
     *
     * @param v the boolean value to convert
     * @return the JSONB bytes representation
     */
    static byte[] toBytes(boolean v) {
        return new byte[]{v ? BC_TRUE : BC_FALSE};
    }

    /**
     * Converts an integer value to JSONB bytes
     *
     * @param i the integer value to convert
     * @return the JSONB bytes representation
     */
    static byte[] toBytes(int i) {
        if (i >= BC_INT32_NUM_MIN && i <= BC_INT32_NUM_MAX) {
            return new byte[]{(byte) i};
        }

        try (JSONWriter jsonWriter = JSONWriter.ofJSONB()) {
            jsonWriter.writeInt32(i);
            return jsonWriter.getBytes();
        }
    }

    /**
     * Converts a byte value to JSONB bytes
     *
     * @param i the byte value to convert
     * @return the JSONB bytes representation
     */
    static byte[] toBytes(byte i) {
        try (JSONWriter jsonWriter = JSONWriter.ofJSONB()) {
            jsonWriter.writeInt8(i);
            return jsonWriter.getBytes();
        }
    }

    /**
     * Converts a short value to JSONB bytes
     *
     * @param i the short value to convert
     * @return the JSONB bytes representation
     */
    static byte[] toBytes(short i) {
        try (JSONWriter jsonWriter = JSONWriter.ofJSONB()) {
            jsonWriter.writeInt16(i);
            return jsonWriter.getBytes();
        }
    }

    /**
     * Converts a long value to JSONB bytes
     *
     * @param i the long value to convert
     * @return the JSONB bytes representation
     */
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
     * Parses JSONB bytes to an object using the specified context
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param jsonbBytes the JSONB bytes to parse
     * @param context the JSON reader context
     * @return the parsed object
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

    /**
     * Parses JSONB bytes to an object with specified features
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param jsonbBytes the JSONB bytes to parse
     * @param features the JSON reader features to apply
     * @return the parsed object
     */
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

    /**
     * Parses JSONB from an input stream to an object using the specified context
     *
     * <p>Reads and buffers the stream to EOF, then parses the JSONB value. The stream is not closed.</p>
     * <details><summary>中文</summary>读取并缓存输入流直到 EOF，然后解析 JSONB 值；不会关闭输入流。</details>
     *
     * @param in the input stream to parse from
     * @param context the JSON reader context
     * @return the parsed object
     */
    static Object parse(InputStream in, JSONReader.Context context) {
        try (JSONReaderJSONB reader = new JSONReaderJSONB(context, in)) {
            Object object = reader.readAny();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parses JSONB bytes to an object with a symbol table and features
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param jsonbBytes the JSONB bytes to parse
     * @param symbolTable the symbol table to use
     * @param features the JSON reader features to apply
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes to a JSONObject
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param jsonbBytes the JSONB bytes to parse
     * @return the parsed JSONObject
     */
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

    /**
     * Parses JSONB bytes to a JSONObject with specified features
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param jsonbBytes the JSONB bytes to parse
     * @param features the JSON reader features to apply
     * @return the parsed JSONObject
     */
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

    /**
     * Parses JSONB bytes to a JSONObject using the specified context
     *
     * <p>Reads and buffers the stream to EOF, then parses the JSONB value. The stream is not closed.</p>
     * <details><summary>中文</summary>读取并缓存输入流直到 EOF，然后解析 JSONB 值；不会关闭输入流。</details>
     *
     * @param in the input stream to parse from
     * @param context the JSON reader context
     * @return the parsed JSONObject
     */
    static JSONObject parseObject(InputStream in, JSONReader.Context context) {
        try (JSONReaderJSONB reader = new JSONReaderJSONB(context, in)) {
            JSONObject object = (JSONObject) reader.readObject();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parses JSONB bytes to a JSONArray
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param jsonbBytes the JSONB bytes to parse
     * @return the parsed JSONArray
     */
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

    /**
     * Parses JSONB from an input stream to a JSONArray using the specified context
     *
     * <p>Reads and buffers the stream to EOF, then parses the JSONB value. The stream is not closed.</p>
     * <details><summary>中文</summary>读取并缓存输入流直到 EOF，然后解析 JSONB 值；不会关闭输入流。</details>
     *
     * @param in the input stream to parse from
     * @param context the JSON reader context
     * @return the parsed JSONArray
     */
    static JSONArray parseArray(InputStream in, JSONReader.Context context) {
        try (JSONReaderJSONB reader = new JSONReaderJSONB(context, in)) {
            JSONArray array = (JSONArray) reader.readArray();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            return array;
        }
    }

    /**
     * Parses JSONB bytes to a list of objects of the specified type
     *
     * <p>Null or empty input returns null. The input is JSONB binary data, not UTF-8 JSON text.</p>
     * <details><summary>中文</summary>null 或空输入返回 null；输入必须是 JSONB 二进制数据，而不是 UTF-8 JSON 文本。</details>
     *
     * @param <T> the type of objects in the list
     * @param jsonbBytes the JSONB bytes to parse
     * @param type the type of objects in the list
     * @return the parsed list, or null for null, empty, or encoded-null input
     */
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

    /**
     * Parses JSONB bytes to a list of objects of the specified type with features
     *
     * <p>Null or empty input returns null. The input is JSONB binary data, not UTF-8 JSON text.</p>
     * <details><summary>中文</summary>null 或空输入返回 null；输入必须是 JSONB 二进制数据，而不是 UTF-8 JSON 文本。</details>
     *
     * @param <T> the type of objects in the list
     * @param jsonbBytes the JSONB bytes to parse
     * @param type the type of objects in the list
     * @param features the JSON reader features to apply
     * @return the parsed list, or null for null, empty, or encoded-null input
     */
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

    /**
     * Parses JSONB bytes to a list of objects with specified types
     *
     * <p>Null or empty input returns null. The input is JSONB binary data, not UTF-8 JSON text.
     * Types are positional: {@code types[i]} controls the conversion of array element {@code i}.</p>
     * <details><summary>中文</summary>null 或空输入返回 null；输入必须是 JSONB 二进制数据，而不是 UTF-8 JSON 文本。types[i] 指定数组第 i 个元素的目标类型。</details>
     *
     * @param <T> the type of objects in the list
     * @param jsonbBytes the JSONB bytes to parse
     * @param types the types of objects in the list
     * @return the parsed list, or null for null, empty, or encoded-null input
     */
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

    /**
     * Parses JSONB bytes to a list of objects with specified types and features
     *
     * <p>Null or empty input returns null. The input is JSONB binary data, not UTF-8 JSON text.
     * Types are positional: {@code types[i]} controls the conversion of array element {@code i}.</p>
     * <details><summary>中文</summary>null 或空输入返回 null；输入必须是 JSONB 二进制数据，而不是 UTF-8 JSON 文本。types[i] 指定数组第 i 个元素的目标类型。</details>
     *
     * @param <T> the type of objects in the list
     * @param jsonbBytes the JSONB bytes to parse
     * @param types the types of objects in the list
     * @param features the JSON reader features to apply
     * @return the parsed list, or null for null, empty, or encoded-null input
     */
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

    /**
     * Parses JSONB bytes to an object of the specified class
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param objectClass the class of the object to parse to
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes to an object of the specified type
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param objectType the type of the object to parse to
     * @return the parsed object
     */
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

    /**
     * Parses a JSONB array into an Object[] with a target type for each position
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.
     * Types are positional: {@code types[i]} controls the conversion of array element {@code i}.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。types[i] 指定数组第 i 个元素的目标类型。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param types the types of the object to parse to
     * @return an Object[] of types.length, with nulls for missing elements, or null for an encoded null
     */
    static <T> T parseObject(byte[] jsonbBytes, Type... types) {
        return parseObject(jsonbBytes, new MultiType(types));
    }

    /**
     * Parses JSONB bytes to an object of the specified type with a symbol table
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param objectType the type of the object to parse to
     * @param symbolTable the symbol table to use
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes to an object of the specified type with a symbol table and features
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param objectType the type of the object to parse to
     * @param symbolTable the symbol table to use
     * @param features the JSON reader features to apply
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes to an object of the specified class with a filter and features
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param objectClass the class of the object to parse to
     * @param filter the filter to apply
     * @param features the JSON reader features to apply
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes to an object of the specified type with a symbol table, filters and features
     *
     * <p>Null or empty input returns null. The input is JSONB binary data, not UTF-8 JSON text.</p>
     * <details><summary>中文</summary>null 或空输入返回 null；输入必须是 JSONB 二进制数据，而不是 UTF-8 JSON 文本。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param objectType the type of the object to parse to
     * @param symbolTable the symbol table to use
     * @param filters the filters to apply
     * @param features the JSON reader features to apply
     * @return the parsed object, or null for null or empty input
     */
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
     * Creates a deep copy of the specified object
     *
     * <p>Delegates to {@link JSON#copy(Object, JSONWriter.Feature...)}. Null returns null; supported
     * immutable values may be returned unchanged.</p>
     * <details><summary>中文</summary>委托给 JSON.copy；null 返回 null，支持的不可变值可能直接返回。</details>
     *
     * @param <T> the type of the object
     * @param object the object to copy
     * @param features the JSON writer features to apply
     * @return a deep copy of the object
     * @since 2.0.30
     */
    static <T> T copy(T object, JSONWriter.Feature... features) {
        return JSON.copy(object, features);
    }

    /**
     * Parses JSONB bytes to an object of the specified type reference
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param typeReference the type reference of the object to parse to
     * @param features the JSON reader features to apply
     * @return the parsed object
     */
    static <T> T parseObject(byte[] jsonbBytes, TypeReference typeReference, JSONReader.Feature... features) {
        return parseObject(jsonbBytes, typeReference.getType(), features);
    }

    /**
     * Parses JSONB from an input stream to an object of the specified class
     *
     * <p>Reads and buffers the stream to EOF, then parses the JSONB value. The stream is not closed.</p>
     * <details><summary>中文</summary>读取并缓存输入流直到 EOF，然后解析 JSONB 值；不会关闭输入流。</details>
     *
     * @param <T> the type of the object
     * @param in the input stream to parse from
     * @param objectClass the class of the object to parse to
     * @param features the JSON reader features to apply
     * @return the parsed object
     * @throws IOException if an I/O error occurs
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
     * Parses JSONB from an input stream to an object of the specified type
     *
     * <p>Reads and buffers the stream to EOF, then parses the JSONB value. The stream is not closed.</p>
     * <details><summary>中文</summary>读取并缓存输入流直到 EOF，然后解析 JSONB 值；不会关闭输入流。</details>
     *
     * @param <T> the type of the object
     * @param in the input stream to parse from
     * @param objectType the type of the object to parse to
     * @param features the JSON reader features to apply
     * @return the parsed object
     * @throws IOException if an I/O error occurs
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
     * Parses JSONB from an input stream to an object of the specified type using the specified context
     *
     * <p>Reads and buffers the stream to EOF, then parses the JSONB value. The stream is not closed.</p>
     * <details><summary>中文</summary>读取并缓存输入流直到 EOF，然后解析 JSONB 值；不会关闭输入流。</details>
     *
     * @param <T> the type of the object
     * @param in the input stream to parse from
     * @param objectType the type of the object to parse to
     * @param context the JSON reader context
     * @return the parsed object
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
     * Parses JSONB from an input stream to an object of the specified class using the specified context
     *
     * <p>Reads and buffers the stream to EOF, then parses the JSONB value. The stream is not closed.</p>
     * <details><summary>中文</summary>读取并缓存输入流直到 EOF，然后解析 JSONB 值；不会关闭输入流。</details>
     *
     * @param <T> the type of the object
     * @param in the input stream to parse from
     * @param objectClass the class of the object to parse to
     * @param context the JSON reader context
     * @return the parsed object
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

    /**
     * Parses JSONB from an input stream with specified length to an object of the specified type using the specified context
     *
     * <p>Reads exactly {@code length} bytes, leaving subsequent bytes and the stream open.
     * Premature EOF causes IllegalArgumentException.</p>
     * <details><summary>中文</summary>读取恰好 length 个字节，保留后续字节且不关闭输入流；提前到达 EOF 时抛出 IllegalArgumentException。</details>
     *
     * @param <T> the type of the object
     * @param in the input stream to parse from
     * @param length the length of data to read
     * @param objectType the type of the object to parse to
     * @param context the JSON reader context
     * @return the parsed object
     * @throws IllegalArgumentException if EOF occurs before length bytes are read
     * @throws IOException if an I/O error occurs
     */
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
            // A stream may return fewer bytes than requested without reaching EOF.
            // <details><summary>中文</summary>流未结束时，单次读取也可能少于请求的字节数。</details>
            int read = 0;
            while (read < length) {
                int count = in.read(bytes, read, length - read);
                if (count < 0) {
                    break;
                }
                if (count == 0) {
                    int value = in.read();
                    if (value < 0) {
                        break;
                    }
                    bytes[read++] = (byte) value;
                } else {
                    read += count;
                }
            }
            if (read != length) {
                throw new IllegalArgumentException("deserialize failed. expected read length: " + length + " but actual read: " + read);
            }

            return parseObject(bytes, 0, length, objectType, context);
        } finally {
            BYTES_UPDATER.lazySet(cacheItem, bytes);
        }
    }

    /**
     * Parses JSONB from an input stream with specified length to an object of the specified type with features
     *
     * <p>Reads exactly {@code length} bytes, leaving subsequent bytes and the stream open.
     * Premature EOF causes IllegalArgumentException.</p>
     * <details><summary>中文</summary>读取恰好 length 个字节，保留后续字节且不关闭输入流；提前到达 EOF 时抛出 IllegalArgumentException。</details>
     *
     * @param <T> the type of the object
     * @param in the input stream to parse from
     * @param length the length of data to read
     * @param objectType the type of the object to parse to
     * @param features the JSON reader features to apply
     * @return the parsed object
     * @throws IllegalArgumentException if EOF occurs before length bytes are read
     * @throws IOException if an I/O error occurs
     */
    static <T> T parseObject(
            InputStream in,
            int length,
            Type objectType,
            JSONReader.Feature... features
    ) throws IOException {
        return parseObject(in, length, objectType, JSONFactory.createReadContext(features));
    }

    /**
     * Parses JSONB bytes to an object of the specified class with features
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param objectClass the class of the object to parse to
     * @param features the JSON reader features to apply
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes to an object of the specified class using the specified context
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param objectClass the class of the object to parse to
     * @param context the JSON reader context
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes to an object of the specified type with features
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param objectClass the type of the object to parse to
     * @param features the JSON reader features to apply
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes with offset and length to an object of the specified class
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference. The slice starts at {@code off} and spans {@code len} bytes.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。切片从 off 开始，共 len 字节。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param off the offset in the byte array
     * @param len the length of data to parse
     * @param objectClass the class of the object to parse to
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes with offset and length to an object of the specified type
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference. The slice starts at {@code off} and spans {@code len} bytes.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。切片从 off 开始，共 len 字节。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param off the offset in the byte array
     * @param len the length of data to parse
     * @param type the type of the object to parse to
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes with offset and length to an object of the specified class with features
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference. The slice starts at {@code off} and spans {@code len} bytes.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。切片从 off 开始，共 len 字节。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param off the offset in the byte array
     * @param len the length of data to parse
     * @param objectClass the class of the object to parse to
     * @param features the JSON reader features to apply
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes with offset and length to an object of the specified type using the specified context
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference. The slice starts at {@code off} and spans {@code len} bytes.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。切片从 off 开始，共 len 字节。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param off the offset in the byte array
     * @param len the length of data to parse
     * @param objectType the type of the object to parse to
     * @param context the JSON reader context
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes with offset and length to an object of the specified type with features
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference. The slice starts at {@code off} and spans {@code len} bytes.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。切片从 off 开始，共 len 字节。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param off the offset in the byte array
     * @param len the length of data to parse
     * @param objectType the type of the object to parse to
     * @param features the JSON reader features to apply
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes with offset and length to an object of the specified class with a symbol table
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference. The slice starts at {@code off} and spans {@code len} bytes.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。切片从 off 开始，共 len 字节。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param off the offset in the byte array
     * @param len the length of data to parse
     * @param objectClass the class of the object to parse to
     * @param symbolTable the symbol table to use
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes with offset and length to an object of the specified type with a symbol table
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference. The slice starts at {@code off} and spans {@code len} bytes.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。切片从 off 开始，共 len 字节。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param off the offset in the byte array
     * @param len the length of data to parse
     * @param objectClass the type of the object to parse to
     * @param symbolTable the symbol table to use
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes with offset and length to an object of the specified class with a symbol table and features
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference. The slice starts at {@code off} and spans {@code len} bytes.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。切片从 off 开始，共 len 字节。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param off the offset in the byte array
     * @param len the length of data to parse
     * @param objectClass the class of the object to parse to
     * @param symbolTable the symbol table to use
     * @param features the JSON reader features to apply
     * @return the parsed object
     */
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

    /**
     * Parses JSONB bytes with offset and length to an object of the specified type with a symbol table and features
     *
     * <p>The input must be nonnull JSONB binary data containing a value, not UTF-8 JSON text.
     * An encoded JSONB null value is distinct from a null byte-array reference. The slice starts at {@code off} and spans {@code len} bytes.</p>
     * <details><summary>中文</summary>输入必须为包含一个值的非 null JSONB 二进制数据，而不是 UTF-8 JSON 文本；编码后的 JSONB null 值不同于 null 字节数组引用。切片从 off 开始，共 len 字节。</details>
     *
     * @param <T> the type of the object
     * @param jsonbBytes the JSONB bytes to parse
     * @param off the offset in the byte array
     * @param len the length of data to parse
     * @param objectClass the type of the object to parse to
     * @param symbolTable the symbol table to use
     * @param features the JSON reader features to apply
     * @return the parsed object
     */
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

    /**
     * Converts a string to JSONB bytes
     *
     * <p>Serializes the String as a JSONB string value; it does not parse the String as JSON.
     * A null String produces the JSONB null marker. Use {@link #fromJSONString(String)} to encode JSON text.</p>
     * <details><summary>中文</summary>将 String 序列化为 JSONB 字符串，不将其作为 JSON 解析；null 字符串生成 JSONB null 标记。转换 JSON 文本请使用 fromJSONString。</details>
     *
     * @param str the string to convert
     * @return the JSONB bytes representation
     */
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

    /**
     * Converts a string to JSONB bytes with specified charset
     *
     * <p>Selects the JSONB string encoding for UTF-8, UTF-16, UTF-16BE, UTF-16LE, US-ASCII,
     * ISO-8859-1 or GB18030. A null or unrecognized charset falls back to {@link #toBytes(String)}.
     * A null String produces the JSONB null marker; the result includes JSONB headers, not just encoded text.</p>
     * <details><summary>中文</summary>支持 UTF-8、UTF-16、UTF-16BE、UTF-16LE、US-ASCII、ISO-8859-1 和 GB18030；null 或未识别的字符集回退到默认编码。null 字符串生成 JSONB null 标记，结果包含 JSONB 头部。</details>
     *
     * @param str the string to convert
     * @param charset the charset to use
     * @return the JSONB bytes representation
     */
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

    /**
     * Converts an object to JSONB bytes
     *
     * @param object the object to convert
     * @return the JSONB bytes representation
     */
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

    /**
     * Converts an object to JSONB bytes with specified context
     *
     * @param object the object to convert
     * @param context the JSON writer context
     * @return the JSONB bytes representation
     */
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

    /**
     * Converts an object to JSONB bytes with a symbol table
     *
     * @param object the object to convert
     * @param symbolTable the symbol table to use
     * @return the JSONB bytes representation
     */
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

    /**
     * Converts an object to JSONB bytes with a symbol table and features
     *
     * @param object the object to convert
     * @param symbolTable the symbol table to use
     * @param features the JSON writer features to apply
     * @return the JSONB bytes representation
     */
    static byte[] toBytes(Object object, SymbolTable symbolTable, JSONWriter.Feature... features) {
        return toBytes(object, new Context(), symbolTable, features);
    }

    /**
     * Converts an object to JSONB bytes with specified context, symbol table and features
     *
     * @param object the object to convert
     * @param context the JSON writer context
     * @param symbolTable the symbol table to use
     * @param features the JSON writer features to apply
     * @return the JSONB bytes representation
     */
    static byte[] toBytes(Object object, JSONWriter.Context context, SymbolTable symbolTable, JSONWriter.Feature... features) {
        context.config(features);
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

    /**
     * Converts an object to JSONB bytes with a symbol table, filters and features
     *
     * @param object the object to convert
     * @param symbolTable the symbol table to use
     * @param filters the filters to apply
     * @param features the JSON writer features to apply
     * @return the JSONB bytes representation
     */
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

    /**
     * Converts an object to JSONB bytes with specified features
     *
     * @param object the object to convert
     * @param features the JSON writer features to apply
     * @return the JSONB bytes representation
     */
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

    /**
     * Creates a symbol table with the specified names
     *
     * <p>Names are sorted and deduplicated before assigning ordinals starting at 1. The same external
     * symbol table must be available when decoding data written with it.</p>
     * <details><summary>中文</summary>名称排序去重后从 1 开始编号；解码使用外部符号表写入的数据时必须提供同一符号表。</details>
     *
     * @param names the names to include in the symbol table
     * @return the created symbol table
     */
    static SymbolTable symbolTable(String... names) {
        return new SymbolTable(names);
    }

    /**
     * Converts JSONB bytes to a JSON string
     *
     * <p>Produces a diagnostic JSON representation that may include JSONB type and reference metadata.
     * It is not a byte-preserving conversion.</p>
     * <details><summary>中文</summary>生成可能包含 JSONB 类型和引用元数据的诊断 JSON 表示，并非保留原始字节的转换。</details>
     *
     * @param jsonbBytes the JSONB bytes to convert
     * @return the JSON string representation
     */
    static String toJSONString(byte[] jsonbBytes) {
        return new JSONBDump(jsonbBytes, false)
                .toString();
    }

    /**
     * Converts JSONB bytes to a JSON string
     *
     * <p>Produces a diagnostic JSON representation that may include JSONB type and reference metadata.
     * It is not a byte-preserving conversion.</p>
     * <details><summary>中文</summary>生成可能包含 JSONB 类型和引用元数据的诊断 JSON 表示，并非保留原始字节的转换。</details>
     *
     * @param jsonbBytes the JSONB bytes to convert
     * @param raw true to expose symbol ordinals and raw type wrappers; false to resolve symbol names
     * @return the JSON string representation
     * @since 2.0.28
     */
    static String toJSONString(byte[] jsonbBytes, boolean raw) {
        return new JSONBDump(jsonbBytes, raw)
                .toString();
    }

    /**
     * Converts JSONB bytes to a JSON string with a symbol table
     *
     * <p>Produces a diagnostic JSON representation that may include JSONB type and reference metadata.
     * It is not a byte-preserving conversion.</p>
     * <details><summary>中文</summary>生成可能包含 JSONB 类型和引用元数据的诊断 JSON 表示，并非保留原始字节的转换。</details>
     *
     * @param jsonbBytes the JSONB bytes to convert
     * @param symbolTable the symbol table to use
     * @return the JSON string representation
     */
    static String toJSONString(byte[] jsonbBytes, SymbolTable symbolTable) {
        return toJSONString(jsonbBytes, symbolTable, false);
    }

    /**
     * Converts JSONB bytes to a JSON string with a symbol table
     *
     * <p>Produces a diagnostic JSON representation that may include JSONB type and reference metadata.
     * It is not a byte-preserving conversion.</p>
     * <details><summary>中文</summary>生成可能包含 JSONB 类型和引用元数据的诊断 JSON 表示，并非保留原始字节的转换。</details>
     *
     * @param jsonbBytes the JSONB bytes to convert
     * @param symbolTable the symbol table to use
     * @param raw true to expose symbol ordinals and raw type wrappers; false to resolve symbol names
     * @return the JSON string representation
     */
    static String toJSONString(byte[] jsonbBytes, SymbolTable symbolTable, boolean raw) {
        return new JSONBDump(jsonbBytes, symbolTable, raw)
                .toString();
    }

    /**
     * Writes an object to an output stream as JSONB bytes
     *
     * <p>Writes JSONB bytes without closing or flushing the supplied OutputStream. Null objects are
     * written as the JSONB null marker. I/O failures are wrapped in JSONException.</p>
     * <details><summary>中文</summary>写入 JSONB 字节，但不关闭或刷新传入的输出流；null 对象写为 JSONB null 标记，I/O 错误包装为 JSONException。</details>
     *
     * @param out the output stream to write to
     * @param object the object to write
     * @param features the JSON writer features to apply
     * @return the number of bytes written
     */
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

    /**
     * Converts a JSON string to JSONB bytes
     *
     * <p>Parses JSON text before encoding its value as JSONB. Formatting and lexical number spelling
     * are not preserved. Null or empty text is converted to the JSONB null marker.</p>
     * <details><summary>中文</summary>先解析 JSON 文本，再将其值编码为 JSONB；不保留排版或数字的原始文本形式。null 或空文本转换为 JSONB null 标记。</details>
     *
     * @param str the JSON string to convert
     * @return the JSONB bytes representation
     */
    static byte[] fromJSONString(String str) {
        return JSONB.toBytes(JSON.parse(str));
    }

    /**
     * Converts UTF-8 JSON text bytes to JSONB bytes
     *
     * <p>Parses UTF-8 JSON text before encoding its value as JSONB; the input is not already JSONB.</p>
     * <details><summary>中文</summary>先解析 UTF-8 JSON 文本，再将其值编码为 JSONB；输入不是已有的 JSONB 数据。</details>
     *
     * @param jsonUtf8Bytes the UTF-8 JSON text bytes to convert
     * @return the JSONB bytes representation
     */
    static byte[] fromJSONBytes(byte[] jsonUtf8Bytes) {
        JSONReader reader = JSONReader.of(jsonUtf8Bytes);
        ObjectReader objectReader = reader.getObjectReader(Object.class);
        Object object = objectReader.readObject(reader, null, null, 0);
        return JSONB.toBytes(object);
    }

    /**
     * Gets the type name for the specified type byte
     *
     * @param type the type byte
     * @return the type name
     */
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

    /**
     * Checks if the specified type is an int32 type
     *
     * @param type the type to check
     * @return true if the type is an int32 type, false otherwise
     */
    static boolean isInt32(int type) {
        return type >= BC_INT32_NUM_MIN && type <= BC_INT32;
    }

    /**
     * Checks if the specified type is an int32 number type
     *
     * <p>Checks a JSONB type marker, not the numeric value to encode. This form uses a single marker byte with no payload.</p>
     * <details><summary>中文</summary>检查的是 JSONB 类型标记，不是待编码的数值。</details>
     *
     * @param type the type to check
     * @return true if the type is an int32 number type, false otherwise
     */
    static boolean isInt32Num(int type) {
        return type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX;
    }

    /**
     * Checks if the specified type is an int32 byte type
     *
     * <p>Checks a JSONB type marker, not the numeric value to encode. This form uses a marker followed by one payload byte.</p>
     * <details><summary>中文</summary>检查的是 JSONB 类型标记，不是待编码的数值。</details>
     *
     * @param type the type to check
     * @return true if the type is an int32 byte type, false otherwise
     */
    static boolean isInt32Byte(int type) {
        return (type & 0xF0) == 0x30;
    }

    /**
     * Checks if the specified type is an int32 short type
     *
     * <p>Checks a JSONB type marker, not the numeric value to encode. This form uses a marker followed by two payload bytes.</p>
     * <details><summary>中文</summary>检查的是 JSONB 类型标记，不是待编码的数值。</details>
     *
     * @param type the type to check
     * @return true if the type is an int32 short type, false otherwise
     */
    static boolean isInt32Short(int type) {
        return (type & 0xF8) == 0x40;
    }

    /**
     * Checks if the specified type is an int64 number type
     *
     * <p>Checks a JSONB type marker, not the numeric value to encode. This form uses a single marker byte with no payload.</p>
     * <details><summary>中文</summary>检查的是 JSONB 类型标记，不是待编码的数值。</details>
     *
     * @param type the type to check
     * @return true if the type is an int64 number type, false otherwise
     */
    static boolean isInt64Num(int type) {
        return type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX;
    }

    /**
     * Checks if the specified type is an int64 byte type
     *
     * <p>Checks a JSONB type marker, not the numeric value to encode. This form uses a marker followed by one payload byte.</p>
     * <details><summary>中文</summary>检查的是 JSONB 类型标记，不是待编码的数值。</details>
     *
     * @param type the type to check
     * @return true if the type is an int64 byte type, false otherwise
     */
    static boolean isInt64Byte(int type) {
        return ((type - BC_INT64_BYTE_MIN) & 0xF0) == 0;
    }

    /**
     * Checks if the specified type is an int64 short type
     *
     * <p>Checks a JSONB type marker, not the numeric value to encode. This form uses a marker followed by two payload bytes.</p>
     * <details><summary>中文</summary>检查的是 JSONB 类型标记，不是待编码的数值。</details>
     *
     * @param type the type to check
     * @return true if the type is an int64 short type, false otherwise
     */
    static boolean isInt64Short(int type) {
        return (type & 0xF8) == 0xC0;
    }

    /**
     * Checks if the specified integer value can be represented as an int32 byte value
     *
     * <p>Tests the inclusive value range {@code [-2048, 2047]}, which fits the two-byte int32 encoding.</p>
     * <details><summary>中文</summary>检查包含边界的 [-2048, 2047] 数值范围，该范围可用两字节 int32 编码。</details>
     *
     * @param i the integer value to check
     * @return true if the value can be represented as an int32 byte value, false otherwise
     */
    static boolean isInt32ByteValue(int i) {
        return ((i + 2048) & ~0xFFF) == 0;
    }

    /**
     * Checks if the specified integer value is within the int32 byte value range
     *
     * <p>Tests the same inclusive range as {@link #isInt32ByteValue(int)}.</p>
     * <details><summary>中文</summary>检查的包含边界数值范围与 isInt32ByteValue 相同。</details>
     *
     * @param i the integer value to check
     * @return true if the value is within the int32 byte value range, false otherwise
     */
    static boolean isInt32ByteValue1(int i) {
        return i >= INT32_BYTE_MIN && i <= INT32_BYTE_MAX;
    }

    /**
     * Low-level JSONB serialization into caller-owned buffers.
     * Callers must provide valid slices and sufficient capacity; these methods do not grow buffers.
     * Write methods return the exclusive end offset. Capacity helpers return allocation estimates,
     * not necessarily the exact encoded length.
     * <details><summary>中文</summary>向调用方缓冲区写入 JSONB；调用方须提供有效切片和足够容量，方法不会扩容。写入方法返回结束偏移（不含），容量方法返回分配估计而非精确编码长度。</details>
     */
    interface IO {
        /**
         * Calculates the capacity needed for an enum
         *
         * @param e the enum value
         * @param features the features to apply
         * @return the capacity needed
         */
        static int enumCapacity(Enum e, long features) {
            if ((features & (MASK_WRITE_ENUM_USING_TO_STRING | MASK_WRITE_ENUMS_USING_NAME)) != 0) {
                return stringCapacity((features & WriteEnumUsingToString.mask) != 0
                        ? e.toString()
                        : e.name());
            }
            return 5;
        }

        /**
         * Writes an enum value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param e the enum value to write
         * @param features the features to apply
         * @return the new offset
         */
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

        /**
         * Writes a Boolean value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Boolean value to write
         * @return the new offset
         */
        static int writeBoolean(byte[] bytes, int off, Boolean value) {
            bytes[off] = value == null ? BC_NULL : value ? BC_TRUE : BC_FALSE;
            return off + 1;
        }

        /**
         * Writes a boolean value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the boolean value to write
         * @return the new offset
         */
        static int writeBoolean(byte[] bytes, int off, boolean value) {
            bytes[off] = value ? BC_TRUE : BC_FALSE;
            return off + 1;
        }

        /**
         * Writes a boolean array to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param values the boolean array to write
         * @return the new offset
         */
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

        /**
         * Writes a Float value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Float value to write
         * @param features the features to apply
         * @return the new offset
         */
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

        /**
         * Writes a float array to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param values the float array to write
         * @return the new offset
         */
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

        /**
         * Writes a float value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the float value to write
         * @return the new offset
         */
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

        /**
         * Writes a Double value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Double value to write
         * @param features the features to apply
         * @return the new offset
         */
        static int writeDouble(byte[] bytes, int off, Double value, long features) {
            if (value == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0
                        ? BC_NULL
                        : BC_DOUBLE_NUM_0;
                return off + 1;
            }
            return IO.writeDouble(bytes, off, value);
        }

        /**
         * Writes a double value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the double value to write
         * @return the new offset
         */
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

        /**
         * Writes a double array to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param values the double array to write
         * @return the new offset
         */
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

        /**
         * Writes a Byte value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param val the Byte value to write
         * @param features the features to apply
         * @return the new offset
         */
        static int writeInt8(byte[] bytes, int off, Byte val, long features) {
            if (val == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0 ? BC_NULL : 0;
                return off + 1;
            }
            putShortLE(bytes, off, (short) ((val << 8) | (BC_INT8 & 0xFF)));
            return off + 2;
        }

        /**
         * Writes a byte value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param val the byte value to write
         * @return the new offset
         */
        static int writeInt8(byte[] bytes, int off, byte val) {
            putShortLE(bytes, off, (short) ((val << 8) | (BC_INT8 & 0xFF)));
            return off + 2;
        }

        /**
         * Writes a Short value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param val the Short value to write
         * @param features the features to apply
         * @return the new offset
         */
        static int writeInt16(byte[] bytes, int off, Short val, long features) {
            if (val == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0 ? BC_NULL : 0;
                return off + 1;
            }
            bytes[off] = BC_INT16;
            putShortBE(bytes, off + 1, val);
            return off + 3;
        }

        /**
         * Writes a short value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param val the short value to write
         * @return the new offset
         */
        static int writeInt16(byte[] bytes, int off, short val) {
            bytes[off] = BC_INT16;
            putShortBE(bytes, off + 1, val);
            return off + 3;
        }

        /**
         * Writes an Integer value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Integer value to write
         * @param features the features to apply
         * @return the new offset
         */
        static int writeInt32(byte[] bytes, int off, Integer value, long features) {
            if (value == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0 ? BC_NULL : 0;
                return off + 1;
            }
            return IO.writeInt32(bytes, off, value);
        }

        /**
         * Writes a string with a symbol table to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param str the string to write
         * @param symbolTable the symbol table to use
         * @return the new offset
         */
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

        /**
         * Writes a symbol to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param symbol the symbol to write
         * @return the new offset
         */
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

        /**
         * Checks and writes a type name to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param object the object to check
         * @param fieldClass the field class
         * @param jsonWriter the JSON writer
         * @return the new offset
         */
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

        /**
         * Writes a type name to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param typeName the type name to write
         * @param jsonWriter the JSON writer
         * @return the new offset
         */
        static int writeTypeName(byte[] bytes, int off, String typeName, JSONWriter jsonWriter) {
            JSONWriterJSONB jsonWriterJSONB = (JSONWriterJSONB) jsonWriter;
            SymbolTable symbolTable = jsonWriter.symbolTable;
            bytes[off++] = BC_TYPED_ANY;

            long hash = Fnv.hashCode64(typeName);

            int symbol = -1;
            if (symbolTable != null) {
                symbol = symbolTable.getOrdinalByHashCode(hash);
                if (symbol != -1) {
                    return writeInt32(bytes, off, -symbol);
                }
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

        /**
         * Writes an integer value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the integer value to write
         * @return the new offset
         */
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

        /**
         * Writes a collection of Long values to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param values the collection of Long values to write
         * @param features the features to apply
         * @return the new offset
         */
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

        /**
         * Writes a Long value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Long value to write
         * @param features the features to apply
         * @return the new offset
         */
        static int writeInt64(byte[] bytes, int off, Long value, long features) {
            if (value == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0
                        ? BC_NULL
                        : (byte) (BC_INT64_NUM_MIN - INT64_NUM_LOW_VALUE);
                return off + 1;
            }
            return IO.writeInt64(bytes, off, value);
        }

        /**
         * Writes a long value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the long value to write
         * @return the new offset
         */
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

        /**
         * Starts an array in a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param size the size of the array
         * @return the new offset
         */
        static int startArray(byte[] bytes, int off, int size) {
            boolean tinyInt = size <= ARRAY_FIX_LEN;
            bytes[off++] = tinyInt ? (byte) (BC_ARRAY_FIX_MIN + size) : BC_ARRAY;
            if (!tinyInt) {
                off = writeInt32(bytes, off, size);
            }
            return off;
        }

        /**
         * Writes a collection of strings to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param strings the collection of strings to write
         * @param features the features to apply
         * @return the new offset
         */
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

        /**
         * Writes a string array to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param strings the string array to write
         * @param features the features to apply
         * @return the new offset
         */
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

        /**
         * Writes a string to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param str the string to write
         * @return the new offset
         */
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

        /**
         * Writes a UTF-16 string to a byte array
         *
         * <p>The source contains UTF-16 code units in the current JVM native byte order, without a BOM.
         * The emitted JSONB marker records that byte order.</p>
         * <details><summary>中文</summary>源字节为当前 JVM 本机字节序的 UTF-16 编码且不含 BOM；输出 JSONB 标记记录该字节序。</details>
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the UTF-16 byte array to write
         * @return the new offset
         */
        static int writeStringUTF16(byte[] bytes, int off, byte[] value) {
            final int strlen = value.length;
            bytes[off] = JDKUtils.BIG_ENDIAN ? BC_STR_UTF16BE : BC_STR_UTF16LE;
            off = JSONB.IO.writeInt32(bytes, off + 1, strlen);
            System.arraycopy(value, 0, bytes, off, strlen);
            return off + strlen;
        }

        /**
         * Writes a Latin-1 string to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Latin-1 byte array to write
         * @return the new offset
         */
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

        /**
         * Calculates the capacity needed for a collection of strings
         *
         * @param strings the collection of strings
         * @return the capacity needed
         */
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

        /**
         * Calculates the capacity needed for a string array
         *
         * @param strings the string array
         * @return the capacity needed
         */
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

        /**
         * Calculates the capacity needed for a collection of Long values
         *
         * @param values the collection of Long values
         * @return the capacity needed
         */
        static int int64Capacity(Collection<Long> values) {
            if (values == null) {
                return 1;
            }
            return stringCapacity(values.getClass().getName())
                    + 7
                    + values.size() * 9;
        }

        /**
         * Calculates the capacity needed for a string
         *
         * @param str the string
         * @return the capacity needed
         */
        static int stringCapacity(String str) {
            if (str == null) {
                return 1;
            }

            int strlen = str.length();
            if (STRING_CODER != null && STRING_VALUE != null) {
                return (strlen << STRING_CODER.applyAsInt(str)) + 6;
            }

            return strlen * 3 + 6;
        }

        /**
         * Puts a small string size to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param val the size value to write
         * @return the new offset
         */
        static int putStringSizeSmall(byte[] bytes, int off, int val) {
            bytes[off] = BC_STR_ASCII;
            putShortBE(bytes, off + 1, (short) ((BC_INT32_BYTE_ZERO << 8) + val));
            return off + 3;
        }

        /**
         * Puts a large string size to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param strlen the size value to write
         * @return the new offset
         */
        static int putStringSizeLarge(byte[] bytes, int off, int strlen) {
            if (strlen <= INT32_SHORT_MAX) {
                putIntBE(bytes, off, (BC_STR_ASCII << 24) + (BC_INT32_SHORT_ZERO << 16) + strlen);
                return off + 4;
            }

            putShortBE(bytes, off, (short) ((BC_STR_ASCII << 8) | BC_INT32));
            putIntBE(bytes, off + 2, strlen);
            return off + 6;
        }

        /**
         * Writes a character array to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param chars the character array to write
         * @return the new offset
         */
        static int writeString(byte[] bytes, int off, char[] chars) {
            return writeString(bytes, off, chars, 0, chars.length);
        }

        /**
         * Writes a character array with offset and length to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param chars the character array to write
         * @param coff the offset in the character array
         * @param strlen the length of characters to write
         * @return the new offset
         */
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

        /**
         * Writes a Latin-1 character array to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param chars the character array to write
         * @param coff the offset in the character array
         * @param strlen the length of characters to write
         * @return the new offset
         */
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

        /**
         * Writes a character-array slice as a JSONB UTF-8 string
         *
         * <p>Encodes a slice of Java UTF-16 characters as a JSONB UTF-8 string, including its type and length.</p>
         * <details><summary>中文</summary>将 Java UTF-16 字符切片编码为 JSONB UTF-8 字符串，包含类型和长度。</details>
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param chars the character array to write
         * @param coff the offset in the character array
         * @param strlen the length of characters to write
         * @return the new offset
         */
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

        /**
         * Calculates the size needed for an integer value
         *
         * <p>Includes the type marker; the result is 1, 2, 3 or 5 bytes.</p>
         * <details><summary>中文</summary>包含类型标记，结果为 1、2、3 或 5 字节。</details>
         *
         * @param i the integer value
         * @return the size needed
         */
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

        /**
         * Writes a UUID value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the UUID value to write
         * @return the new offset
         */
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

        /**
         * Writes an Instant value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Instant value to write
         * @return the new offset
         */
        static int writeInstant(byte[] bytes, int off, Instant value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            bytes[off] = BC_TIMESTAMP;
            off = JSONB.IO.writeInt64(bytes, off + 1, value.getEpochSecond());
            return JSONB.IO.writeInt32(bytes, off, value.getNano());
        }

        /**
         * Writes a LocalDate value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the LocalDate value to write
         * @return the new offset
         */
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

        /**
         * Writes a LocalTime value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the LocalTime value to write
         * @return the new offset
         */
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

        /**
         * Writes a LocalDateTime value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the LocalDateTime value to write
         * @return the new offset
         */
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

        /**
         * Writes an OffsetDateTime value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the OffsetDateTime value to write
         * @return the new offset
         */
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

        /**
         * Writes an OffsetTime value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the OffsetTime value to write
         * @return the new offset
         */
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

        /**
         * Writes a reference to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param path the reference path
         * @param jsonWriter the JSON writer
         * @return the new offset
         */
        static int writeReference(byte[] bytes, int off, String path, JSONWriter jsonWriter) {
            if (jsonWriter.lastReference == path) {
                path = "#-1";
            } else {
                jsonWriter.lastReference = path;
            }
            bytes[off] = BC_REFERENCE;
            return writeString(bytes, off + 1, path);
        }

        /**
         * Writes a raw name to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param name the name byte array to write
         * @param nameHash the name hash
         * @param jsonWriter the JSON writer
         * @return the new offset
         */
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
