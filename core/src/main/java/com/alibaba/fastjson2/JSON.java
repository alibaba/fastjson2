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
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONReader.EOI;
import static com.alibaba.fastjson2.JSONReader.Feature.IgnoreCheckClose;
import static com.alibaba.fastjson2.JSONReader.Feature.UseNativeObject;

public interface JSON {
    /**
     * fastjson2 version name
     */
    String VERSION = "2.0.36";

    /**
     * Parses the json string as a {@link JSONArray} or {@link JSONObject}.
     * Returns {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified text to be parsed
     * @return either {@link JSONArray} or {@link JSONObject} or null
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as a {@link JSONArray} or {@link JSONObject}.
     * Returns {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified text to be parsed
     * @param features the specified features is applied to parsing
     * @return either {@link JSONArray} or {@link JSONObject} or null
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as a {@link JSONArray} or {@link JSONObject}.
     * Returns {@code null} if received {@link String} is {@code null} or empty or length is 0.
     *
     * @param text the specified text to be parsed
     * @param offset the starting index of string
     * @param length the specified length of string
     * @param features the specified features is applied to parsing
     * @return either {@link JSONArray} or {@link JSONObject} or null
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as a {@link JSONArray} or {@link JSONObject}.
     * Returns {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified text to be parsed
     * @param context the specified custom context
     * @return either {@link JSONArray} or {@link JSONObject} or null
     * @throws JSONException If a parsing error occurs
     * @throws NullPointerException If received context is null
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
     * Parses the json byte array as a {@link JSONArray} or {@link JSONObject}.
     * Returns {@code null} if received byte array is {@code null} or empty.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param features the specified features is applied to parsing
     * @return either {@link JSONArray} or {@link JSONObject} or null
     * @throws JSONException If a parsing error occurs
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
     * Parses the json char array as a {@link JSONArray} or {@link JSONObject}.
     * Returns {@code null} if received char array is {@code null} or empty.
     *
     * @param chars the specified char array to be parsed
     * @param features the specified features is applied to parsing
     * @return either {@link JSONArray} or {@link JSONObject} or null
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as a {@link JSONObject}. Returns {@code null}
     * if received {@link String} is {@code null} or empty or its content is null.
     *
     * @param text the specified string to be parsed
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as a {@link JSONObject}. Returns {@code null}
     * if received {@link String} is {@code null} or empty or its content is null.
     *
     * @param text the specified string to be parsed
     * @param features the specified features is applied to parsing
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as a {@link JSONObject}. Returns {@code null} if received
     * {@link String} is {@code null} or empty or length is 0 or its content is null.
     *
     * @param text the specified text to be parsed
     * @param offset the starting index of string
     * @param length the specified length of string
     * @param features the specified features is applied to parsing
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as a {@link JSONObject}. Returns {@code null} if received
     * {@link String} is {@code null} or empty or length is 0 or its content is null.
     *
     * @param text the specified text to be parsed
     * @param offset the starting index of string
     * @param length the specified length of string
     * @param context the specified custom context
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
     * @throws NullPointerException If received context is null
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
     * Parses the json string as a {@link JSONObject}. Returns {@code null} if
     * received {@link String} is {@code null} or empty or its content is null.
     *
     * @param text the specified string to be parsed
     * @param context the specified custom context
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
     * @throws NullPointerException If received context is null
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
     * Parses the json reader as a {@link JSONObject}. Returns {@code null}
     * if received {@link Reader} is {@code null} or its content is null.
     *
     * @param input the specified reader to be parsed
     * @param features the specified features is applied to parsing
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json stream as a {@link JSONObject}. Returns {@code null} if
     * received {@link InputStream} is {@code null} or closed or its content is null.
     *
     * @param input the specified stream to be parsed
     * @param features the specified features is applied to parsing
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as a {@link JSONObject}. Returns {@code null}
     * if received byte array is {@code null} or empty or its content is null.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json char array as a {@link JSONObject}. Returns {@code null}
     * if received char array is {@code null} or empty or its content is null.
     *
     * @param chars the specified char array to be parsed
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json stream as a {@link JSONObject}. Returns {@code null}
     * if received {@link InputStream} is {@code null} or its content is null.
     *
     * @param in the specified stream to be parsed
     * @param charset the specified charset of the stream
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json stream of the url as a {@link JSONObject}.
     * Returns {@code null} if received {@link URL} is {@code null}.
     *
     * @param url the specified url to be parsed
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If an I/O error or parsing error occurs
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
     * Parses the json byte array as a {@link JSONObject}. Returns {@code null}
     * if received byte array is {@code null} or empty or its content is null.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param features the specified features is applied to parsing
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as a {@link JSONObject}. Returns {@code null} if
     * received byte array is {@code null} or empty or length is 0 or its content is null.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param offset the starting index of array
     * @param length the specified length of array
     * @param features the specified features is applied to parsing
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json chars array as a {@link JSONObject}. Returns {@code null} if
     * received chars array is {@code null} or empty or length is 0 or its content is null.
     *
     * @param chars the specified chars array to be parsed
     * @param offset the starting index of array
     * @param length the specified length of array
     * @param features the specified features is applied to parsing
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as a {@link JSONObject}. Returns {@code null} if
     * received byte array is {@code null} or empty or length is 0 or its content is null.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param offset the starting index of array
     * @param length the specified length of array
     * @param charset the specified charset of the stream
     * @param features the specified features is applied to parsing
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param clazz the specified class of {@link T}
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as {@link T}. Returns {@code null}
     * if received {@link String} is {@code null} or empty or its content is null.
     *
     * @param text the specified string to be parsed
     * @param clazz the specified class of {@link T}
     * @param filter the specified filter is applied to parsing
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as {@link T}. Returns {@code null}
     * if received {@link String} is {@code null} or empty or its content is null.
     *
     * @param text the specified string to be parsed
     * @param type the specified actual type of {@link T}
     * @param format the specified date format
     * @param filters the specified filters is applied to parsing
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param type the specified actual type of {@link T}
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param type the specified actual type of {@link T}
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param types the specified actual parameter types
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
     * @see MultiType
     * @see JSON#parseObject(String, Type)
     */
    static <T> T parseObject(String text, Type... types) {
        return parseObject(text, new MultiType(types));
    }

    /**
     * Parses the json string as {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param typeReference the specified actual type
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param typeReference the specified actual type
     * @param filter the specified filter is applied to parsing
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param clazz the specified class of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as {@link T}. Returns {@code null}
     * if received {@link String} is {@code null} or empty or length is 0.
     *
     * @param text the specified string to be parsed
     * @param offset the starting index of string
     * @param length the specified length of string
     * @param clazz the specified class of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param clazz the specified class of {@link T}
     * @param context the specified custom context
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
     * @throws NullPointerException If received context is null
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
     * Parses the json string as {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param clazz the specified class of {@link T}
     * @param format the specified date format
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param type the specified actual type
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param type the specified actual type
     * @param filter the specified filter is applied to parsing
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param type the specified actual type
     * @param format the specified date format
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json char array as {@link T}. Returns {@code null}
     * if received char array is {@code null} or empty or length is 0.
     *
     * @param chars the specified char array to be parsed
     * @param type the specified actual type
     * @param offset the starting index of array
     * @param length the specified length of array
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json char array as {@link T}. Returns
     * {@code null} if received char array is {@code null} or empty.
     *
     * @param chars the specified char array to be parsed
     * @param clazz the specified class of {@link T}
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as {@link T}. Returns {@code null}
     * if received byte array is {@code null} or empty or length is 0.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param offset the starting index of array
     * @param length the specified length of array
     * @param type the specified actual type
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as {@link T}. Returns
     * {@code null} if received byte array is {@code null} or empty.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param type the specified actual type of {@link T}
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as {@link T}. Returns
     * {@code null} if received byte array is {@code null} or empty.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param clazz the specified class of {@link T}
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as {@link T}. Returns
     * {@code null} if received byte array is {@code null} or empty.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param clazz the specified class of {@link T}
     * @param filter the specified filter is applied to parsing
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as {@link T}. Returns
     * {@code null} if received byte array is {@code null} or empty.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param clazz the specified class of {@link T}
     * @param context the specified custom context
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
     * @throws NullPointerException If received context is null
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
     * Parses the json byte array as {@link T}. Returns
     * {@code null} if received byte array is {@code null} or empty.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param type the specified actual type
     * @param format the specified date format
     * @param filters the specified filters is applied to parsing
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    @SuppressWarnings("unchecked")
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
     * Parses the json byte array as {@link T}. Returns
     * {@code null} if received byte array is {@code null} or empty.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param clazz the specified class of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as {@link T}. Returns
     * {@code null} if received byte array is {@code null} or empty.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param type the specified actual type of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as {@link T}. Returns
     * {@code null} if received byte array is {@code null} or empty.
     *
     * @param chars the specified chars
     * @param objectClass the specified actual type of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as {@link T}. Returns
     * {@code null} if received byte array is {@code null} or empty.
     *
     * @param chars the specified chars
     * @param type the specified actual type of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as {@link T}. Returns
     * {@code null} if received byte array is {@code null} or empty.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param type the specified actual type of {@link T}
     * @param filter the specified filter is applied to parsing
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as {@link T}. Returns
     * {@code null} if received byte array is {@code null} or empty.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param type the specified actual type of {@link T}
     * @param format the specified date format
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte buffer as a {@link T}. Returns
     * {@code null} if received {@link ByteBuffer} is {@code null}.
     *
     * @param buffer the specified buffer to be parsed
     * @param objectClass the specified class of {@link T}
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json reader as a {@link T}. Returns {@code null}
     * if received {@link Reader} is {@code null} or its content is null.
     *
     * @param input the specified reader to be parsed
     * @param type the specified actual type of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json stream as a {@link T}. Returns {@code null}
     * if received {@link InputStream} is {@code null} or its content is null.
     *
     * @param input the specified stream to be parsed
     * @param type the specified actual type of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json stream of the url as {@link T}.
     * Returns {@code null} if received {@link URL} is {@code null}.
     *
     * @param url the specified url to be parsed
     * @param type the specified actual type of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If an I/O error or parsing error occurs
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
     * Parses the json stream of the url as {@link T}.
     * Returns {@code null} if received {@link URL} is {@code null}.
     *
     * @param url the specified url to be parsed
     * @param objectClass the specified class of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If an I/O error or parsing error occurs
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
     * Parses the json stream of the url as a {@link JSONObject} and call the function
     * to convert it to {@link T}. Returns {@code null} if received {@link URL} is {@code null}.
     *
     * @param url the specified url to be parsed
     * @param function the specified converter
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If an I/O error or parsing error occurs
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
     * Parses the json stream as a {@link T}. Returns {@code null}
     * if received {@link InputStream} is {@code null} or its content is null.
     *
     * @param input the specified stream to be parsed
     * @param type the specified actual type of {@link T}
     * @param format the specified date format
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json stream as a {@link T}. Returns {@code null}
     * if received {@link InputStream} is {@code null} or its content is null.
     *
     * @param input the specified stream to be parsed
     * @param charset the specified charset of the stream
     * @param type the specified actual type of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as {@link T}. Returns {@code null}
     * if received byte array is {@code null} or empty or length is 0.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param offset the starting index of array
     * @param length the specified length of array
     * @param charset the specified charset of the stream
     * @param type the specified actual type of {@link T}
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as {@link T}. Returns {@code null}
     * if received byte array is {@code null} or empty or length is 0.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param offset the starting index of array
     * @param length the specified length of array
     * @param charset the specified charset of the stream
     * @param type the specified actual type of {@link T}
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as {@link T}. Returns {@code null}
     * if received byte array is {@code null} or empty or length is 0.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param offset the starting index of array
     * @param length the specified length of array
     * @param charset the specified charset of the stream
     * @param type the specified actual class of {@link T}
     * @return {@link T} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json stream through the specified delimiter as
     * {@link T} objects and call the specified consumer to consume it
     *
     * @param input the specified stream to be parsed
     * @param type the specified actual class of {@link T}
     * @param consumer the specified consumer is called multiple times
     * @param features the specified features is applied to parsing
     * @throws JSONException If an I/O error or parsing error occurs
     * @throws NullPointerException If the specified stream is null
     * @since 2.0.2
     */
    static <T> void parseObject(InputStream input, Type type, Consumer<T> consumer, JSONReader.Feature... features) {
        parseObject(input, StandardCharsets.UTF_8, '\n', type, consumer, features);
    }

    /**
     * Parses the json stream through the specified delimiter as
     * {@link T} objects and call the specified consumer to consume it
     *
     * @param input the specified stream to be parsed
     * @param charset the specified charset of the stream
     * @param type the specified actual class of {@link T}
     * @param delimiter the specified delimiter for the stream
     * @param consumer the specified consumer is called multiple times
     * @param features the specified features is applied to parsing
     * @throws JSONException If an I/O error or parsing error occurs
     * @throws NullPointerException If the specified stream is null
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
                if (n == -1) {
                    break;
                }

                int k = offset;
                offset += n;
                boolean dispose = false;

                for (; k < offset; ++k) {
                    if (bytes[k] == delimiter) {
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
     * Parses the json reader through the specified delimiter as
     * {@link T} objects and call the specified consumer to consume it
     *
     * @param input the specified reader to be parsed
     * @param type the specified actual class of {@link T}
     * @param delimiter the specified delimiter for the stream
     * @param consumer the specified consumer is called multiple times
     * @throws JSONException If an I/O error or parsing error occurs
     * @throws NullPointerException If the specified reader is null
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
                if (n == -1) {
                    break;
                }

                int k = offset;
                offset += n;
                boolean dispose = false;

                for (; k < offset; ++k) {
                    if (chars[k] == delimiter) {
                        end = k;

                        JSONReader jsonReader = JSONReader.of(chars, start, end - start, context);
                        if (objectReader == null) {
                            objectReader = context.getObjectReader(type);
                        }

                        consumer.accept(
                                objectReader.readObject(jsonReader, type, null, 0)
                        );
                        start = end + 1;
                        dispose = true;
                    }
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
     * Parses the json string as a {@link JSONArray}. Returns {@code null} if
     * received {@link String} is {@code null} or empty or its content is null.
     *
     * @param text the specified string to be parsed
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as a {@link JSONArray}. Returns {@code null}
     * if received byte array is {@code null} or empty or its content is null.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as a {@link JSONArray}. Returns {@code null}
     * if received byte array is {@code null} or empty or length is 0 or its content is null.
     *
     * @param bytes the specified byte array to be parsed
     * @param offset the starting index of array
     * @param length the specified length of array
     * @param charset the specified charset of the stream
     * @throws JSONException If a parsing error occurs
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
     * Parses the json char array as a {@link JSONArray}. Returns {@code null}
     * if received byte array is {@code null} or empty or its content is null.
     *
     * @param chars the specified char array to be parsed
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as a {@link JSONArray}. Returns {@code null}
     * if received {@link String} is {@code null} or empty or its content is null.
     *
     * @param text the specified string to be parsed
     * @param features the specified features is applied to parsing
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json stream of the url as a {@link JSONArray}.
     * Returns {@code null} if received {@link URL} is {@code null}.
     *
     * @param url the specified url to be parsed
     * @param features the specified features is applied to parsing
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException If an I/O error or parsing error occurs
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
     * Parses the json stream as a {@link JSONArray}. Returns {@code null}
     * if received {@link InputStream} is {@code null} or its content is null.
     *
     * @param in the specified stream to be parsed
     * @param features the specified features is applied to parsing
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException If an I/O error or parsing error occurs
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
     * Parses the json string as a list of {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param type the specified actual type of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link List} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as a list of {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param type the specified actual type of {@link T}
     * @return {@link List} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as a list of {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param type the specified actual class of {@link T}
     * @return {@link List} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as a list of {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param types the specified actual parameter type
     * @return {@link List} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as a list of {@link T}. Returns
     * {@code null} if received {@link String} is {@code null} or empty.
     *
     * @param text the specified string to be parsed
     * @param type the specified actual class of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link List} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json char array as a list of {@link T}. Returns
     * {@code null} if received char array is {@code null} or empty.
     *
     * @param chars the specified char array to be parsed
     * @param type the specified actual class of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link List} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json string as a list of {@link T}. Returns {@code null}
     * if received {@link String} is {@code null} or empty or its content is null.
     *
     * @param text the specified string to be parsed
     * @param types the specified actual parameter type
     * @param features the specified features is applied to parsing
     * @return {@link List} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as a list of {@link T}. Returns
     * {@code null} if received byte array is {@code null} or empty.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param type the specified actual type of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link List} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as a list of {@link T}. Returns
     * {@code null} if received byte array is {@code null} or empty.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param type the specified actual class of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link List} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Parses the json byte array as a list of {@link T}. Returns {@code null}
     * if received byte array is {@code null} or empty or the specified length is 0.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param offset the starting index of array
     * @param length the specified length of array
     * @param charset the specified charset of the stream
     * @param type the specified actual class of {@link T}
     * @param features the specified features is applied to parsing
     * @return {@link List} or {@code null}
     * @throws JSONException If a parsing error occurs
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
     * Serializes the specified object to the json string
     *
     * @param object the specified object will be serialized
     * @return {@link String} that is not null
     * @throws JSONException If a serialization error occurs
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
     * Serializes the specified object to the json string
     *
     * @param object the specified object will be serialized
     * @param context the specified custom context
     * @return {@link String} that is not null
     * @throws JSONException If a serialization error occurs
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
     * Serializes the specified object to the json string
     *
     * @param object the specified object will be serialized
     * @param features the specified features is applied to serialization
     * @return {@link String} that is not null
     * @throws JSONException If a serialization error occurs
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
     * Serializes the specified object to the json string
     *
     * @param object the specified object will be serialized
     * @param filter the specified filter is applied to serialization
     * @param features the specified features is applied to serialization
     * @return {@link String} that is not null
     * @throws JSONException If a serialization error occurs
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
     * Serializes the specified object to the json string
     *
     * @param object the specified object will be serialized
     * @param filters the specified filters is applied to serialization
     * @param features the specified features is applied to serialization
     * @return {@link String} that is not null
     * @throws JSONException If a serialization error occurs
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
     * Serializes the specified object to the json string
     *
     * @param object the specified object will be serialized
     * @param format the specified date format
     * @param features the specified features is applied to serialization
     * @return {@link String} that is not null
     * @throws JSONException If a serialization error occurs
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
     * Serializes the specified object to the json string
     *
     * @param object the specified object will be serialized
     * @param format the specified date format
     * @param filters the specified filters is applied to serialization
     * @param features the specified features is applied to serialization
     * @return {@link String} that is not null
     * @throws JSONException If a serialization error occurs
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
     * Serializes the specified object to the json byte array
     *
     * @param object the specified object will be serialized
     * @return {@code byte[]} that is not null
     * @throws JSONException If a serialization error occurs
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
     * Serializes the specified object to the json byte array
     *
     * @param object the specified object will be serialized
     * @param format the specified date format
     * @param features the specified features is applied to serialization
     * @return {@code byte[]} that is not null
     * @throws JSONException If a serialization error occurs
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
     * Serializes the specified object to the json byte array
     *
     * @param object the specified object will be serialized
     * @param filters the specified filters is applied to serialization
     * @return {@code byte[]} that is not null
     * @throws JSONException If a serialization error occurs
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
     * Serializes the specified object to the json byte array
     *
     * @param object the specified object will be serialized
     * @param features the specified features is applied to serialization
     * @return {@code byte[]} that is not null
     * @throws JSONException If a serialization error occurs
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
     * Serializes the specified object to the json byte array
     *
     * @param object the specified object will be serialized
     * @param filters the specified filters is applied to serialization
     * @param features the specified features is applied to serialization
     * @return {@code byte[]} that is not null
     * @throws JSONException If a serialization error occurs
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
     * Serializes the specified object to the json byte array
     *
     * @param object the specified object will be serialized
     * @param format the specified date format
     * @param filters the specified filters is applied to serialization
     * @param features the specified features is applied to serialization
     * @return {@code byte[]} that is not null
     * @throws JSONException If a serialization error occurs
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
     * Serializes the specified object to the json byte array and write it to {@link OutputStream}
     *
     * @param out the specified output stream to be written
     * @param object the specified object will be serialized
     * @return the length of byte stream
     * @throws JSONException If an I/O error or serialization error occurs
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
     * Serializes the specified object to the json byte array and write it to {@link OutputStream}
     *
     * @param out the specified output stream to be written
     * @param object the specified object will be serialized
     * @param features the specified features is applied to serialization
     * @return the length of byte stream
     * @throws JSONException If an I/O error or serialization error occurs
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
     * Serializes the specified object to the json byte array and write it to {@link OutputStream}
     *
     * @param out the specified output stream to be written
     * @param object the specified object will be serialized
     * @param filters the specified filters is applied to serialization
     * @param features the specified features is applied to serialization
     * @return the length of byte stream
     * @throws JSONException If an I/O error or serialization error occurs
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
     * Serializes the specified object to the json byte array and write it to {@link OutputStream}
     *
     * @param out the specified output stream to be written
     * @param object the specified object will be serialized
     * @param format the specified date format
     * @param filters the specified filters is applied to serialization
     * @param features the specified features is applied to serialization
     * @return the length of byte stream
     * @throws JSONException If an I/O error or serialization error occurs
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
     * Verify that the json string is legal json text
     *
     * @param text the specified string will be validated
     * @return {@code true} or {@code false}
     */
    static boolean isValid(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(text)) {
            jsonReader.skipValue();
            return jsonReader.isEnd() && !jsonReader.comma;
        } catch (JSONException error) {
            return false;
        }
    }

    /**
     * Verify that the json char array is legal json text
     *
     * @param chars the specified array will be validated
     * @return {@code true} or {@code false}
     */
    static boolean isValid(char[] chars) {
        if (chars == null || chars.length == 0) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(chars)) {
            jsonReader.skipValue();
            return jsonReader.isEnd();
        } catch (JSONException error) {
            return false;
        }
    }

    /**
     * Verify that the json string is a legal JsonObject
     *
     * @param text the specified string will be validated
     * @return {@code true} or {@code false}
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
            return jsonReader.isEnd();
        } catch (JSONException error) {
            return false;
        }
    }

    /**
     * Verify that the json byte array is a legal JsonObject
     *
     * @param bytes the specified array will be validated
     * @return {@code true} or {@code false}
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
            return jsonReader.isEnd();
        } catch (JSONException error) {
            return false;
        }
    }

    /**
     * Verify the {@link String} is JSON Array
     *
     * @param text the {@link String} to validate
     * @return {@code true} or {@code false}
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
            return jsonReader.isEnd();
        } catch (JSONException error) {
            return false;
        }
    }

    /**
     * Verify that the json byte array is legal json text
     *
     * @param bytes the specified array will be validated
     * @return {@code true} or {@code false}
     */
    static boolean isValid(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(bytes)) {
            jsonReader.skipValue();
            return jsonReader.isEnd();
        } catch (JSONException error) {
            return false;
        }
    }

    /**
     * Verify that the json byte array is a legal JsonArray
     *
     * @param bytes the specified array will be validated
     * @return {@code true} or {@code false}
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
            return jsonReader.isEnd();
        } catch (JSONException error) {
            return false;
        }
    }

    /**
     * Verify that the json byte array is legal json text
     *
     * @param bytes the specified array will be validated
     * @param offset the starting index of array
     * @param length the specified length of array
     * @param charset the specified charset of the stream
     * @return {@code true} or {@code false}
     */
    static boolean isValid(byte[] bytes, int offset, int length, Charset charset) {
        if (bytes == null || bytes.length == 0 || length == 0) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(bytes, offset, length, charset)) {
            jsonReader.skipValue();
            return jsonReader.isEnd();
        } catch (JSONException error) {
            return false;
        }
    }

    /**
     * Converts the specified object to a {@link JSONArray} or
     * {@link JSONObject}. Returns {@code null} if received object is {@code null}
     *
     * @param object the specified object to be converted
     * @return {@link JSONArray} or {@link JSONObject} or {@code null}
     */
    static Object toJSON(Object object) {
        return toJSON(object, (JSONWriter.Feature[]) null);
    }

    /**
     * Converts the specified object to a {@link JSONArray} or
     * {@link JSONObject}. Returns {@code null} if received object is {@code null}
     *
     * @param object the specified object to be converted
     * @param features the specified features is applied to serialization
     * @return {@link JSONArray} or {@link JSONObject} or {@code null}
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
        if (objectWriter instanceof ObjectWriterAdapter && !writeContext.isEnabled(JSONWriter.Feature.ReferenceDetection)) {
            ObjectWriterAdapter objectWriterAdapter = (ObjectWriterAdapter) objectWriter;
            return objectWriterAdapter.toJSONObject(object);
        }

        String str;
        try (JSONWriter writer = JSONWriter.of(writeContext)) {
            objectWriter.write(writer, object, null, null, 0);
            str = writer.toString();
        } catch (NullPointerException | NumberFormatException ex) {
            throw new JSONException("toJSONString error", ex);
        }

        return parse(str);
    }

    /**
     * Converts the specified object to an object of the specified goal type
     *
     * @param clazz the specified goal class
     * @param object the specified object to be converted
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
     * @param clazz the specified goal class
     * @param object the specified object to be converted
     * @deprecated since 2.0.4, please use {@link #to(Class, Object)}
     */
    static <T> T toJavaObject(Object object, Class<T> clazz) {
        return to(clazz, object);
    }

    /**
     * @since 2.0.2
     */
    static void mixIn(Class<?> target, Class<?> mixinSource) {
        JSONFactory.defaultObjectWriterProvider.mixIn(target, mixinSource);
        JSONFactory.getDefaultObjectReaderProvider().mixIn(target, mixinSource);
    }

    /**
     * Register an {@link ObjectReader} for {@link Type} in default {@link com.alibaba.fastjson2.reader.ObjectReaderProvider}
     *
     * @see JSONFactory#getDefaultObjectReaderProvider()
     * @see com.alibaba.fastjson2.reader.ObjectReaderProvider#register(Type, ObjectReader)
     * @since 2.0.2
     */
    static ObjectReader<?> register(Type type, ObjectReader<?> objectReader) {
        return JSONFactory.getDefaultObjectReaderProvider().register(type, objectReader);
    }

    /**
     * Register if absent an {@link ObjectReader} for {@link Type} in default {@link com.alibaba.fastjson2.reader.ObjectReaderProvider}
     *
     * @see JSONFactory#getDefaultObjectReaderProvider()
     * @see com.alibaba.fastjson2.reader.ObjectReaderProvider#registerIfAbsent(Type, ObjectReader)
     * @since 2.0.6
     */
    static ObjectReader<?> registerIfAbsent(Type type, ObjectReader<?> objectReader) {
        return JSONFactory.getDefaultObjectReaderProvider().registerIfAbsent(type, objectReader);
    }

    /**
     * Register an {@link ObjectReaderModule} in default {@link com.alibaba.fastjson2.reader.ObjectReaderProvider}
     *
     * @see JSONFactory#getDefaultObjectReaderProvider()
     * @see com.alibaba.fastjson2.reader.ObjectReaderProvider#register(ObjectReaderModule)
     */
    static boolean register(ObjectReaderModule objectReaderModule) {
        ObjectReaderProvider provider = getDefaultObjectReaderProvider();
        return provider.register(objectReaderModule);
    }

    static void registerSeeAlsoSubType(Class subTypeClass) {
        registerSeeAlsoSubType(subTypeClass, null);
    }

    static void registerSeeAlsoSubType(Class subTypeClass, String subTypeClassName) {
        ObjectReaderProvider provider = getDefaultObjectReaderProvider();
        provider.registerSeeAlsoSubType(subTypeClass, subTypeClassName);
    }

    /**
     * Register an {@link ObjectWriterModule} in default {@link  com.alibaba.fastjson2.writer.ObjectWriterProvider}
     *
     * @see JSONFactory#getDefaultObjectWriterProvider()
     * @see com.alibaba.fastjson2.writer.ObjectWriterProvider#register(ObjectWriterModule)
     */
    static boolean register(ObjectWriterModule objectWriterModule) {
        return JSONFactory.getDefaultObjectWriterProvider().register(objectWriterModule);
    }

    /**
     * Register an {@link ObjectWriter} for {@link Type} in default {@link  com.alibaba.fastjson2.writer.ObjectWriterProvider}
     *
     * @see JSONFactory#getDefaultObjectWriterProvider()
     * @see com.alibaba.fastjson2.writer.ObjectWriterProvider#register(Type, ObjectWriter)
     * @since 2.0.2
     */
    static ObjectWriter<?> register(Type type, ObjectWriter<?> objectWriter) {
        return JSONFactory.getDefaultObjectWriterProvider().register(type, objectWriter);
    }

    /**
     * Register if absent an {@link ObjectWriter} for {@link Type} in default {@link  com.alibaba.fastjson2.writer.ObjectWriterProvider}
     *
     * @see JSONFactory#getDefaultObjectWriterProvider()
     * @see com.alibaba.fastjson2.writer.ObjectWriterProvider#registerIfAbsent(Type, ObjectWriter)
     * @since 2.0.6
     */
    static ObjectWriter<?> registerIfAbsent(Type type, ObjectWriter<?> objectWriter) {
        return JSONFactory.getDefaultObjectWriterProvider().registerIfAbsent(type, objectWriter);
    }

    /**
     * Register ObjectWriterFilter
     *
     * @param type
     * @param filter
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
     * @param features the specified features to be used
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
     * @param feature the specified feature to be used
     * @param state enable this feature if and only if {@code state} is {@code true}, disable otherwise
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
     * @param feature the specified feature
     * @since 2.0.6
     */
    static boolean isEnabled(JSONReader.Feature feature) {
        return (JSONFactory.defaultReaderFeatures & feature.mask) != 0;
    }

    /**
     * config default reader dateFormat
     *
     * @param dateFormat
     * @since 2.0.30
     */
    static void configReaderDateFormat(String dateFormat) {
        defaultReaderFormat = dateFormat;
    }

    /**
     * config default reader dateFormat
     *
     * @param dateFormat
     */
    static void configWriterDateFormat(String dateFormat) {
        defaultWriterFormat = dateFormat;
    }

    /**
     * Enable the specified features in default writer
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
     * @param feature the specified feature
     * @since 2.0.6
     */
    static boolean isEnabled(JSONWriter.Feature feature) {
        return (JSONFactory.defaultWriterFeatures & feature.mask) != 0;
    }

    /**
     * Builds a new {@link T} using the properties of the specified object
     *
     * @param object the specified object will be copied
     * @param features the specified features is applied to serialization
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
        long featuresValue = 0;
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

            if (objectReader instanceof ObjectReaderNoneDefaultConstructor) {
                Map<String, Object> map = new HashMap(fieldWriters.size());
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
     * Builds a new instance of targetClass using the properties of the specified object
     *
     * @param object the specified object will be copied
     * @param targetClass the specified target class
     * @param features the specified features is applied to serialization
     * @since 2.0.16
     */
    static <T> T copyTo(Object object, Class<T> targetClass, JSONWriter.Feature... features) {
        if (object == null) {
            return null;
        }

        Class<?> objectClass = object.getClass();

        boolean fieldBased = false, beanToArray = false;
        long featuresValue = 0;
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
                Map<String, Object> map = new HashMap(fieldWriters.size());
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
}
