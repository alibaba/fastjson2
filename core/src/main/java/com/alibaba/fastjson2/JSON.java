package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.*;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.MapMultiValueType;
import com.alibaba.fastjson2.util.MultiType;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONReader.EOI;
import static com.alibaba.fastjson2.JSONReader.Feature.IgnoreCheckClose;
import static com.alibaba.fastjson2.JSONReader.Feature.UseNativeObject;

/**
 * Entry point for parsing JSON text, serializing Java values, and configuring default codecs.
 * Use {@link JSONB} for the binary JSONB format.
 *
 * <p>{@code parse} reads an untyped value, including strings, numbers, booleans and null.
 * {@code parseObject} overloads with a target type convert the value to that type;
 * {@code parseArray} overloads with an element type produce a typed list.
 * Byte input and output use UTF-8 unless an overload accepts a charset.
 * Offsets and lengths count bytes for byte arrays and UTF-16 code units for strings
 * and character arrays.</p>
 *
 * <p>Feature arguments enable options in addition to the defaults. An overload accepting
 * a context uses that context's provider and settings. Single-value parsing normally
 * checks for trailing input; {@link JSONReader.Feature#IgnoreCheckClose} disables that check.
 * Validation methods use fastjson2's accepted syntax, including supported extensions;
 * they are not strict RFC-only validators.</p>
 *
 * <details><summary>中文</summary>
 * 提供 JSON 文本解析、Java 值序列化和默认编解码器配置；二进制格式请使用 JSONB。
 * parse 支持对象、数组和标量；带目标类型的方法执行类型转换。未指定字符集时字节输入输出使用 UTF-8。
 * 字节数组偏移按字节计数，字符串和字符数组按 UTF-16 代码单元计数。
 * 特性参数在默认配置上启用选项；验证方法遵循 fastjson2 支持的语法，并非严格的 RFC 校验器。
 * </details>
 *
 * <p>Examples include a consumer overload for newline-delimited records.</p>
 * <details><summary>中文</summary>示例包含按行读取 JSON 记录的消费器重载。</details>
 * <pre>{@code
 * JSONObject object = JSON.parseObject("{\"name\":\"John\",\"age\":30}");
 * String pretty = JSON.toJSONString(object, JSONWriter.Feature.PrettyFormat);
 * byte[] utf8 = JSON.toJSONBytes(object);
 *
 * List<Integer> numbers = JSON.parseArray("[1,2,3]", Integer.class);
 * Map<String, List<Integer>> groups = JSON.parseObject(
 *         "{\"items\":[1,2]}", new TypeReference<Map<String, List<Integer>>>() {});
 *
 * try (InputStream input = new java.io.ByteArrayInputStream(utf8)) {
 *     JSON.<JSONObject>parseObject(input, JSONObject.class, System.out::println);
 * }
 * }</pre>
 *
 * @since 2.0.0
 */

public interface JSON {
    /**
     * fastjson2 version name
     */
    String VERSION = "2.0.64";

    /**
     * Parses one JSON value from the string.
     * The result may be an object, array, string, number, boolean or null.
     * Object and array representations depend on the reader context and features.
     *
     * <p>Returns null for a null input or an empty input.</p>
     *
     * <details><summary>中文</summary>
     * 解析一个 JSON 值，支持对象、数组及标量；容器类型由读取上下文和特性决定。
     * </details>
     *
     * @param text the JSON text
     * @return the parsed Java value, or null
     * @throws JSONException if a parsing error occurs
     */
    static Object parse(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        final JSONReader.Context context = new JSONReader.Context(provider);
        try (JSONReader reader = JSONReader.of(text, context)) {
            Object object;
            char ch = reader.current();

            if (context.objectSupplier == null
                    && (context.features & UseNativeObject.mask) == 0
                    && (ch == '{' || ch == '[')
            ) {
                if (ch == '{') {
                    JSONObject jsonObject = new JSONObject();
                    reader.read(jsonObject, 0);
                    object = jsonObject;
                } else {
                    JSONArray array = new JSONArray();
                    reader.read(array);
                    object = array;
                }
                if (reader.resolveTasks != null) {
                    reader.handleResolveTasks(object);
                }
            } else {
                ObjectReader<?> objectReader = provider.getObjectReader(Object.class, false);
                object = objectReader.readObject(reader, null, null, 0);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses one JSON value from the string.
     * The result may be an object, array, string, number, boolean or null.
     * Object and array representations depend on the reader context and features.
     *
     * <p>Returns null for a null input or an empty input.</p>
     *
     * <details><summary>中文</summary>
     * 解析一个 JSON 值，支持对象、数组及标量；容器类型由读取上下文和特性决定。
     * </details>
     *
     * @param text the JSON text
     * @param features reader features to enable in addition to the defaults
     * @return the parsed Java value, or null
     * @throws JSONException if a parsing error occurs
     */
    static Object parse(String text, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        final JSONReader.Context context = new JSONReader.Context(provider, features);
        final ObjectReader<?> objectReader = provider.getObjectReader(Object.class, false);

        try (JSONReader reader = JSONReader.of(text, context)) {
            context.config(features);
            Object object = objectReader.readObject(reader, null, null, 0);
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses one JSON value from the string slice.
     * The result may be an object, array, string, number, boolean or null.
     * Object and array representations depend on the reader context and features.
     *
     * <p>Returns null for a null input or an empty input, or when length is zero.</p>
     *
     * <details><summary>中文</summary>
     * 解析一个 JSON 值，支持对象、数组及标量；容器类型由读取上下文和特性决定。
     * </details>
     *
     * @param text the JSON text
     * @param offset the zero-based offset in UTF-16 code units
     * @param length the number of UTF-16 code units to read, not the end index
     * @param features reader features to enable in addition to the defaults
     * @return the parsed Java value, or null
     * @throws JSONException if a parsing error occurs
     */
    static Object parse(String text, int offset, int length, JSONReader.Feature... features) {
        if (text == null || text.isEmpty() || length == 0) {
            return null;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        final JSONReader.Context context = new JSONReader.Context(provider, features);
        ObjectReader<?> objectReader = provider.getObjectReader(Object.class, false);

        try (JSONReader reader = JSONReader.of(text, offset, length, context)) {
            Object object = objectReader.readObject(reader, null, null, 0);
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses one JSON value from the string.
     * The result may be an object, array, string, number, boolean or null.
     * Object and array representations depend on the reader context and features.
     *
     * <p>Returns null for a null input or an empty input.</p>
     *
     * <details><summary>中文</summary>
     * 解析一个 JSON 值，支持对象、数组及标量；容器类型由读取上下文和特性决定。
     * </details>
     *
     * @param text the JSON text
     * @param context the reader provider and parsing settings, not null
     * @return the parsed Java value, or null
     * @throws JSONException if a parsing error occurs
     * @throws NullPointerException if received context is null
     */
    static Object parse(String text, JSONReader.Context context) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        ObjectReader<?> objectReader = context.provider.getObjectReader(Object.class, false);

        try (JSONReader reader = JSONReader.of(text, context)) {
            Object object = objectReader.readObject(reader, null, null, 0);
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses one JSON value from the byte array.
     * The result may be an object, array, string, number, boolean or null.
     * Object and array representations depend on the reader context and features.
     *
     * <p>Returns null for a null input or an empty input.</p>
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 解析一个 JSON 值，支持对象、数组及标量；容器类型由读取上下文和特性决定。
     * </details>
     *
     * @param bytes the UTF-8 JSON text
     * @param features reader features to enable in addition to the defaults
     * @return the parsed Java value, or null
     * @throws JSONException if a parsing error occurs
     */
    static Object parse(byte[] bytes, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        final JSONReader.Context context = new JSONReader.Context(provider, features);
        ObjectReader<?> objectReader = provider.getObjectReader(Object.class, false);

        try (JSONReader reader = JSONReader.of(bytes, context)) {
            Object object = objectReader.readObject(reader, null, null, 0);
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses one JSON value from the byte array.
     * The result may be an object, array, string, number, boolean or null.
     * Object and array representations depend on the reader context and features.
     *
     * <p>Returns null for a null input or an empty input.</p>
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 解析一个 JSON 值，支持对象、数组及标量；容器类型由读取上下文和特性决定。
     * </details>
     *
     * @param bytes the UTF-8 JSON text
     * @param context the reader provider and parsing settings, not null
     * @return the parsed Java value, or null
     * @throws JSONException if a parsing error occurs
     * @since 2.0.51
     */
    static Object parse(byte[] bytes, JSONReader.Context context) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        ObjectReader<?> objectReader = context.getObjectReader(Object.class);

        try (JSONReader reader = JSONReader.of(bytes, context)) {
            Object object = objectReader.readObject(reader, null, null, 0);
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses one JSON value from the byte array slice.
     * The result may be an object, array, string, number, boolean or null.
     * Object and array representations depend on the reader context and features.
     *
     * <p>Returns null for a null input or an empty input.</p>
     *
     * <p>The selected bytes are decoded with the supplied charset; they must contain JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 解析一个 JSON 值，支持对象、数组及标量；容器类型由读取上下文和特性决定。
     * </details>
     *
     * @param bytes the encoded JSON text
     * @param offset the zero-based offset in bytes
     * @param length the number of bytes to read, not the end index
     * @param charset the input charset: UTF-8, UTF-16, US-ASCII or ISO-8859-1; not null
     * @param context the reader provider and parsing settings, not null
     * @return the parsed Java value, or null
     * @throws JSONException if a parsing error occurs
     * @since 2.0.51
     */
    static Object parse(byte[] bytes, int offset, int length, Charset charset, JSONReader.Context context) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        ObjectReader<?> objectReader = context.getObjectReader(Object.class);

        try (JSONReader reader = JSONReader.of(bytes, offset, length, charset, context)) {
            Object object = objectReader.readObject(reader, null, null, 0);
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses one JSON value from the character array.
     * The result may be an object, array, string, number, boolean or null.
     * Object and array representations depend on the reader context and features.
     *
     * <p>Returns null for a null input or an empty input.</p>
     *
     * <details><summary>中文</summary>
     * 解析一个 JSON 值，支持对象、数组及标量；容器类型由读取上下文和特性决定。
     * </details>
     *
     * @param chars the JSON text as UTF-16 code units
     * @param features reader features to enable in addition to the defaults
     * @return the parsed Java value, or null
     * @throws JSONException if a parsing error occurs
     */
    static Object parse(char[] chars, JSONReader.Feature... features) {
        if (chars == null || chars.length == 0) {
            return null;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        final JSONReader.Context context = new JSONReader.Context(provider, features);
        ObjectReader<?> objectReader = provider.getObjectReader(Object.class, false);

        try (JSONReader reader = JSONReader.of(chars, context)) {
            Object object = objectReader.readObject(reader, null, null, 0);
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses one JSON value from the character array.
     * The result may be an object, array, string, number, boolean or null.
     * Object and array representations depend on the reader context and features.
     *
     * <p>Returns null for a null input or an empty input.</p>
     *
     * <details><summary>中文</summary>
     * 解析一个 JSON 值，支持对象、数组及标量；容器类型由读取上下文和特性决定。
     * </details>
     *
     * @param chars the JSON text as UTF-16 code units
     * @param context the reader provider and parsing settings, not null
     * @return the parsed Java value, or null
     * @throws JSONException if a parsing error occurs
     * @since 2.0.51
     */
    static Object parse(char[] chars, JSONReader.Context context) {
        if (chars == null || chars.length == 0) {
            return null;
        }

        ObjectReader<?> objectReader = context.getObjectReader(Object.class);

        try (JSONReader reader = JSONReader.of(chars, context)) {
            Object object = objectReader.readObject(reader, null, null, 0);
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses one JSON value from the input.
     * The result may be an object, array, string, number, boolean or null.
     * Object and array representations depend on the reader context and features.
     *
     * <p>Returns null for a null input.</p>
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <p>Input bytes are decoded as UTF-8.</p>
     *
     * <details><summary>中文</summary>
     * 解析一个 JSON 值，支持对象、数组及标量；容器类型由读取上下文和特性决定。
     * </details>
     *
     * @param in the input stream to parse, or null
     * @param features reader features to enable in addition to the defaults
     * @return the parsed Java value, or null
     * @throws JSONException if a parsing error occurs
     * @since 2.0.61
     */
    static Object parse(InputStream in, JSONReader.Feature... features) {
        return parse(in, JSONFactory.createReadContext(features));
    }

    /**
     * Parses one JSON value from the input.
     * The result may be an object, array, string, number, boolean or null.
     * Object and array representations depend on the reader context and features.
     *
     * <p>Returns null for a null input.</p>
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <p>Input bytes are decoded as UTF-8.</p>
     *
     * <details><summary>中文</summary>
     * 解析一个 JSON 值，支持对象、数组及标量；容器类型由读取上下文和特性决定。
     * </details>
     *
     * @param in the input stream to parse, or null
     * @param context the reader provider and parsing settings, not null
     * @return the parsed Java value, or null
     * @throws JSONException if a parsing error occurs
     * @throws NullPointerException if received context is null
     * @since 2.0.47
     */
    static Object parse(InputStream in, JSONReader.Context context) {
        if (in == null) {
            return null;
        }

        ObjectReader<?> objectReader = context.getObjectReader(Object.class);
        try (JSONReaderUTF8 reader = new JSONReaderUTF8(context, in)) {
            Object object = objectReader.readObject(reader, null, null, 0);
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses one JSON value from the input.
     * The result may be an object, array, string, number, boolean or null.
     * Object and array representations depend on the reader context and features.
     *
     * <p>Returns null for a null input.</p>
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 解析一个 JSON 值，支持对象、数组及标量；容器类型由读取上下文和特性决定。
     * </details>
     *
     * @param in the input stream to parse, or null
     * @param charset the charset used to decode the input; null selects UTF-8
     * @return the parsed Java value, or null
     * @throws JSONException if a parsing error occurs
     * @since 2.0.61
     */
    static Object parse(InputStream in, Charset charset) {
        return parse(in, charset, JSONFactory.createReadContext());
    }

    /**
     * Parses one JSON value from the input.
     * The result may be an object, array, string, number, boolean or null.
     * Object and array representations depend on the reader context and features.
     *
     * <p>Returns null for a null input.</p>
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 解析一个 JSON 值，支持对象、数组及标量；容器类型由读取上下文和特性决定。
     * </details>
     *
     * @param in the input stream to parse, or null
     * @param charset the charset used to decode the input; null selects UTF-8
     * @param context the reader provider and parsing settings, not null
     * @return the parsed Java value, or null
     * @throws JSONException if a parsing error occurs
     * @throws NullPointerException if received context is null
     * @since 2.0.61
     */
    static Object parse(InputStream in, Charset charset, JSONReader.Context context) {
        if (in == null) {
            return null;
        }

        ObjectReader<?> objectReader = context.getObjectReader(Object.class);
        try (JSONReader reader = JSONReader.of(in, charset, context)) {
            Object object = objectReader.readObject(reader, null, null, 0);
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into {@link JSONObject}.
     * Returns null for null input or an empty input.
     * The JSON literal {@code null} also returns null.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param text the JSON text
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONObject parseObject(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(text, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0L);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into {@link JSONObject}.
     * Returns null for null input or an empty input.
     * The JSON literal {@code null} also returns null.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param text the JSON text
     * @param features reader features to enable in addition to the defaults
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONObject parseObject(String text, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(text, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input slice into {@link JSONObject}.
     * Returns null for null input or an empty input, or when length is zero.
     * The JSON literal {@code null} also returns null.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param text the JSON text
     * @param offset the zero-based offset in UTF-16 code units
     * @param length the number of UTF-16 code units to read, not the end index
     * @param features reader features to enable in addition to the defaults
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONObject parseObject(String text, int offset, int length, JSONReader.Feature... features) {
        if (text == null || text.isEmpty() || length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(text, offset, length, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input slice into {@link JSONObject}.
     * Returns null for null input or an empty input, or when length is zero.
     * The JSON literal {@code null} also returns null.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param text the JSON text
     * @param offset the zero-based offset in UTF-16 code units
     * @param length the number of UTF-16 code units to read, not the end index
     * @param context the reader provider and parsing settings, not null
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     * @throws NullPointerException if received context is null
     * @since 2.0.30
     */
    static JSONObject parseObject(String text, int offset, int length, JSONReader.Context context) {
        if (text == null || text.isEmpty() || length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text, offset, length, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into {@link JSONObject}.
     * Returns null for null input or an empty input.
     * The JSON literal {@code null} also returns null.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param text the JSON text
     * @param context the reader provider and parsing settings, not null
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     * @throws NullPointerException if received context is null
     */
    static JSONObject parseObject(String text, JSONReader.Context context) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the input into {@link JSONObject}.
     * Returns null for null input, including an input with no value after whitespace.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param input the input to parse, or null
     * @param features reader features to enable in addition to the defaults
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONObject parseObject(Reader input, JSONReader.Feature... features) {
        if (input == null) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(input, context)) {
            if (reader.isEnd()) {
                return null;
            }

            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the input into {@link JSONObject}.
     * Returns null for null input, including an input with no value after whitespace.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <p>Input bytes are decoded as UTF-8.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param input the input to parse, or null
     * @param features reader features to enable in addition to the defaults
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONObject parseObject(InputStream input, JSONReader.Feature... features) {
        if (input == null) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(input, StandardCharsets.UTF_8, context)) {
            if (reader.isEnd()) {
                return null;
            }

            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input into {@link JSONObject}.
     * Returns null for null input or an empty input.
     * The JSON literal {@code null} also returns null.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param bytes the UTF-8 JSON text
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONObject parseObject(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(bytes, context)) {
            if (reader.nextIfNull()) {
                return null;
            }

            JSONObject object = new JSONObject();
            reader.read(object, 0L);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the character input into {@link JSONObject}.
     * Returns null for null input or an empty input.
     * The JSON literal {@code null} also returns null.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param chars the JSON text as UTF-16 code units
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONObject parseObject(char[] chars) {
        if (chars == null || chars.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(chars, context)) {
            if (reader.nextIfNull()) {
                return null;
            }

            JSONObject object = new JSONObject();
            reader.read(object, 0L);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the input into {@link JSONObject}.
     * Returns null for null input.
     * The JSON literal {@code null} also returns null.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param in the input stream to parse, or null
     * @param charset the charset used to decode the input; null selects UTF-8
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONObject parseObject(InputStream in, Charset charset) {
        if (in == null) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(in, charset, context)) {
            if (reader.nextIfNull()) {
                return null;
            }

            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the input into {@link JSONObject}.
     * Returns null for null input, including an input with no value after whitespace.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param input the input to parse, or null
     * @param charset the charset used to decode the input; null selects UTF-8
     * @param context the reader provider and parsing settings, not null
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     *
     * @since 2.0.47
     */
    static JSONObject parseObject(InputStream input, Charset charset, JSONReader.Context context) {
        if (input == null) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(input, charset, context)) {
            if (reader.isEnd()) {
                return null;
            }

            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the URL into {@link JSONObject}.
     * Returns null for null input.
     *
     * <p>Opens the URL as UTF-8 JSON and closes the opened stream after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param url the URL to open, or null
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if an I/O error or parsing error occurs
     * @see URL#openStream()
     * @see JSON#parseObject(InputStream, Charset)
     */
    static JSONObject parseObject(URL url) {
        if (url == null) {
            return null;
        }

        try (InputStream is = url.openStream()) {
            return parseObject(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JSONException("JSON#parseObject cannot parse '" + url + "'", e);
        }
    }

    /**
     * Parses JSON from the encoded byte input into {@link JSONObject}.
     * Returns null for null input or an empty input.
     * The JSON literal {@code null} also returns null.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param bytes the UTF-8 JSON text
     * @param features reader features to enable in addition to the defaults
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONObject parseObject(byte[] bytes, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(bytes, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input slice into {@link JSONObject}.
     * Returns null for null input or an empty input, or when length is zero.
     * The JSON literal {@code null} also returns null.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param bytes the UTF-8 JSON text
     * @param offset the zero-based offset in bytes
     * @param length the number of bytes to read, not the end index
     * @param features reader features to enable in addition to the defaults
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONObject parseObject(byte[] bytes, int offset, int length, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0 || length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(bytes, offset, length, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the character input slice into {@link JSONObject}.
     * Returns null for null input or an empty input, or when length is zero.
     * The JSON literal {@code null} also returns null.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param chars the JSON text as UTF-16 code units
     * @param offset the zero-based offset in UTF-16 code units
     * @param length the number of UTF-16 code units to read, not the end index
     * @param features reader features to enable in addition to the defaults
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONObject parseObject(char[] chars, int offset, int length, JSONReader.Feature... features) {
        if (chars == null || chars.length == 0 || length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(chars, offset, length, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input slice into {@link JSONObject}.
     * Returns null for null input or an empty input, or when length is zero.
     * The JSON literal {@code null} also returns null.
     *
     * <p>The selected bytes are decoded with the supplied charset; they must contain JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param bytes the encoded JSON text
     * @param offset the zero-based offset in bytes
     * @param length the number of bytes to read, not the end index
     * @param charset the input charset: UTF-8, UTF-16, US-ASCII or ISO-8859-1; not null
     * @param features reader features to enable in addition to the defaults
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException if a parsing error occurs
     * @see JSON#parseObject(byte[], int, int, JSONReader.Feature...)
     */
    static JSONObject parseObject(
            byte[] bytes,
            int offset,
            int length,
            Charset charset,
            JSONReader.Feature... features
    ) {
        if (bytes == null || bytes.length == 0 || length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(bytes, offset, length, charset, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param clazz the target class
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Class<T> clazz) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider);
        ObjectReader<T> objectReader = provider.getObjectReader(
                clazz,
                (defaultReaderFeatures & JSONReader.Feature.FieldBased.mask) != 0
        );

        try (JSONReader reader = JSONReader.of(text, context)) {
            T object = objectReader.readObject(reader, clazz, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into the requested Java type.
     * Returns null for null input or an empty input.
     * The JSON literal {@code null} also returns null.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param clazz the target class
     * @param filter the filter to apply while reading
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(
            String text,
            Class<T> clazz,
            Filter filter,
            JSONReader.Feature... features
    ) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(filter, features);
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

        try (JSONReader reader = JSONReader.of(text, context)) {
            if (reader.nextIfNull()) {
                return null;
            }

            T object = objectReader.readObject(reader, clazz, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into the requested Java type.
     * Returns null for null input or an empty input.
     * The JSON literal {@code null} also returns null.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param type the target type, including any generic arguments
     * @param format the date/time pattern or supported format name, such as {@code millis}, {@code unixtime} or {@code iso8601}
     * @param filters filters to apply while reading
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(
            String text,
            Type type,
            String format,
            Filter[] filters,
            JSONReader.Feature... features
    ) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        JSONReader.Context context = new JSONReader.Context(
                JSONFactory.getDefaultObjectReaderProvider(),
                null,
                filters,
                features
        );
        context.setDateFormat(format);

        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader<T> objectReader = context.provider.getObjectReader(type, fieldBased);

        try (JSONReader reader = JSONReader.of(text, context)) {
            if (reader.nextIfNull()) {
                return null;
            }

            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param type the target type, including any generic arguments
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Type type) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider);
        final ObjectReader<T> objectReader = provider.getObjectReader(
                type,
                (defaultReaderFeatures & JSONReader.Feature.FieldBased.mask) != 0
        );

        try (JSONReader reader = JSONReader.of(text, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param type the target type, including any generic arguments
     * @param context the reader provider and parsing settings, not null
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     * @since 2.0.52
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Type type, JSONReader.Context context) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(text, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses a JSON object into a map whose value types are configured by key.
     * Returns null for a null or empty input string.
     *
     * <details><summary>中文</summary>
     * 解析 JSON 对象为按键配置值类型的 Map；空输入返回 null。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param type the map type and per-key value types
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     * @since 2.0.34
     */
    static <T extends Map<String, Object>> T parseObject(String text, MapMultiValueType<T> type) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        final ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(text, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses a JSON array into an {@code Object[]} using one target type per position.
     * Returns null for a null or empty string. The inferred {@code T} must be compatible
     * with {@code Object[]}; use {@link #parseArray(String, Type...)} for a list result.
     *
     * <details><summary>中文</summary>
     * 按位置指定类型解析 JSON 数组，返回 Object[]；空输入返回 null。泛型结果必须与 Object[] 兼容；需要 List 时使用 parseArray。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param types the target type for each array position, in order
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     * @see MultiType
     * @see JSON#parseObject(String, Type)
     */
    static <T> T parseObject(String text, Type... types) {
        return parseObject(text, new MultiType(types));
    }

    /**
     * Parses JSON from the string input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param typeReference the captured target type, including generic arguments
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, TypeReference<T> typeReference, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        Type type = typeReference.getType();
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader<T> objectReader = context.provider.getObjectReader(type, fieldBased);

        try (JSONReader reader = JSONReader.of(text, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param typeReference the captured target type, including generic arguments
     * @param filter the filter to apply while reading
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(
            String text,
            TypeReference<T> typeReference,
            Filter filter,
            JSONReader.Feature... features
    ) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(filter, features);
        Type type = typeReference.getType();
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader<T> objectReader = context.provider.getObjectReader(type, fieldBased);

        try (JSONReader reader = JSONReader.of(text, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param clazz the target class
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Class<T> clazz, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

        try (JSONReader reader = JSONReader.of(text, context)) {
            T object = objectReader.readObject(reader, clazz, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input slice into the requested Java type.
     * Returns null for null input or an empty input, or when length is zero.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param offset the zero-based offset in UTF-16 code units
     * @param length the number of UTF-16 code units to read, not the end index
     * @param clazz the target class
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, int offset, int length, Class<T> clazz, JSONReader.Feature... features) {
        if (text == null || text.isEmpty() || length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

        try (JSONReader reader = JSONReader.of(text, offset, length, context)) {
            T object = objectReader.readObject(reader, clazz, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param clazz the target class
     * @param context the reader provider and parsing settings, not null
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     * @throws NullPointerException if received context is null
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Class<T> clazz, JSONReader.Context context) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

        try (JSONReader reader = JSONReader.of(text, context)) {
            T object = objectReader.readObject(reader, clazz, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param clazz the target class
     * @param format the date/time pattern or supported format name, such as {@code millis}, {@code unixtime} or {@code iso8601}
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Class<T> clazz, String format, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        if (format != null && !format.isEmpty()) {
            context.setDateFormat(format);
        }

        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

        try (JSONReader reader = JSONReader.of(text, context)) {
            T object = objectReader.readObject(reader, clazz, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param type the target type, including any generic arguments
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Type type, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(text, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param type the target type, including any generic arguments
     * @param filter the filter to apply while reading
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Type type, Filter filter, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(filter, features);
        ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(text, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the string input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param type the target type, including any generic arguments
     * @param format the date/time pattern or supported format name, such as {@code millis}, {@code unixtime} or {@code iso8601}
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Type type, String format, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        if (format != null && !format.isEmpty()) {
            context.setDateFormat(format);
        }

        try (JSONReader reader = JSONReader.of(text, context)) {
            ObjectReader<T> objectReader = context.getObjectReader(type);
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the character input slice into the requested Java type.
     * Returns null for null input or an empty input, or when length is zero.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param chars the JSON text as UTF-16 code units
     * @param offset the zero-based offset in UTF-16 code units
     * @param length the number of UTF-16 code units to read, not the end index
     * @param type the target type, including any generic arguments
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     * @since 2.0.13
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(char[] chars, int offset, int length, Type type, JSONReader.Feature... features) {
        if (chars == null || chars.length == 0 || length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(chars, offset, length, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the character input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param chars the JSON text as UTF-16 code units
     * @param clazz the target class
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(char[] chars, Class<T> clazz) {
        if (chars == null || chars.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        final ObjectReader<T> objectReader = context.getObjectReader(clazz);

        try (JSONReader reader = JSONReader.of(chars, context)) {
            T object = objectReader.readObject(reader, clazz, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input slice into the requested Java type.
     * Returns null for null input or an empty input, or when length is zero.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the UTF-8 JSON text
     * @param offset the zero-based offset in bytes
     * @param length the number of bytes to read, not the end index
     * @param type the target type, including any generic arguments
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     * @since 2.0.13
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, int offset, int length, Type type, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0 || length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(bytes, offset, length, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the UTF-8 JSON text
     * @param type the target type, including any generic arguments
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, Type type) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        final ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(bytes, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the UTF-8 JSON text
     * @param clazz the target class
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        JSONReader.Context context = new JSONReader.Context(provider);
        ObjectReader<T> objectReader = provider.getObjectReader(
                clazz,
                (JSONFactory.defaultReaderFeatures & JSONReader.Feature.FieldBased.mask) != 0
        );

        try (JSONReader reader = JSONReader.of(bytes, context)) {
            T object = objectReader.readObject(reader, clazz, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the UTF-8 JSON text
     * @param clazz the target class
     * @param filter the filter to apply while reading
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(
            byte[] bytes,
            Class<T> clazz,
            Filter filter,
            JSONReader.Feature... features
    ) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(filter, features);
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

        try (JSONReader reader = JSONReader.of(bytes, context)) {
            T object = objectReader.readObject(reader, clazz, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the UTF-8 JSON text
     * @param clazz the target class
     * @param context the reader provider and parsing settings, not null
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     * @throws NullPointerException if received context is null
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(
            byte[] bytes,
            Class<T> clazz,
            JSONReader.Context context
    ) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        final ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

        try (JSONReader reader = JSONReader.of(bytes, context)) {
            T object = objectReader.readObject(reader, clazz, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the UTF-8 JSON text
     * @param type the target type, including any generic arguments
     * @param format the date/time pattern or supported format name, such as {@code millis}, {@code unixtime} or {@code iso8601}
     * @param filters filters to apply while reading
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static <T> T parseObject(
            byte[] bytes,
            Type type,
            String format,
            Filter[] filters,
            JSONReader.Feature... features
    ) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        JSONReader.Context context = new JSONReader.Context(
                JSONFactory.getDefaultObjectReaderProvider(),
                null,
                filters,
                features
        );
        context.setDateFormat(format);

        return parseObject(bytes, type, context);
    }

    /**
     * Parses JSON from the encoded byte input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the UTF-8 JSON text
     * @param type the target type, including any generic arguments
     * @param context the reader provider and parsing settings, not null
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     * @throws NullPointerException if received context is null
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, Type type, JSONReader.Context context) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader<T> objectReader = context.provider.getObjectReader(type, fieldBased);

        try (JSONReader reader = JSONReader.of(bytes, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the UTF-8 JSON text
     * @param clazz the target class
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, Class<T> clazz, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        final ObjectReader<T> objectReader = context.getObjectReader(clazz);

        try (JSONReader reader = JSONReader.of(bytes, context)) {
            T object = objectReader.readObject(reader, clazz, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the UTF-8 JSON text
     * @param type the target type, including any generic arguments
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, Type type, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        final ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(bytes, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the character input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param chars the JSON text as UTF-16 code units
     * @param objectClass the target class
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(char[] chars, Class<T> objectClass, JSONReader.Feature... features) {
        if (chars == null || chars.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        final ObjectReader<T> objectReader = context.getObjectReader(objectClass);

        try (JSONReader reader = JSONReader.of(chars, context)) {
            T object = objectReader.readObject(reader, objectClass, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the character input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param chars the JSON text as UTF-16 code units
     * @param type the target type, including any generic arguments
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(char[] chars, Type type, JSONReader.Feature... features) {
        if (chars == null || chars.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        final ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(chars, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the UTF-8 JSON text
     * @param type the target type, including any generic arguments
     * @param filter the filter to apply while reading
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, Type type, Filter filter, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(filter, features);
        final ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(bytes, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input into the requested Java type.
     * Returns null for null input or an empty input.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the UTF-8 JSON text
     * @param type the target type, including any generic arguments
     * @param format the date/time pattern or supported format name, such as {@code millis}, {@code unixtime} or {@code iso8601}
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, Type type, String format, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        if (format != null && !format.isEmpty()) {
            context.setDateFormat(format);
        }
        final ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(bytes, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the byte buffer into the requested Java type.
     * Returns null for null input.
     *
     * <p>The buffer must contain UTF-8 JSON text.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param buffer the buffer containing UTF-8 JSON text
     * @param objectClass the target class
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(ByteBuffer buffer, Class<T> objectClass) {
        if (buffer == null) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        final ObjectReader<T> objectReader = context.getObjectReader(objectClass);

        try (JSONReader reader = JSONReader.of(buffer, null, context)) {
            T object = objectReader.readObject(reader, objectClass, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the input into the requested Java type.
     * Returns null for null input, including an input with no value after whitespace.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param input the input to parse, or null
     * @param type the target type, including any generic arguments
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(Reader input, Type type, JSONReader.Feature... features) {
        if (input == null) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        final ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(input, context)) {
            if (reader.isEnd()) {
                return null;
            }

            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the input into the requested Java type.
     * Returns null for null input, including an input with no value after whitespace.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <p>Input bytes are decoded as UTF-8.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param input the input to parse, or null
     * @param type the target type, including any generic arguments
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(InputStream input, Type type, JSONReader.Feature... features) {
        if (input == null) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        context.config(features);
        final ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(input, StandardCharsets.UTF_8, context)) {
            if (reader.isEnd()) {
                return null;
            }

            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the input into the requested Java type.
     * Returns null for null input, including an input with no value after whitespace.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param input the input to parse, or null
     * @param charset the charset used to decode the input; null selects UTF-8
     * @param type the target type, including any generic arguments
     * @param context the reader provider and parsing settings, not null
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(InputStream input, Charset charset, Type type, JSONReader.Context context) {
        if (input == null) {
            return null;
        }

        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader<T> objectReader = context.provider.getObjectReader(type, fieldBased);

        try (JSONReader reader = JSONReader.of(input, charset, context)) {
            if (reader.isEnd()) {
                return null;
            }

            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the input into the requested Java type.
     * Returns null for null input, including an input with no value after whitespace.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param input the input to parse, or null
     * @param charset the charset used to decode the input; null selects UTF-8
     * @param type the target type, including any generic arguments
     * @param context the reader provider and parsing settings, not null
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(InputStream input, Charset charset, Class<T> type, JSONReader.Context context) {
        if (input == null) {
            return null;
        }

        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader<T> objectReader = context.provider.getObjectReader(type, fieldBased);

        try (JSONReader reader = JSONReader.of(input, charset, context)) {
            if (reader.isEnd()) {
                return null;
            }

            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the URL into the requested Java type.
     * Returns null for null input.
     *
     * <p>Opens the URL as UTF-8 JSON and closes the opened stream after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param url the URL to open, or null
     * @param type the target type, including any generic arguments
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if an I/O error or parsing error occurs
     * @see URL#openStream()
     * @see JSON#parseObject(InputStream, Type, JSONReader.Feature...)
     * @since 2.0.4
     */
    static <T> T parseObject(URL url, Type type, JSONReader.Feature... features) {
        if (url == null) {
            return null;
        }

        try (InputStream is = url.openStream()) {
            return parseObject(is, type, features);
        } catch (IOException e) {
            throw new JSONException("parseObject error", e);
        }
    }

    /**
     * Parses JSON from the URL into the requested Java type.
     * Returns null for null input.
     *
     * <p>Opens the URL as UTF-8 JSON and closes the opened stream after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param url the URL to open, or null
     * @param objectClass the target class
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if an I/O error or parsing error occurs
     * @see URL#openStream()
     * @see JSON#parseObject(InputStream, Type, JSONReader.Feature...)
     * @since 2.0.9
     */
    static <T> T parseObject(URL url, Class<T> objectClass, JSONReader.Feature... features) {
        if (url == null) {
            return null;
        }

        try (InputStream is = url.openStream()) {
            return parseObject(is, objectClass, features);
        } catch (IOException e) {
            throw new JSONException("JSON#parseObject cannot parse '" + url + "' to '" + objectClass + "'", e);
        }
    }

    /**
     * Parses the JSON stream of the url as a {@link JSONObject} and calls the function
     * to convert it to {@code T}. Returns {@code null} if received {@link URL} is {@code null}.
     *
     * <p>Opens the URL as UTF-8 JSON and closes the opened stream after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param url the URL to open, or null
     * @param function the specified converter
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if an I/O error or parsing error occurs
     * @see URL#openStream()
     * @see JSON#parseObject(InputStream, JSONReader.Feature...)
     * @since 2.0.4
     */
    static <T> T parseObject(URL url, Function<JSONObject, T> function, JSONReader.Feature... features) {
        if (url == null) {
            return null;
        }

        try (InputStream is = url.openStream()) {
            JSONObject object = parseObject(is, features);
            if (object == null) {
                return null;
            }
            return function.apply(object);
        } catch (IOException e) {
            throw new JSONException("JSON#parseObject cannot parse '" + url + "'", e);
        }
    }

    /**
     * Parses JSON from the input into the requested Java type.
     * Returns null for null input.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <p>Input bytes are decoded as UTF-8.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param input the input to parse, or null
     * @param type the target type, including any generic arguments
     * @param format the date/time pattern or supported format name, such as {@code millis}, {@code unixtime} or {@code iso8601}
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(InputStream input, Type type, String format, JSONReader.Feature... features) {
        if (input == null) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        if (format != null && !format.isEmpty()) {
            context.setDateFormat(format);
        }
        ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(input, StandardCharsets.UTF_8, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the input into the requested Java type.
     * Returns null for null input.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param input the input to parse, or null
     * @param charset the charset used to decode the input; null selects UTF-8
     * @param type the target type, including any generic arguments
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(InputStream input, Charset charset, Type type, JSONReader.Feature... features) {
        if (input == null) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(input, charset, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input slice into the requested Java type.
     * Returns null for null input or an empty input, or when length is zero.
     *
     * <p>The selected bytes are decoded with the supplied charset; they must contain JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the encoded JSON text
     * @param offset the zero-based offset in bytes
     * @param length the number of bytes to read, not the end index
     * @param charset the input charset: UTF-8, UTF-16, US-ASCII or ISO-8859-1; not null
     * @param type the target type, including any generic arguments
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, int offset, int length, Charset charset, Type type) {
        if (bytes == null || bytes.length == 0 || length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        final ObjectReader<T> objectReader = context.getObjectReader(type);
        try (JSONReader reader = JSONReader.of(bytes, offset, length, charset, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input slice into the requested Java type.
     * Returns null for null input or an empty input, or when length is zero.
     *
     * <p>The selected bytes are decoded with the supplied charset; they must contain JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the encoded JSON text
     * @param offset the zero-based offset in bytes
     * @param length the number of bytes to read, not the end index
     * @param charset the input charset: UTF-8, UTF-16, US-ASCII or ISO-8859-1; not null
     * @param type the target type, including any generic arguments
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, int offset, int length, Charset charset, Class<T> type) {
        if (bytes == null || bytes.length == 0 || length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        ObjectReader<T> objectReader = context.getObjectReader(type);
        try (JSONReader reader = JSONReader.of(bytes, offset, length, charset, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses JSON from the encoded byte input slice into the requested Java type.
     * Returns null for null input or an empty input, or when length is zero.
     *
     * <p>The selected bytes are decoded with the supplied charset; they must contain JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the result type
     * @param bytes the encoded JSON text
     * @param offset the zero-based offset in bytes
     * @param length the number of bytes to read, not the end index
     * @param charset the input charset: UTF-8, UTF-16, US-ASCII or ISO-8859-1; not null
     * @param type the target type, including any generic arguments
     * @param features reader features to enable in addition to the defaults
     * @return {@code T} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(
            byte[] bytes,
            int offset,
            int length,
            Charset charset,
            Class<T> type,
            JSONReader.Feature... features
    ) {
        if (bytes == null || bytes.length == 0 || length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        ObjectReader<T> objectReader = context.getObjectReader(type);

        try (JSONReader reader = JSONReader.of(bytes, offset, length, charset, context)) {
            T object = objectReader.readObject(reader, type, null, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses newline-delimited UTF-8 JSON records and passes each converted value to the consumer.
     * The final nonempty record is delivered even without a trailing delimiter.
     * Records are split before JSON parsing, so the delimiter must not occur inside a record.
     * The input remains open; consumer exceptions propagate to the caller.
     *
     * <details><summary>中文</summary>
     * 按分隔符逐条解析并调用消费器；最后一条非空记录无需结尾分隔符。分隔符不可出现在记录内部。输入保持打开，消费器异常向调用方传播。
     * </details>
     *
     * @param <T> the record type
     * @param input the input to consume without closing, not null
     * @param type the target type, including any generic arguments
     * @param consumer the callback invoked synchronously for each parsed record
     * @param features reader features to enable in addition to the defaults
     * @throws JSONException if an I/O error or parsing error occurs
     * @throws NullPointerException if the specified stream is null
     * @since 2.0.2
     */
    static <T> void parseObject(InputStream input, Type type, Consumer<T> consumer, JSONReader.Feature... features) {
        parseObject(input, StandardCharsets.UTF_8, '\n', type, consumer, features);
    }

    /**
     * Parses delimiter-separated JSON records and passes each converted value to the consumer.
     * The final nonempty record is delivered even without a trailing delimiter.
     * Records are split before JSON parsing, so the delimiter must not occur inside a record.
     * The input remains open; consumer exceptions propagate to the caller.
     *
     * <p>The delimiter is matched against individual bytes; use a positive ASCII delimiter
     * and an encoding in which it occupies one byte. For character-based framing, use the Reader overload.</p>
     *
     * <details><summary>中文</summary>
     * 按分隔符逐条解析并调用消费器；最后一条非空记录无需结尾分隔符。分隔符不可出现在记录内部。输入保持打开，消费器异常向调用方传播。
     * </details>
     *
     * @param <T> the record type
     * @param input the input to consume without closing, not null
     * @param charset the input charset: UTF-8, UTF-16, US-ASCII or ISO-8859-1; not null
     * @param delimiter the single-byte record separator
     * @param type the target type, including any generic arguments
     * @param consumer the callback invoked synchronously for each parsed record
     * @param features reader features to enable in addition to the defaults
     * @throws JSONException if an I/O error or parsing error occurs
     * @throws NullPointerException if the specified stream is null
     * @since 2.0.2
     */
    @SuppressWarnings("unchecked")
    static <T> void parseObject(
            InputStream input,
            Charset charset,
            char delimiter,
            Type type,
            Consumer<T> consumer,
            JSONReader.Feature... features
    ) {
        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        final CacheItem cacheItem = CACHE_ITEMS[cacheIndex];
        byte[] bytes = BYTES_UPDATER.getAndSet(cacheItem, null);
        int bufferSize = 512 * 1024;
        if (bytes == null) {
            bytes = new byte[bufferSize];
        }

        int offset = 0, start = 0, end;
        ObjectReader<? extends T> objectReader = null;

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try {
            while (true) {
                int n = input.read(bytes, offset, bytes.length - offset);
                int k = offset;
                if (n != -1) {
                    offset += n;
                }
                boolean dispose = false;

                // At EOF, the end of the buffer also terminates the final record.
                // <details><summary>中文</summary>流结束时，缓冲区末尾也作为最后一条记录的结束位置。</details>
                for (; k <= offset; ++k) {
                    if (k == offset ? n == -1 && start < offset : bytes[k] == delimiter) {
                        end = k;

                        JSONReader jsonReader = JSONReader.of(bytes, start, end - start, charset, context);
                        if (objectReader == null) {
                            objectReader = context.getObjectReader(type);
                        }

                        T object = objectReader.readObject(jsonReader, type, null, 0);
                        if (jsonReader.resolveTasks != null) {
                            jsonReader.handleResolveTasks(object);
                        }
                        if (jsonReader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                            throw new JSONException(jsonReader.info("input not end"));
                        }

                        consumer.accept(
                                object
                        );
                        start = end + 1;
                        dispose = true;
                    }
                }

                if (n == -1) {
                    break;
                }
                if (offset == bytes.length) {
                    if (dispose) {
                        int len = bytes.length - start;
                        System.arraycopy(bytes, start, bytes, 0, len);
                        start = 0;
                        offset = len;
                    } else {
                        bytes = Arrays.copyOf(bytes, bytes.length + bufferSize);
                    }
                }
            }
        } catch (IOException e) {
            throw new JSONException("JSON#parseObject cannot parse the 'InputStream' to '" + type + "'", e);
        } finally {
            BYTES_UPDATER.lazySet(cacheItem, bytes);
        }
    }

    /**
     * Parses delimiter-separated JSON records and passes each converted value to the consumer.
     * The final nonempty record is delivered even without a trailing delimiter.
     * Records are split before JSON parsing, so the delimiter must not occur inside a record.
     * The input remains open; consumer exceptions propagate to the caller.
     *
     * <details><summary>中文</summary>
     * 按分隔符逐条解析并调用消费器；最后一条非空记录无需结尾分隔符。分隔符不可出现在记录内部。输入保持打开，消费器异常向调用方传播。
     * </details>
     *
     * @param <T> the record type
     * @param input the input to consume without closing, not null
     * @param delimiter the character separating JSON records
     * @param type the target type, including any generic arguments
     * @param consumer the callback invoked synchronously for each parsed record
     * @throws JSONException if an I/O error or parsing error occurs
     * @throws NullPointerException if the specified reader is null
     * @since 2.0.2
     */
    @SuppressWarnings("unchecked")
    static <T> void parseObject(Reader input, char delimiter, Type type, Consumer<T> consumer) {
        final int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        final CacheItem cacheItem = CACHE_ITEMS[cacheIndex];
        char[] chars = CHARS_UPDATER.getAndSet(cacheItem, null);
        if (chars == null) {
            chars = new char[8192];
        }

        int offset = 0, start = 0, end;
        ObjectReader<? extends T> objectReader = null;

        final JSONReader.Context context = JSONFactory.createReadContext();
        try {
            while (true) {
                int n = input.read(chars, offset, chars.length - offset);
                int k = offset;
                if (n != -1) {
                    offset += n;
                }
                boolean dispose = false;

                for (; k <= offset; ++k) {
                    if (k == offset ? n == -1 && start < offset : chars[k] == delimiter) {
                        end = k;

                        JSONReader jsonReader = JSONReader.of(chars, start, end - start, context);
                        if (objectReader == null) {
                            objectReader = context.getObjectReader(type);
                        }

                        T object = objectReader.readObject(jsonReader, type, null, 0);
                        if (jsonReader.resolveTasks != null) {
                            jsonReader.handleResolveTasks(object);
                        }
                        if (jsonReader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                            throw new JSONException(jsonReader.info("input not end"));
                        }
                        consumer.accept(object);
                        start = end + 1;
                        dispose = true;
                    }
                }

                if (n == -1) {
                    break;
                }
                if (offset == chars.length) {
                    if (dispose) {
                        int len = chars.length - start;
                        System.arraycopy(chars, start, chars, 0, len);
                        start = 0;
                        offset = len;
                    } else {
                        chars = Arrays.copyOf(chars, chars.length + 8192);
                    }
                }
            }
        } catch (IOException e) {
            throw new JSONException("JSON#parseObject cannot parse the 'Reader' to '" + type + "'", e);
        } finally {
            CHARS_UPDATER.lazySet(cacheItem, chars);
        }
    }

    /**
     * Parses JSON from the string input into {@link JSONArray}.
     * Returns null for null input or an empty input.
     * The JSON literal {@code null} also returns null.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param text the JSON text
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONArray parseArray(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(text, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray();
            reader.read(array);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return array;
        }
    }

    /**
     * Parses JSON from the encoded byte input into {@link JSONArray}.
     * Returns null for null input or an empty input.
     * The JSON literal {@code null} also returns null.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param bytes the UTF-8 JSON text
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONArray parseArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(bytes, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray();
            reader.read(array);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return array;
        }
    }

    /**
     * Parses JSON from the encoded byte input slice into {@link JSONArray}.
     * Returns null for null input or an empty input, or when length is zero.
     * The JSON literal {@code null} also returns null.
     *
     * <p>The selected bytes are decoded with the supplied charset; they must contain JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param bytes the encoded JSON text
     * @param offset the zero-based offset in bytes
     * @param length the number of bytes to read, not the end index
     * @param charset the input charset: UTF-8, UTF-16, US-ASCII or ISO-8859-1; not null
     * @throws JSONException if a parsing error occurs
     * @since 2.0.13
     */
    static JSONArray parseArray(byte[] bytes, int offset, int length, Charset charset) {
        if (bytes == null || bytes.length == 0 || length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(bytes, offset, length, charset, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray();
            reader.read(array);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return array;
        }
    }

    /**
     * Parses JSON from the character input into {@link JSONArray}.
     * Returns null for null input or an empty input.
     * The JSON literal {@code null} also returns null.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param chars the JSON text as UTF-16 code units
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONArray parseArray(char[] chars) {
        if (chars == null || chars.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(chars, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray();
            reader.read(array);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return array;
        }
    }

    /**
     * Parses JSON from the string input into {@link JSONArray}.
     * Returns null for null input or an empty input.
     * The JSON literal {@code null} also returns null.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param text the JSON text
     * @param features reader features to enable in addition to the defaults
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static JSONArray parseArray(String text, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(text, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray();
            reader.read(array);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return array;
        }
    }

    /**
     * Parses JSON from the URL into {@link JSONArray}.
     * Returns null for null input.
     *
     * <p>Opens the URL as UTF-8 JSON and closes the opened stream after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param url the URL to open, or null
     * @param features reader features to enable in addition to the defaults
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException if an I/O error or parsing error occurs
     * @see URL#openStream()
     * @see JSON#parseArray(InputStream, JSONReader.Feature...)
     */
    static JSONArray parseArray(URL url, JSONReader.Feature... features) {
        if (url == null) {
            return null;
        }

        try (InputStream is = url.openStream()) {
            return parseArray(is, features);
        } catch (IOException e) {
            throw new JSONException("JSON#parseArray cannot parse '" + url + "' to '" + JSONArray.class + "'", e);
        }
    }

    /**
     * Parses JSON from the input into {@link JSONArray}.
     * Returns null for null input.
     * The JSON literal {@code null} also returns null.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param input the input to parse, or null
     * @param features reader features to enable in addition to the defaults
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException if an I/O error or parsing error occurs
     */
    static JSONArray parseArray(Reader input, JSONReader.Feature... features) {
        if (input == null) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(input, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray();
            reader.read(array);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return array;
        }
    }

    /**
     * Parses JSON from the input into {@link JSONArray}.
     * Returns null for null input.
     * The JSON literal {@code null} also returns null.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <p>Input bytes are decoded as UTF-8.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param in the input stream to parse, or null
     * @param features reader features to enable in addition to the defaults
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException if an I/O error or parsing error occurs
     */
    static JSONArray parseArray(InputStream in, JSONReader.Feature... features) {
        if (in == null) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(in, StandardCharsets.UTF_8, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray();
            reader.read(array);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return array;
        }
    }

    /**
     * Parses JSON from the input into {@link JSONArray}.
     * Returns null for null input.
     * The JSON literal {@code null} also returns null.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param in the input stream to parse, or null
     * @param charset the charset used to decode the input; null selects UTF-8
     * @param context the reader provider and parsing settings, not null
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException if an I/O error or parsing error occurs
     */
    static JSONArray parseArray(InputStream in, Charset charset, JSONReader.Context context) {
        if (in == null) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(in, charset, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray();
            reader.read(array);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return array;
        }
    }

    /**
     * Parses JSON from the string input into a list of the requested element type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the list element type
     * @param text the JSON text
     * @param type the type of every list element
     * @param features reader features to enable in addition to the defaults
     * @return {@link List} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> List<T> parseArray(String text, Type type, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(text, context)) {
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return list;
        }
    }

    /**
     * Parses JSON from the string input into a list of the requested element type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the list element type
     * @param text the JSON text
     * @param type the type of every list element
     * @return {@link List} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> List<T> parseArray(String text, Type type) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(text, context)) {
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return list;
        }
    }

    /**
     * Parses JSON from the string input into a list of the requested element type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the list element type
     * @param text the JSON text
     * @param type the type of every list element
     * @return {@link List} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> List<T> parseArray(String text, Class<T> type) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(text, context)) {
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return list;
        }
    }

    /**
     * Parses a JSON array into a list using one target type per array position.
     * The type array describes positions, not type arguments of a single element type.
     * Returns null for a null or empty input string.
     *
     * <details><summary>中文</summary>
     * 按数组位置逐项使用指定类型解析为列表；类型数组描述各位置的类型，并非单个元素类型的泛型参数。空输入返回 null。
     * </details>
     *
     * @param <T> the list element type
     * @param text the JSON text
     * @param types the target type for each array position, in order
     * @return {@link List} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> List<T> parseArray(String text, Type... types) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(text, context)) {
            List<T> list = reader.readList(types);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return list;
        }
    }

    /**
     * Parses JSON from the string input into a list of the requested element type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the list element type
     * @param text the JSON text
     * @param type the type of every list element
     * @param features reader features to enable in addition to the defaults
     * @return {@link List} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> List<T> parseArray(String text, Class<T> type, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(text, context)) {
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return list;
        }
    }

    /**
     * Parses JSON from the character input into a list of the requested element type.
     * Returns null for null input or an empty input.
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the list element type
     * @param chars the JSON text as UTF-16 code units
     * @param type the type of every list element
     * @param features reader features to enable in addition to the defaults
     * @return {@link List} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> List<T> parseArray(char[] chars, Class<T> type, JSONReader.Feature... features) {
        if (chars == null || chars.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(chars, context)) {
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return list;
        }
    }

    /**
     * Parses a JSON array into a list using one target type per array position.
     * The type array describes positions, not type arguments of a single element type.
     * Returns null for a null or empty input string.
     *
     * <details><summary>中文</summary>
     * 按数组位置逐项使用指定类型解析为列表；类型数组描述各位置的类型，并非单个元素类型的泛型参数。空输入返回 null。
     * </details>
     *
     * @param <T> the list element type
     * @param text the JSON text
     * @param types the target type for each array position, in order
     * @param features reader features to enable in addition to the defaults
     * @return {@link List} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    static <T> List<T> parseArray(String text, Type[] types, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(text, context)) {
            if (reader.nextIfNull()) {
                return null;
            }

            reader.startArray();
            List<T> array = new ArrayList<>(types.length);
            for (int i = 0; i < types.length; i++) {
                array.add(
                        reader.read(types[i])
                );
            }
            reader.endArray();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return array;
        }
    }

    /**
     * Parses JSON from the input into a list of the requested element type.
     * Returns null for null input.
     *
     * <p>The JSON reader created by this method closes the supplied input after parsing.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the list element type
     * @param input the input to parse, or null
     * @param type the type of every list element
     * @param features reader features to enable in addition to the defaults
     * @return {@link List} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> List<T> parseArray(Reader input, Type type, JSONReader.Feature... features) {
        if (input == null) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(input, context)) {
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return list;
        }
    }

    /**
     * Parses JSON from the encoded byte input into a list of the requested element type.
     * Returns null for null input or an empty input.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the list element type
     * @param bytes the UTF-8 JSON text
     * @param type the type of every list element
     * @param features reader features to enable in addition to the defaults
     * @return {@link List} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> List<T> parseArray(byte[] bytes, Type type, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(bytes, context)) {
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return list;
        }
    }

    /**
     * Parses JSON from the encoded byte input into a list of the requested element type.
     * Returns null for null input or an empty input.
     *
     * <p>The bytes must contain UTF-8 JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the list element type
     * @param bytes the UTF-8 JSON text
     * @param type the type of every list element
     * @param features reader features to enable in addition to the defaults
     * @return {@link List} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> List<T> parseArray(byte[] bytes, Class<T> type, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(bytes, context)) {
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return list;
        }
    }

    /**
     * Parses JSON from the encoded byte input slice into a list of the requested element type.
     * Returns null for null input or an empty input, or when length is zero.
     *
     * <p>The selected bytes are decoded with the supplied charset; they must contain JSON text, not JSONB.</p>
     *
     * <details><summary>中文</summary>
     * 按指定目标类型和读取配置解析 JSON；输入形式、字符集及空值行为见本方法说明。
     * </details>
     *
     * @param <T> the list element type
     * @param bytes the encoded JSON text
     * @param offset the zero-based offset in bytes
     * @param length the number of bytes to read, not the end index
     * @param charset the input charset: UTF-8, UTF-16, US-ASCII or ISO-8859-1; not null
     * @param type the type of every list element
     * @param features reader features to enable in addition to the defaults
     * @return {@link List} or {@code null}
     * @throws JSONException if a parsing error occurs
     */
    @SuppressWarnings("unchecked")
    static <T> List<T> parseArray(
            byte[] bytes,
            int offset,
            int length,
            Charset charset,
            Class<T> type,
            JSONReader.Feature... features
    ) {
        if (bytes == null || bytes.length == 0 || length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(bytes, offset, length, charset, context)) {
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return list;
        }
    }

    /**
     * Serializes a Java value to a JSON string.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 字符串；null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @return the JSON text, never null
     * @throws JSONException if a serialization error occurs
     */
    static String toJSONString(Object object) {
        final ObjectWriterProvider provider = defaultObjectWriterProvider;
        final JSONWriter.Context context = new JSONWriter.Context(provider);
        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                if (valueClass == JSONObject.class && context.features == 0) {
                    writer.write((JSONObject) object);
                } else {
                    ObjectWriter<?> objectWriter = provider.getObjectWriter(
                            valueClass,
                            valueClass,
                            (defaultWriterFeatures & JSONWriter.Feature.FieldBased.mask) != 0
                    );
                    objectWriter.write(writer, object, null, null, 0);
                }
            }
            return writer.toString();
        } catch (NullPointerException | NumberFormatException e) {
            throw new JSONException("JSON#toJSONString cannot serialize '" + object + "'", e);
        }
    }

    /**
     * Serializes a Java value to a JSON string.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 字符串；null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @param context the writer provider and serialization settings, not null
     * @return the JSON text, never null
     * @throws JSONException if a serialization error occurs
     */
    static String toJSONString(Object object, JSONWriter.Context context) {
        if (context == null) {
            context = JSONFactory.createWriteContext();
        }

        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        } catch (NullPointerException | NumberFormatException e) {
            throw new JSONException("JSON#toJSONString cannot serialize '" + object + "'", e);
        }
    }

    /**
     * Serializes a Java value to a JSON string.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 字符串；null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @param features writer features to enable in addition to the defaults
     * @return the JSON text, never null
     * @throws JSONException if a serialization error occurs
     */
    static String toJSONString(Object object, JSONWriter.Feature... features) {
        JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);
        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;
                Class<?> valueClass = object.getClass();

                boolean fieldBased = (context.features & JSONWriter.Feature.FieldBased.mask) != 0;
                ObjectWriter<?> objectWriter = context.provider.getObjectWriter(valueClass, valueClass, fieldBased);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        }
    }

    /**
     * Serializes a Java value to a JSON string.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 字符串；null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @param filter the filter to apply while writing
     * @param features writer features to enable in addition to the defaults
     * @return the JSON text, never null
     * @throws JSONException if a serialization error occurs
     */
    static String toJSONString(Object object, Filter filter, JSONWriter.Feature... features) {
        JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);
        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;
                if (filter != null) {
                    writer.context.configFilter(filter);
                }

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        }
    }

    /**
     * Serializes a Java value to a JSON string.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 字符串；null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @param filters filters to apply while writing
     * @param features writer features to enable in addition to the defaults
     * @return the JSON text, never null
     * @throws JSONException if a serialization error occurs
     */
    static String toJSONString(Object object, Filter[] filters, JSONWriter.Feature... features) {
        final JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);
        if (filters != null && filters.length != 0) {
            context.configFilter(filters);
        }

        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        }
    }

    /**
     * Serializes a Java value to a JSON string.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 字符串；null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @param format the date/time pattern or supported format name, such as {@code millis}, {@code unixtime} or {@code iso8601}
     * @param features writer features to enable in addition to the defaults
     * @return the JSON text, never null
     * @throws JSONException if a serialization error occurs
     */
    static String toJSONString(Object object, String format, JSONWriter.Feature... features) {
        final JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);
        if (format != null && !format.isEmpty()) {
            context.setDateFormat(format);
        }

        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;
                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        }
    }

    /**
     * Serializes a Java value to a JSON string.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 字符串；null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @param format the date/time pattern or supported format name, such as {@code millis}, {@code unixtime} or {@code iso8601}
     * @param filters filters to apply while writing
     * @param features writer features to enable in addition to the defaults
     * @return the JSON text, never null
     * @throws JSONException if a serialization error occurs
     */
    static String toJSONString(Object object, String format, Filter[] filters, JSONWriter.Feature... features) {
        final JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);
        if (format != null && !format.isEmpty()) {
            context.setDateFormat(format);
        }
        if (filters != null && filters.length != 0) {
            context.configFilter(filters);
        }

        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;
                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        }
    }

    /**
     * Serializes a Java value to UTF-8 JSON bytes.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 文本字节；未指定字符集时使用 UTF-8，null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @return the encoded JSON text, never null
     * @throws JSONException if a serialization error occurs
     */
    static byte[] toJSONBytes(Object object) {
        final ObjectWriterProvider provider = defaultObjectWriterProvider;
        final JSONWriter.Context context = new JSONWriter.Context(provider);
        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                if (valueClass == JSONObject.class && writer.context.features == 0) {
                    writer.write((JSONObject) object);
                } else {
                    ObjectWriter<?> objectWriter = provider.getObjectWriter(
                            valueClass,
                            valueClass,
                            (defaultWriterFeatures & JSONWriter.Feature.FieldBased.mask) != 0
                    );
                    objectWriter.write(writer, object, null, null, 0);
                }
            }
            return writer.getBytes();
        }
    }

    /**
     * Serializes a Java value to JSON bytes in the requested charset.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 文本字节；未指定字符集时使用 UTF-8，null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @param charset the output charset, not null
     * @param features writer features to enable in addition to the defaults
     * @return the encoded JSON text, never null
     * @throws JSONException if a serialization error occurs
     * @since 2.0.47
     */
    static byte[] toJSONBytes(Object object, Charset charset, JSONWriter.Feature... features) {
        final ObjectWriterProvider provider = defaultObjectWriterProvider;
        final JSONWriter.Context context = new JSONWriter.Context(provider, features);
        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                if (valueClass == JSONObject.class && writer.context.features == 0) {
                    writer.write((JSONObject) object);
                } else {
                    ObjectWriter<?> objectWriter = provider.getObjectWriter(
                            valueClass,
                            valueClass,
                            (context.features & JSONWriter.Feature.FieldBased.mask) != 0
                    );
                    objectWriter.write(writer, object, null, null, 0);
                }
            }
            return writer.getBytes(charset);
        }
    }

    /**
     * Serializes a Java value to JSON bytes in the requested charset.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 文本字节；未指定字符集时使用 UTF-8，null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @param charset the output charset, not null
     * @param context the writer provider and serialization settings, not null
     * @return the encoded JSON text, never null
     * @throws JSONException if a serialization error occurs
     * @since 2.0.47
     */
    static byte[] toJSONBytes(Object object, Charset charset, JSONWriter.Context context) {
        final ObjectWriterProvider provider = context.provider;
        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                if (valueClass == JSONObject.class && writer.context.features == 0) {
                    writer.write((JSONObject) object);
                } else {
                    ObjectWriter<?> objectWriter = provider.getObjectWriter(
                            valueClass,
                            valueClass,
                            (context.features & JSONWriter.Feature.FieldBased.mask) != 0
                    );
                    objectWriter.write(writer, object, null, null, 0);
                }
            }
            return writer.getBytes(charset);
        }
    }

    /**
     * Serializes a Java value to UTF-8 JSON bytes.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 文本字节；未指定字符集时使用 UTF-8，null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @param format the date/time pattern or supported format name, such as {@code millis}, {@code unixtime} or {@code iso8601}
     * @param features writer features to enable in addition to the defaults
     * @return the encoded JSON text, never null
     * @throws JSONException if a serialization error occurs
     */
    static byte[] toJSONBytes(Object object, String format, JSONWriter.Feature... features) {
        final JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);
        if (format != null && !format.isEmpty()) {
            context.setDateFormat(format);
        }

        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    /**
     * Serializes a Java value to UTF-8 JSON bytes.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 文本字节；未指定字符集时使用 UTF-8，null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @param filters filters to apply while writing
     * @return the encoded JSON text, never null
     * @throws JSONException if a serialization error occurs
     */
    static byte[] toJSONBytes(Object object, Filter... filters) {
        final JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider);
        if (filters != null && filters.length != 0) {
            context.configFilter(filters);
        }

        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    /**
     * Serializes a Java value to UTF-8 JSON bytes.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 文本字节；未指定字符集时使用 UTF-8，null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @param features writer features to enable in addition to the defaults
     * @return the encoded JSON text, never null
     * @throws JSONException if a serialization error occurs
     */
    static byte[] toJSONBytes(Object object, JSONWriter.Feature... features) {
        final JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);
        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    /**
     * Serializes a Java value to UTF-8 JSON bytes.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 文本字节；未指定字符集时使用 UTF-8，null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @param filters filters to apply while writing
     * @param features writer features to enable in addition to the defaults
     * @return the encoded JSON text, never null
     * @throws JSONException if a serialization error occurs
     */
    static byte[] toJSONBytes(Object object, Filter[] filters, JSONWriter.Feature... features) {
        final JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);
        if (filters != null && filters.length != 0) {
            context.configFilter(filters);
        }

        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    /**
     * Serializes a Java value to UTF-8 JSON bytes.
     * A null value is serialized as the JSON literal {@code null}.
     *
     * <details><summary>中文</summary>
     * 将 Java 值序列化为 JSON 文本字节；未指定字符集时使用 UTF-8，null 值输出为 JSON null 文本。
     * </details>
     *
     * @param object the Java value to serialize, possibly null
     * @param format the date/time pattern or supported format name, such as {@code millis}, {@code unixtime} or {@code iso8601}
     * @param filters filters to apply while writing
     * @param features writer features to enable in addition to the defaults
     * @return the encoded JSON text, never null
     * @throws JSONException if a serialization error occurs
     */
    static byte[] toJSONBytes(Object object, String format, Filter[] filters, JSONWriter.Feature... features) {
        final JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);
        if (format != null && !format.isEmpty()) {
            context.setDateFormat(format);
        }
        if (filters != null && filters.length != 0) {
            context.configFilter(filters);
        }

        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    /**
     * Serializes a value as UTF-8 JSON and writes it to the output stream.
     * The stream is neither flushed nor closed by this method. A null value is written as {@code null}.
     *
     * <details><summary>中文</summary>
     * 将值序列化为 UTF-8 JSON 并写入输出流；不刷新或关闭输出流。null 值写为 JSON null。
     * </details>
     *
     * @param out the destination stream, not null
     * @param object the Java value to serialize, possibly null
     * @return the number of bytes written by this call
     * @throws JSONException if an I/O error or serialization error occurs
     */
    static int writeTo(OutputStream out, Object object) {
        final JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider);

        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.flushTo(out);
        } catch (Exception e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    /**
     * Serializes a value as UTF-8 JSON and writes it to the output stream.
     * The stream is neither flushed nor closed by this method. A null value is written as {@code null}.
     *
     * <details><summary>中文</summary>
     * 将值序列化为 UTF-8 JSON 并写入输出流；不刷新或关闭输出流。null 值写为 JSON null。
     * </details>
     *
     * @param out the destination stream, not null
     * @param object the Java value to serialize, possibly null
     * @param context the writer provider and serialization settings, not null
     * @return the number of bytes written by this call
     * @throws JSONException if an I/O error or serialization error occurs
     * @since 2.0.51
     */
    static int writeTo(OutputStream out, Object object, JSONWriter.Context context) {
        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.flushTo(out);
        } catch (Exception e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    /**
     * Serializes a value as UTF-8 JSON and writes it to the output stream.
     * The stream is neither flushed nor closed by this method. A null value is written as {@code null}.
     *
     * <details><summary>中文</summary>
     * 将值序列化为 UTF-8 JSON 并写入输出流；不刷新或关闭输出流。null 值写为 JSON null。
     * </details>
     *
     * @param out the destination stream, not null
     * @param object the Java value to serialize, possibly null
     * @param features writer features to enable in addition to the defaults
     * @return the number of bytes written by this call
     * @throws JSONException if an I/O error or serialization error occurs
     * @since 2.0.2
     */
    static int writeTo(OutputStream out, Object object, JSONWriter.Feature... features) {
        final JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);
        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.flushTo(out);
        } catch (Exception e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    /**
     * Serializes a value as UTF-8 JSON and writes it to the output stream.
     * The stream is neither flushed nor closed by this method. A null value is written as {@code null}.
     *
     * <details><summary>中文</summary>
     * 将值序列化为 UTF-8 JSON 并写入输出流；不刷新或关闭输出流。null 值写为 JSON null。
     * </details>
     *
     * @param out the destination stream, not null
     * @param object the Java value to serialize, possibly null
     * @param filters filters to apply while writing
     * @param features writer features to enable in addition to the defaults
     * @return the number of bytes written by this call
     * @throws JSONException if an I/O error or serialization error occurs
     * @since 2.0.2
     */
    static int writeTo(OutputStream out, Object object, Filter[] filters, JSONWriter.Feature... features) {
        final JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);
        if (filters != null && filters.length != 0) {
            context.configFilter(filters);
        }

        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.flushTo(out);
        } catch (Exception e) {
            throw new JSONException("JSON#writeTo cannot serialize '" + object + "' to 'OutputStream'", e);
        }
    }

    /**
     * Serializes a value as UTF-8 JSON and writes it to the output stream.
     * The stream is neither flushed nor closed by this method. A null value is written as {@code null}.
     *
     * <details><summary>中文</summary>
     * 将值序列化为 UTF-8 JSON 并写入输出流；不刷新或关闭输出流。null 值写为 JSON null。
     * </details>
     *
     * @param out the destination stream, not null
     * @param object the Java value to serialize, possibly null
     * @param format the date/time pattern or supported format name, such as {@code millis}, {@code unixtime} or {@code iso8601}
     * @param filters filters to apply while writing
     * @param features writer features to enable in addition to the defaults
     * @return the number of bytes written by this call
     * @throws JSONException if an I/O error or serialization error occurs
     * @since 2.0.2
     */
    static int writeTo(
            OutputStream out,
            Object object,
            String format,
            Filter[] filters,
            JSONWriter.Feature... features
    ) {
        final JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);
        if (format != null && !format.isEmpty()) {
            context.setDateFormat(format);
        }
        if (filters != null && filters.length != 0) {
            context.configFilter(filters);
        }

        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.flushTo(out);
        } catch (Exception e) {
            throw new JSONException("JSON#writeTo cannot serialize '" + object + "' to 'OutputStream'", e);
        }
    }

    /**
     * Tests whether the string contains exactly one JSON value.
     * Uses fastjson2's accepted syntax rather than enforcing only strict RFC JSON.
     * Null or empty input, and trailing content return false.
     *
     * <details><summary>中文</summary>
     * 检查输入是否包含一个完整的指定类型 JSON 值；采用 fastjson2 支持的语法，空输入、根类型不符或尾随内容返回 false。
     * </details>
     *
     * @param text the JSON text
     * @return true if one complete JSON value is accepted, otherwise false
     * @since 2.0.2
     */
    static boolean isValid(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(text)) {
            jsonReader.skipValue();
            return jsonReader.isEnd() && !jsonReader.comma;
        } catch (JSONException | ArrayIndexOutOfBoundsException error) {
            return false;
        }
    }

    /**
     * Tests whether the string contains exactly one JSON value.
     * Uses fastjson2's accepted syntax rather than enforcing only strict RFC JSON.
     * Null or empty input, and trailing content return false.
     *
     * <details><summary>中文</summary>
     * 检查输入是否包含一个完整的指定类型 JSON 值；采用 fastjson2 支持的语法，空输入、根类型不符或尾随内容返回 false。
     * </details>
     *
     * @param text the JSON text
     * @param features reader features to enable in addition to the defaults
     * @return true if one complete JSON value is accepted, otherwise false
     * @since 2.0.2
     */
    static boolean isValid(String text, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(text, JSONFactory.createReadContext(features))) {
            jsonReader.skipValue();
            return jsonReader.isEnd() && !jsonReader.comma;
        } catch (JSONException | ArrayIndexOutOfBoundsException error) {
            return false;
        }
    }

    /**
     * Tests whether the character array contains exactly one JSON value.
     * Uses fastjson2's accepted syntax rather than enforcing only strict RFC JSON.
     * Null or empty input, and trailing content return false.
     *
     * <details><summary>中文</summary>
     * 检查输入是否包含一个完整的指定类型 JSON 值；采用 fastjson2 支持的语法，空输入、根类型不符或尾随内容返回 false。
     * </details>
     *
     * @param chars the JSON text as UTF-16 code units
     * @return true if one complete JSON value is accepted, otherwise false
     * @since 2.0.2
     */
    static boolean isValid(char[] chars) {
        if (chars == null || chars.length == 0) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(chars)) {
            jsonReader.skipValue();
            return jsonReader.isEnd() && !jsonReader.comma;
        } catch (JSONException | ArrayIndexOutOfBoundsException error) {
            return false;
        }
    }

    /**
     * Tests whether the string contains exactly one JSON object.
     * Uses fastjson2's accepted syntax rather than enforcing only strict RFC JSON.
     * Null or empty input, a wrong root kind, and trailing content return false.
     *
     * <details><summary>中文</summary>
     * 检查输入是否包含一个完整的指定类型 JSON 值；采用 fastjson2 支持的语法，空输入、根类型不符或尾随内容返回 false。
     * </details>
     *
     * @param text the JSON text
     * @return true if one complete JSON object is accepted, otherwise false
     * @since 2.0.2
     */
    static boolean isValidObject(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(text)) {
            if (!jsonReader.isObject()) {
                return false;
            }
            jsonReader.skipValue();
            return jsonReader.isEnd() && !jsonReader.comma;
        } catch (JSONException | ArrayIndexOutOfBoundsException error) {
            return false;
        }
    }

    /**
     * Tests whether the byte array contains exactly one JSON object.
     * Uses fastjson2's accepted syntax rather than enforcing only strict RFC JSON.
     * Null or empty input, a wrong root kind, and trailing content return false.
     *
     * <details><summary>中文</summary>
     * 检查输入是否包含一个完整的指定类型 JSON 值；采用 fastjson2 支持的语法，空输入、根类型不符或尾随内容返回 false。
     * </details>
     *
     * @param bytes the UTF-8 JSON text
     * @return true if one complete JSON object is accepted, otherwise false
     * @since 2.0.2
     */
    static boolean isValidObject(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(bytes)) {
            if (!jsonReader.isObject()) {
                return false;
            }
            jsonReader.skipValue();
            return jsonReader.isEnd() && !jsonReader.comma;
        } catch (JSONException | ArrayIndexOutOfBoundsException error) {
            return false;
        }
    }

    /**
     * Tests whether the string contains exactly one JSON array.
     * Uses fastjson2's accepted syntax rather than enforcing only strict RFC JSON.
     * Null or empty input, a wrong root kind, and trailing content return false.
     *
     * <details><summary>中文</summary>
     * 检查输入是否包含一个完整的指定类型 JSON 值；采用 fastjson2 支持的语法，空输入、根类型不符或尾随内容返回 false。
     * </details>
     *
     * @param text the JSON text
     * @return true if one complete JSON array is accepted, otherwise false
     * @since 2.0.2
     */
    static boolean isValidArray(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(text)) {
            if (!jsonReader.isArray()) {
                return false;
            }
            jsonReader.skipValue();
            return jsonReader.isEnd() && !jsonReader.comma;
        } catch (JSONException | ArrayIndexOutOfBoundsException error) {
            return false;
        }
    }

    /**
     * Tests whether the byte array contains exactly one JSON value.
     * Uses fastjson2's accepted syntax rather than enforcing only strict RFC JSON.
     * Null or empty input, and trailing content return false.
     *
     * <details><summary>中文</summary>
     * 检查输入是否包含一个完整的指定类型 JSON 值；采用 fastjson2 支持的语法，空输入、根类型不符或尾随内容返回 false。
     * </details>
     *
     * @param bytes the UTF-8 JSON text
     * @return true if one complete JSON value is accepted, otherwise false
     * @since 2.0.2
     */
    static boolean isValid(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(bytes)) {
            jsonReader.skipValue();
            return jsonReader.isEnd() && !jsonReader.comma;
        } catch (JSONException | ArrayIndexOutOfBoundsException error) {
            return false;
        }
    }

    /**
     * Tests whether the byte array contains exactly one JSON value.
     * Uses fastjson2's accepted syntax rather than enforcing only strict RFC JSON.
     * Null or empty input, and trailing content return false.
     *
     * <details><summary>中文</summary>
     * 检查输入是否包含一个完整的指定类型 JSON 值；采用 fastjson2 支持的语法，空输入、根类型不符或尾随内容返回 false。
     * </details>
     *
     * @param bytes the encoded JSON text
     * @param charset the input charset: UTF-8, UTF-16, US-ASCII or ISO-8859-1; not null
     * @return true if one complete JSON value is accepted, otherwise false
     * @since 2.0.2
     */
    static boolean isValid(byte[] bytes, Charset charset) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }

        return isValid(bytes, 0, bytes.length, charset);
    }

    /**
     * Tests whether the byte array contains exactly one JSON array.
     * Uses fastjson2's accepted syntax rather than enforcing only strict RFC JSON.
     * Null or empty input, a wrong root kind, and trailing content return false.
     *
     * <details><summary>中文</summary>
     * 检查输入是否包含一个完整的指定类型 JSON 值；采用 fastjson2 支持的语法，空输入、根类型不符或尾随内容返回 false。
     * </details>
     *
     * @param bytes the UTF-8 JSON text
     * @return true if one complete JSON array is accepted, otherwise false
     */
    static boolean isValidArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(bytes)) {
            if (!jsonReader.isArray()) {
                return false;
            }
            jsonReader.skipValue();
            return jsonReader.isEnd() && !jsonReader.comma;
        } catch (JSONException | ArrayIndexOutOfBoundsException error) {
            return false;
        }
    }

    /**
     * Tests whether the byte array slice contains exactly one JSON value.
     * Uses fastjson2's accepted syntax rather than enforcing only strict RFC JSON.
     * Null or empty input, and trailing content return false.
     *
     * <details><summary>中文</summary>
     * 检查输入是否包含一个完整的指定类型 JSON 值；采用 fastjson2 支持的语法，空输入、根类型不符或尾随内容返回 false。
     * </details>
     *
     * @param bytes the encoded JSON text
     * @param offset the zero-based offset in bytes
     * @param length the number of bytes to read, not the end index
     * @param charset the input charset: UTF-8, UTF-16, US-ASCII or ISO-8859-1; not null
     * @return true if one complete JSON value is accepted, otherwise false
     */
    static boolean isValid(byte[] bytes, int offset, int length, Charset charset) {
        if (bytes == null || bytes.length == 0 || length == 0) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(bytes, offset, length, charset)) {
            jsonReader.skipValue();
            return jsonReader.isEnd() && !jsonReader.comma;
        } catch (JSONException | ArrayIndexOutOfBoundsException error) {
            return false;
        }
    }

    /**
     * Converts a Java value to its JSON representation.
     * Existing {@link JSONObject} and {@link JSONArray} instances are returned unchanged.
     * Other results may be containers, scalar values or null; this is not a deep-copy operation.
     *
     * <details><summary>中文</summary>
     * 将 Java 值转换为 JSON 表示，结果可为容器、标量或 null。已有 JSONObject 和 JSONArray 原样返回；此方法不保证深复制。
     * </details>
     *
     * @param object the specified object to be converted
     * @return the JSON representation, or null for a null input
     */
    static Object toJSON(Object object) {
        return toJSON(object, (JSONWriter.Feature[]) null);
    }

    /**
     * Converts a Java value to its JSON representation.
     * Existing {@link JSONObject} and {@link JSONArray} instances are returned unchanged.
     * Other results may be containers, scalar values or null; this is not a deep-copy operation.
     *
     * <details><summary>中文</summary>
     * 将 Java 值转换为 JSON 表示，结果可为容器、标量或 null。已有 JSONObject 和 JSONArray 原样返回；此方法不保证深复制。
     * </details>
     *
     * @param object the specified object to be converted
     * @param features writer features to enable in addition to the defaults
     * @return the JSON representation, or null for a null input
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static Object toJSON(Object object, JSONWriter.Feature... features) {
        if (object == null) {
            return null;
        }
        if (object instanceof JSONObject || object instanceof JSONArray) {
            return object;
        }

        JSONWriter.Context writeContext = features == null ?
                JSONFactory.createWriteContext() : JSONFactory.createWriteContext(features);
        Class<?> valueClass = object.getClass();
        ObjectWriter<?> objectWriter = writeContext.getObjectWriter(valueClass, valueClass);
        if (objectWriter instanceof ObjectWriterAdapter
                && !writeContext.isEnabled(JSONWriter.Feature.ReferenceDetection)
                && (objectWriter.getFeatures() & JSONWriter.Feature.WriteClassName.mask) == 0) {
            ObjectWriterAdapter objectWriterAdapter = (ObjectWriterAdapter) objectWriter;
            return objectWriterAdapter.toJSONObject(object, writeContext.features);
        }

        String str;
        try (JSONWriter writer = JSONWriter.of(writeContext)) {
            objectWriter.write(writer, object, null, null, writeContext.features);
            str = writer.toString();
        } catch (NullPointerException | NumberFormatException ex) {
            throw new JSONException("toJSONString error", ex);
        }

        return parse(str);
    }

    /**
     * Converts the specified object to an object of the specified goal type
     *
     * <details><summary>中文</summary>
     * 将值转换为目标类型；输入为 null 时返回 null。
     * </details>
     *
     * @param <T> the target type
     * @param clazz the target class
     * @param object the specified object to be converted
     * @return the converted object of type T, or null if the input object is null
     * @since 2.0.4
     */
    static <T> T to(Class<T> clazz, Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof JSONObject) {
            return ((JSONObject) object).to(clazz);
        }

        return TypeUtils.cast(object, clazz, JSONFactory.getDefaultObjectReaderProvider());
    }

    /**
     * Converts the specified object to an object of the specified goal type
     *
     * <details><summary>中文</summary>
     * 将值转换为目标类型；输入为 null 时返回 null。
     * </details>
     *
     * @param <T> the target type
     * @param object the specified object to be converted
     * @param clazz the target class
     * @return the converted object of type T, or null if the input object is null
     * @deprecated since 2.0.4, please use {@link #to(Class, Object)}
     */
    static <T> T toJavaObject(Object object, Class<T> clazz) {
        return to(clazz, object);
    }

    /**
     * Associates mixin annotations with a target class in the default reader and writer providers.
     * This changes serialization/deserialization metadata; it does not copy Java fields or methods.
     * Passing null as the mixin source removes the association.
     *
     * <details><summary>中文</summary>
     * 在默认读写器提供者中关联目标类的混入注解，仅影响序列化元数据，不复制 Java 成员。混入来源为 null 时移除关联。
     * </details>
     *
     * @param target the target class to mix into
     * @param mixinSource the source class to mix from
     * @since 2.0.2
     */
    static void mixIn(Class<?> target, Class<?> mixinSource) {
        JSONFactory.defaultObjectWriterProvider.mixIn(target, mixinSource);
        JSONFactory.getDefaultObjectReaderProvider().mixIn(target, mixinSource);
    }

    /**
     * Registers an {@link ObjectReader} for {@link Type} in default {@link com.alibaba.fastjson2.reader.ObjectReaderProvider}
     *
     * <p>Uses the method-based cache. Use the boolean overload to register a field-based codec.</p>
     *
     * <details><summary>中文</summary>
     * 在默认提供者中注册编解码配置，返回值表示先前缓存项或注册结果。
     * 使用基于方法的缓存；需要字段缓存时使用带 boolean 的重载。
     * </details>
     *
     * @param type the type to register an ObjectReader for
     * @param objectReader the reader to register, or null to remove the cached reader
     * @return the previously registered ObjectReader, or null if there was none
     * @see JSONFactory#getDefaultObjectReaderProvider()
     * @see com.alibaba.fastjson2.reader.ObjectReaderProvider#register(Type, ObjectReader)
     * @since 2.0.2
     */
    static ObjectReader<?> register(Type type, ObjectReader<?> objectReader) {
        return JSONFactory.getDefaultObjectReaderProvider().register(type, objectReader);
    }

    /**
     * Registers an {@link ObjectReader} for {@link Type} in default {@link com.alibaba.fastjson2.reader.ObjectReaderProvider}
     *
     * <details><summary>中文</summary>
     * 在默认提供者中注册编解码配置，返回值表示先前缓存项或注册结果。
     * </details>
     *
     * @param type the type to register an ObjectReader for
     * @param objectReader the reader to register, or null to remove the cached reader
     * @param fieldBased whether to use the field-based cache rather than the method-based cache
     * @return the previously registered ObjectReader, or null if there was none
     * @see JSONFactory#getDefaultObjectReaderProvider()
     * @see com.alibaba.fastjson2.reader.ObjectReaderProvider#register(Type, ObjectReader, boolean)
     * @since 2.0.38
     */
    static ObjectReader<?> register(Type type, ObjectReader<?> objectReader, boolean fieldBased) {
        return JSONFactory.getDefaultObjectReaderProvider().register(type, objectReader, fieldBased);
    }

    /**
     * Registers, if absent, an {@link ObjectReader} for {@link Type} in default {@link com.alibaba.fastjson2.reader.ObjectReaderProvider}
     *
     * <p>Uses the method-based cache. Use the boolean overload to register a field-based codec.</p>
     *
     * <details><summary>中文</summary>
     * 在默认提供者中注册编解码配置，返回值表示先前缓存项或注册结果。
     * 使用基于方法的缓存；需要字段缓存时使用带 boolean 的重载。
     * </details>
     *
     * @param type the type to register an ObjectReader for
     * @param objectReader the ObjectReader to register
     * @return the previously registered ObjectReader, or null if there was none
     * @see JSONFactory#getDefaultObjectReaderProvider()
     * @see com.alibaba.fastjson2.reader.ObjectReaderProvider#registerIfAbsent(Type, ObjectReader)
     * @since 2.0.6
     */
    static ObjectReader<?> registerIfAbsent(Type type, ObjectReader<?> objectReader) {
        return JSONFactory.getDefaultObjectReaderProvider().registerIfAbsent(type, objectReader);
    }

    /**
     * Registers, if absent, an {@link ObjectReader} for {@link Type} in default {@link com.alibaba.fastjson2.reader.ObjectReaderProvider}
     *
     * <details><summary>中文</summary>
     * 在默认提供者中注册编解码配置，返回值表示先前缓存项或注册结果。
     * </details>
     *
     * @param type the type to register an ObjectReader for
     * @param objectReader the ObjectReader to register
     * @param fieldBased whether to use the field-based cache rather than the method-based cache
     * @return the previously registered ObjectReader, or null if there was none
     * @see JSONFactory#getDefaultObjectReaderProvider()
     * @see com.alibaba.fastjson2.reader.ObjectReaderProvider#registerIfAbsent(Type, ObjectReader, boolean)
     * @since 2.0.38
     */
    static ObjectReader<?> registerIfAbsent(Type type, ObjectReader<?> objectReader, boolean fieldBased) {
        return JSONFactory.getDefaultObjectReaderProvider().registerIfAbsent(type, objectReader, fieldBased);
    }

    /**
     * Registers an {@link ObjectReaderModule} in default {@link com.alibaba.fastjson2.reader.ObjectReaderProvider}
     *
     * <details><summary>中文</summary>
     * 在默认提供者中注册编解码配置，返回值表示先前缓存项或注册结果。
     * </details>
     *
     * @param objectReaderModule the ObjectReaderModule to register
     * @return true if registered, or false if the same module instance was already registered
     * @see JSONFactory#getDefaultObjectReaderProvider()
     * @see com.alibaba.fastjson2.reader.ObjectReaderProvider#register(ObjectReaderModule)
     * @since 2.0.2
     */
    static boolean register(ObjectReaderModule objectReaderModule) {
        ObjectReaderProvider provider = getDefaultObjectReaderProvider();
        return provider.register(objectReaderModule);
    }

    /**
     * Adds a subtype to its superclass reader when that reader supports see-also subtypes.
     *
     * <details><summary>中文</summary>
     * 当父类读入器支持 see-also 子类型时，为其添加子类型；其他读入器不受影响。
     * </details>
     *
     * @param subTypeClass the sub type class to register
     * @throws JSONException if the subtype has no superclass
     * @since 2.0.2
     */
    static void registerSeeAlsoSubType(Class subTypeClass) {
        registerSeeAlsoSubType(subTypeClass, null);
    }

    /**
     * Adds a subtype to its superclass reader when that reader supports see-also subtypes.
     *
     * <details><summary>中文</summary>
     * 当父类读入器支持 see-also 子类型时，为其添加子类型；其他读入器不受影响。
     * </details>
     *
     * @param subTypeClass the sub type class to register
     * @param subTypeClassName the class name for the sub type
     * @throws JSONException if the subtype has no superclass
     * @since 2.0.2
     */
    static void registerSeeAlsoSubType(Class subTypeClass, String subTypeClassName) {
        ObjectReaderProvider provider = getDefaultObjectReaderProvider();
        provider.registerSeeAlsoSubType(subTypeClass, subTypeClassName);
    }

    /**
     * Registers an {@link ObjectWriterModule} in default {@link  com.alibaba.fastjson2.writer.ObjectWriterProvider}
     *
     * <details><summary>中文</summary>
     * 在默认提供者中注册编解码配置，返回值表示先前缓存项或注册结果。
     * </details>
     *
     * @param objectWriterModule the ObjectWriterModule to register
     * @return true if registered, or false if the same module instance was already registered
     * @see JSONFactory#getDefaultObjectWriterProvider()
     * @see com.alibaba.fastjson2.writer.ObjectWriterProvider#register(ObjectWriterModule)
     * @since 2.0.2
     */
    static boolean register(ObjectWriterModule objectWriterModule) {
        return JSONFactory.getDefaultObjectWriterProvider().register(objectWriterModule);
    }

    /**
     * Registers an {@link ObjectWriter} for {@link Type} in default {@link  com.alibaba.fastjson2.writer.ObjectWriterProvider}
     *
     * <p>Selects the field-based or method-based cache using the current global
     * {@link JSONWriter.Feature#FieldBased} default. Use the boolean overload to select it explicitly.</p>
     *
     * <details><summary>中文</summary>
     * 在默认提供者中注册编解码配置，返回值表示先前缓存项或注册结果。
     * 按当前全局 FieldBased 特性选择字段或方法缓存；带 boolean 的重载可显式选择。
     * </details>
     *
     * @param type the type to register an ObjectWriter for
     * @param objectWriter the writer to register, or null to remove the cached writer
     * @return the previously registered ObjectWriter, or null if there was none
     * @see JSONFactory#getDefaultObjectWriterProvider()
     * @see com.alibaba.fastjson2.writer.ObjectWriterProvider#register(Type, ObjectWriter)
     * @since 2.0.2
     */
    static ObjectWriter<?> register(Type type, ObjectWriter<?> objectWriter) {
        return JSONFactory.getDefaultObjectWriterProvider().register(type, objectWriter);
    }

    /**
     * Registers an {@link ObjectWriter} for {@link Type} in default {@link  com.alibaba.fastjson2.writer.ObjectWriterProvider}
     *
     * <details><summary>中文</summary>
     * 在默认提供者中注册编解码配置，返回值表示先前缓存项或注册结果。
     * </details>
     *
     * @param type the type to register an ObjectWriter for
     * @param objectWriter the writer to register, or null to remove the cached writer
     * @param fieldBased whether to use the field-based cache rather than the method-based cache
     * @return the previously registered ObjectWriter, or null if there was none
     * @see JSONFactory#getDefaultObjectWriterProvider()
     * @see com.alibaba.fastjson2.writer.ObjectWriterProvider#register(Type, ObjectWriter, boolean)
     * @since 2.0.38
     */
    static ObjectWriter<?> register(Type type, ObjectWriter<?> objectWriter, boolean fieldBased) {
        return JSONFactory.getDefaultObjectWriterProvider().register(type, objectWriter, fieldBased);
    }

    /**
     * Registers, if absent, an {@link ObjectWriter} for {@link Type} in default {@link  com.alibaba.fastjson2.writer.ObjectWriterProvider}
     *
     * <p>Uses the method-based cache. Use the boolean overload to register a field-based codec.</p>
     *
     * <details><summary>中文</summary>
     * 在默认提供者中注册编解码配置，返回值表示先前缓存项或注册结果。
     * 使用基于方法的缓存；需要字段缓存时使用带 boolean 的重载。
     * </details>
     *
     * @param type the type to register an ObjectWriter for
     * @param objectWriter the ObjectWriter to register
     * @return the previously registered ObjectWriter, or null if there was none
     * @see JSONFactory#getDefaultObjectWriterProvider()
     * @see com.alibaba.fastjson2.writer.ObjectWriterProvider#registerIfAbsent(Type, ObjectWriter)
     * @since 2.0.6
     */
    static ObjectWriter<?> registerIfAbsent(Type type, ObjectWriter<?> objectWriter) {
        return JSONFactory.getDefaultObjectWriterProvider().registerIfAbsent(type, objectWriter);
    }

    /**
     * Registers, if absent, an {@link ObjectWriter} for {@link Type} in default {@link  com.alibaba.fastjson2.writer.ObjectWriterProvider}
     *
     * <details><summary>中文</summary>
     * 在默认提供者中注册编解码配置，返回值表示先前缓存项或注册结果。
     * </details>
     *
     * @param type the type to register an ObjectWriter for
     * @param objectWriter the ObjectWriter to register
     * @param fieldBased whether to use the field-based cache rather than the method-based cache
     * @return the previously registered ObjectWriter, or null if there was none
     * @see JSONFactory#getDefaultObjectWriterProvider()
     * @see com.alibaba.fastjson2.writer.ObjectWriterProvider#registerIfAbsent(Type, ObjectWriter, boolean)
     * @since 2.0.6
     */
    static ObjectWriter<?> registerIfAbsent(Type type, ObjectWriter<?> objectWriter, boolean fieldBased) {
        return JSONFactory.getDefaultObjectWriterProvider().registerIfAbsent(type, objectWriter, fieldBased);
    }

    /**
     * Installs a supported serialization filter on the default writer for a class.
     * Filters that do not implement a supported writer-filter interface are ignored.
     *
     * <details><summary>中文</summary>
     * 为类的默认写入器设置受支持的序列化过滤器；不支持的过滤器类型不生效。
     * </details>
     *
     * @param type the class type to register filter for
     * @param filter the filter to apply to the specified type
     * @since 2.0.19
     */
    static void register(Class type, Filter filter) {
        boolean writerFilter
                = filter instanceof AfterFilter
                || filter instanceof BeforeFilter
                || filter instanceof ContextNameFilter
                || filter instanceof ContextValueFilter
                || filter instanceof LabelFilter
                || filter instanceof NameFilter
                || filter instanceof PropertyFilter
                || filter instanceof PropertyPreFilter
                || filter instanceof ValueFilter;
        if (writerFilter) {
            ObjectWriter objectWriter
                    = JSONFactory
                    .getDefaultObjectWriterProvider()
                    .getObjectWriter(type);
            objectWriter.setFilter(filter);
        }
    }

    /**
     * Enable the specified features in default reader
     *
     * <p>Changes the global defaults used by subsequently created contexts. Existing contexts retain their settings.</p>
     *
     * <details><summary>中文</summary>
     * 设置或查询默认配置；修改默认值不会追溯更新已有上下文。
     * </details>
     *
     * @param features the specified features to be used
     * @throws JSONException if enabling {@link JSONReader.Feature#SupportAutoType} globally
     * @since 2.0.6
     */
    static void config(JSONReader.Feature... features) {
        for (int i = 0; i < features.length; i++) {
            JSONReader.Feature feature = features[i];
            if (feature == JSONReader.Feature.SupportAutoType) {
                throw new JSONException("not support config global autotype support");
            }

            JSONFactory.defaultReaderFeatures |= feature.mask;
        }
    }

    /**
     * Enable or disable the specified features in default reader
     *
     * <p>Changes the global defaults used by subsequently created contexts. Existing contexts retain their settings.</p>
     *
     * <details><summary>中文</summary>
     * 设置或查询默认配置；修改默认值不会追溯更新已有上下文。
     * </details>
     *
     * @param feature the specified feature to be used
     * @param state enable this feature if and only if {@code state} is {@code true}, disable otherwise
     * @throws JSONException if enabling {@link JSONReader.Feature#SupportAutoType} globally
     * @since 2.0.6
     */
    static void config(JSONReader.Feature feature, boolean state) {
        if (feature == JSONReader.Feature.SupportAutoType && state) {
            throw new JSONException("not support config global autotype support");
        }

        if (state) {
            JSONFactory.defaultReaderFeatures |= feature.mask;
        } else {
            JSONFactory.defaultReaderFeatures &= ~feature.mask;
        }
    }

    /**
     * Check if the default reader enables the specified feature
     *
     * <details><summary>中文</summary>
     * 设置或查询默认配置；修改默认值不会追溯更新已有上下文。
     * </details>
     *
     * @param feature the specified feature
     * @return true if the feature is enabled in the global defaults
     * @since 2.0.6
     */
    static boolean isEnabled(JSONReader.Feature feature) {
        return (JSONFactory.defaultReaderFeatures & feature.mask) != 0;
    }

    /**
     * Config default reader dateFormat
     *
     * <p>Changes the global defaults used by subsequently created contexts. Existing contexts retain their settings.</p>
     *
     * <details><summary>中文</summary>
     * 设置或查询默认配置；修改默认值不会追溯更新已有上下文。
     * </details>
     *
     * @param dateFormat the date format to use for reading
     * @since 2.0.30
     */
    static void configReaderDateFormat(String dateFormat) {
        defaultReaderFormat = dateFormat;
    }

    /**
     * Config default writer dateFormat
     *
     * <p>Changes the global defaults used by subsequently created contexts. Existing contexts retain their settings.</p>
     *
     * <details><summary>中文</summary>
     * 设置或查询默认配置；修改默认值不会追溯更新已有上下文。
     * </details>
     *
     * @param dateFormat the date format to use for writing
     * @since 2.0.30
     */
    static void configWriterDateFormat(String dateFormat) {
        defaultWriterFormat = dateFormat;
    }

    /**
     * Config default reader zoneId
     *
     * <p>Changes the global defaults used by subsequently created contexts. Existing contexts retain their settings.</p>
     *
     * <details><summary>中文</summary>
     * 设置或查询默认配置；修改默认值不会追溯更新已有上下文。
     * </details>
     *
     * @param zoneId the zone ID to use for reading
     * @since 2.0.36
     */
    static void configReaderZoneId(ZoneId zoneId) {
        defaultReaderZoneId = zoneId;
    }

    /**
     * Config default writer zoneId
     *
     * <p>Changes the global defaults used by subsequently created contexts. Existing contexts retain their settings.</p>
     *
     * <details><summary>中文</summary>
     * 设置或查询默认配置；修改默认值不会追溯更新已有上下文。
     * </details>
     *
     * @param zoneId the zone ID to use for writing
     * @since 2.0.36
     */
    static void configWriterZoneId(ZoneId zoneId) {
        defaultWriterZoneId = zoneId;
    }

    /**
     * Enable the specified features in default writer
     *
     * <p>Changes the global defaults used by subsequently created contexts. Existing contexts retain their settings.</p>
     *
     * <details><summary>中文</summary>
     * 设置或查询默认配置；修改默认值不会追溯更新已有上下文。
     * </details>
     *
     * @param features the specified features to be used
     * @since 2.0.6
     */
    static void config(JSONWriter.Feature... features) {
        for (int i = 0; i < features.length; i++) {
            JSONFactory.defaultWriterFeatures |= features[i].mask;
        }
    }

    /**
     * Enable or disable the specified features in default writer
     *
     * <p>Changes the global defaults used by subsequently created contexts. Existing contexts retain their settings.</p>
     *
     * <details><summary>中文</summary>
     * 设置或查询默认配置；修改默认值不会追溯更新已有上下文。
     * </details>
     *
     * @param feature the specified feature to be used
     * @param state enable this feature if and only if {@code state} is {@code true}, disable otherwise
     * @since 2.0.6
     */
    static void config(JSONWriter.Feature feature, boolean state) {
        if (state) {
            JSONFactory.defaultWriterFeatures |= feature.mask;
        } else {
            JSONFactory.defaultWriterFeatures &= ~feature.mask;
        }
    }

    /**
     * Check if the default writer enables the specified feature
     *
     * <details><summary>中文</summary>
     * 设置或查询默认配置；修改默认值不会追溯更新已有上下文。
     * </details>
     *
     * @param feature the specified feature
     * @return true if the feature is enabled in the global defaults
     * @since 2.0.6
     */
    static boolean isEnabled(JSONWriter.Feature feature) {
        return (JSONFactory.defaultWriterFeatures & feature.mask) != 0;
    }

    /**
     * Copies a value using the registered readers and writers.
     * Values recognized as primitive-like or enums are returned unchanged. Other values
     * are copied through bean properties or a JSONB round trip; this does not guarantee
     * a new instance for every nested value or preservation of all object identities.
     *
     * <details><summary>中文</summary>
     * 通过已注册的读写器复制值；基础值和枚举可直接返回原值。其他值通过属性复制或 JSONB 转换，不保证每个嵌套值均创建新实例或保留全部对象身份。
     * </details>
     *
     * @param <T> the type of the object to copy
     * @param object the specified object will be copied
     * @param features the specified features is applied to serialization
     * @return the copied value, the original primitive-like/enum value, or null for a null input
     * @since 2.0.12
     */
    static <T> T copy(T object, JSONWriter.Feature... features) {
        if (object == null) {
            return null;
        }

        Class<?> objectClass = object.getClass();
        if (ObjectWriterProvider.isPrimitiveOrEnum(objectClass)) {
            return object;
        }

        boolean fieldBased = false, beanToArray = false;
        long featuresValue = JSONFactory.defaultReaderFeatures;
        for (int i = 0; i < features.length; i++) {
            JSONWriter.Feature feature = features[i];
            featuresValue |= feature.mask;
            if (feature == JSONWriter.Feature.FieldBased) {
                fieldBased = true;
            } else if (feature == JSONWriter.Feature.BeanToArray) {
                beanToArray = true;
            }
        }

        ObjectWriter objectWriter = defaultObjectWriterProvider.getObjectWriter(objectClass, objectClass, fieldBased);
        ObjectReader objectReader = defaultObjectReaderProvider.getObjectReader(objectClass, fieldBased);

        if (objectWriter instanceof ObjectWriterAdapter && objectReader instanceof ObjectReaderBean) {
            List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();

            final int size = fieldWriters.size();
            if (objectReader instanceof ObjectReaderNoneDefaultConstructor) {
                Map<String, Object> map = new HashMap<>(size, 1F);
                for (int i = 0; i < size; i++) {
                    FieldWriter fieldWriter = fieldWriters.get(i);
                    Object fieldValue = fieldWriter.getFieldValue(object);
                    map.put(fieldWriter.fieldName, fieldValue);
                }

                return (T) objectReader.createInstance(map, featuresValue);
            }

            T instance = (T) objectReader.createInstance(featuresValue);
            for (int i = 0; i < size; i++) {
                FieldWriter fieldWriter = fieldWriters.get(i);
                FieldReader fieldReader = objectReader.getFieldReader(fieldWriter.fieldName);
                if (fieldReader == null) {
                    continue;
                }

                Object fieldValue = fieldWriter.getFieldValue(object);
                Object fieldValueCopied = copy(fieldValue);
                fieldReader.accept(instance, fieldValueCopied);
            }

            return instance;
        }

        byte[] jsonbBytes;
        try (JSONWriter writer = JSONWriter.ofJSONB(features)) {
            writer.config(JSONWriter.Feature.WriteClassName);
            objectWriter.writeJSONB(writer, object, null, null, 0);
            jsonbBytes = writer.getBytes();
        }

        try (JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes, JSONReader.Feature.SupportAutoType, JSONReader.Feature.SupportClassForName)) {
            if (beanToArray) {
                jsonReader.context.config(JSONReader.Feature.SupportArrayToBean);
            }

            return (T) objectReader.readJSONBObject(jsonReader, null, null, featuresValue);
        }
    }

    /**
     * Creates a target instance using the source properties and registered codecs.
     * Matching properties are converted as required. Compatible property values may be
     * assigned directly, so mutable nested values can be shared with the source.
     *
     * <details><summary>中文</summary>
     * 使用源属性和已注册编解码器创建目标实例；匹配属性按需转换。兼容的属性值可直接赋值，因此可变嵌套值可能与源对象共享。
     * </details>
     *
     * @param <T> the target type
     * @param object the specified object will be copied
     * @param targetClass the class of the copy to create
     * @param features the specified features is applied to serialization
     * @return a new instance of targetClass with properties copied from the input object, or null if the input is null
     * @since 2.0.16
     */
    static <T> T copyTo(Object object, Class<T> targetClass, JSONWriter.Feature... features) {
        if (object == null) {
            return null;
        }

        Class<?> objectClass = object.getClass();

        boolean fieldBased = false, beanToArray = false;
        long featuresValue = JSONFactory.defaultReaderFeatures;
        for (int i = 0; i < features.length; i++) {
            JSONWriter.Feature feature = features[i];
            featuresValue |= feature.mask;
            if (feature == JSONWriter.Feature.FieldBased) {
                fieldBased = true;
            } else if (feature == JSONWriter.Feature.BeanToArray) {
                beanToArray = true;
            }
        }

        ObjectWriter objectWriter = defaultObjectWriterProvider.getObjectWriter(objectClass, objectClass, fieldBased);
        ObjectReader objectReader = defaultObjectReaderProvider.getObjectReader(targetClass, fieldBased);

        if (objectWriter instanceof ObjectWriterAdapter && objectReader instanceof ObjectReaderBean) {
            List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();

            if (objectReader instanceof ObjectReaderNoneDefaultConstructor) {
                Map<String, Object> map = new HashMap<>(fieldWriters.size(), 1F);
                for (int i = 0; i < fieldWriters.size(); i++) {
                    FieldWriter fieldWriter = fieldWriters.get(i);
                    Object fieldValue = fieldWriter.getFieldValue(object);
                    map.put(fieldWriter.fieldName, fieldValue);
                }

                return (T) objectReader.createInstance(map, featuresValue);
            }

            T instance = (T) objectReader.createInstance(featuresValue);
            for (int i = 0; i < fieldWriters.size(); i++) {
                FieldWriter fieldWriter = fieldWriters.get(i);
                FieldReader fieldReader = objectReader.getFieldReader(fieldWriter.fieldName);
                if (fieldReader == null) {
                    continue;
                }

                Object fieldValue = fieldWriter.getFieldValue(object);

                Object fieldValueCopied;
                if (fieldWriter.fieldClass == Date.class
                        && fieldReader.fieldClass == String.class) {
                    fieldValueCopied = DateUtils.format((Date) fieldValue, fieldWriter.format);
                } else if (fieldWriter.fieldClass == LocalDate.class
                        && fieldReader.fieldClass == String.class) {
                    fieldValueCopied = DateUtils.format((LocalDate) fieldValue, fieldWriter.format);
                } else if (fieldValue == null || fieldReader.supportAcceptType(fieldValue.getClass())) {
                    fieldValueCopied = fieldValue;
                } else {
                    fieldValueCopied = copy(fieldValue);
                }

                fieldReader.accept(instance, fieldValueCopied);
            }

            return instance;
        }

        byte[] jsonbBytes;
        try (JSONWriter writer = JSONWriter.ofJSONB(features)) {
            writer.config(JSONWriter.Feature.WriteClassName);
            objectWriter.writeJSONB(writer, object, null, null, 0);
            jsonbBytes = writer.getBytes();
        }

        try (JSONReader jsonReader = JSONReader.ofJSONB(
                jsonbBytes,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.SupportClassForName)
        ) {
            if (beanToArray) {
                jsonReader.context.config(JSONReader.Feature.SupportArrayToBean);
            }

            return (T) objectReader.readJSONBObject(jsonReader, null, null, 0);
        }
    }

    /**
     * Configure the Enum classes as a JavaBean
     *
     * <details><summary>中文</summary>
     * 设置或查询默认配置；修改默认值不会追溯更新已有上下文。
     * </details>
     *
     * @param enumClasses the enum classes to configure as JavaBeans
     * @since 2.0.55
     */
    @SuppressWarnings("rawtypes")
    @SafeVarargs
    static void configEnumAsJavaBean(Class<? extends Enum>... enumClasses) {
        JSONFactory.getDefaultObjectWriterProvider().configEnumAsJavaBean(enumClasses);
    }
}
