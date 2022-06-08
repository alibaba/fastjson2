package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Function;

public interface JSON {
    /**
     * FASTJSON2 version name
     */
    String VERSION = "2.0.7";

    /**
     * Parse JSON {@link String} into {@link JSONArray} or {@link JSONObject}
     *
     * @param text the JSON {@link String} to be parsed
     * @return Object
     */
    static Object parse(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            ObjectReader<?> objectReader = reader.getObjectReader(Object.class);
            return objectReader.readObject(reader, 0);
        }
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray} or {@link JSONObject} with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param features features to be enabled in parsing
     * @return Object
     */
    static Object parse(String text, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            reader.context.config(features);
            ObjectReader<?> objectReader = reader.getObjectReader(Object.class);
            return objectReader.readObject(reader, 0);
        }
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray} or {@link JSONObject} with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param offset   the index of the first byte to parse
     * @param length   the number of bytes to parse
     * @param features features to be enabled in parsing
     * @return Object
     */
    static Object parse(String text, int offset, int length, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text, offset, length)) {
            reader.context.config(features);
            ObjectReader<?> objectReader = reader.getObjectReader(Object.class);
            return objectReader.readObject(reader, 0);
        }
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray} or {@link JSONObject} with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param context specify the context use by JSONReader
     * @return Object
     */
    static Object parse(String text, JSONReader.Context context) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(context, text)) {
            ObjectReader<?> objectReader = reader.getObjectReader(Object.class);
            return objectReader.readObject(reader, 0);
        }
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray} or {@link JSONObject} with specified {@link JSONReader.Feature}s enabled
     *
     * @param bytes     the UTF8 Bytes to be parsed
     * @param features features to be enabled in parsing
     * @return Object
     */
    static Object parse(byte[] bytes, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes)) {
            reader.context.config(features);
            ObjectReader<?> objectReader = reader.getObjectReader(Object.class);
            return objectReader.readObject(reader, 0);
        }
    }

    /**
     * Parse JSON {@link String} into {@link JSONObject}
     *
     * @param text the JSON {@link String} to be parsed
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    static JSONObject parseObject(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0L);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into {@link JSONObject}
     *
     * @param text     the JSON {@link String} to be parsed
     * @param features features to be enabled in parsing
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    static JSONObject parseObject(String text, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            if (reader.nextIfNull()) {
                return null;
            }
            reader.context.config(features);
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into {@link JSONObject}
     *
     * @param text     the JSON {@link String} to be parsed
     * @param offset   the index of the first byte to parse
     * @param length   the number of bytes to parse
     * @param features features to be enabled in parsing
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    static JSONObject parseObject(String text, int offset, int length, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text, offset, length)) {
            if (reader.nextIfNull()) {
                return null;
            }
            reader.context.config(features);
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into {@link JSONObject}
     *
     * @param text     the JSON {@link String} to be parsed
     * @param context specify the context use by JSONReader
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    static JSONObject parseObject(String text, JSONReader.Context context) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(context, text)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse Reader into into {@link JSONObject}
     *
     * @param input    the JSON {@link InputStream} to be parsed
     * @param features features to be enabled in parsing
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    static JSONObject parseObject(Reader input, JSONReader.Feature... features) {
        if (input == null) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(input)) {
            if (reader.isEnd()) {
                return null;
            }

            reader.getContext().config(features);
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 inputStream into into {@link JSONObject}
     *
     * @param input    the JSON {@link InputStream} to be parsed
     * @param features features to be enabled in parsing
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    static JSONObject parseObject(InputStream input, JSONReader.Feature... features) {
        if (input == null) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(input, StandardCharsets.UTF_8)) {
            if (reader.isEnd()) {
                return null;
            }

            reader.getContext().config(features);
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into {@link JSONObject}
     *
     * @param bytes UTF8 encoded JSON byte array to parse
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    static JSONObject parseObject(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes)) {
            if (reader.nextIfNull()) {
                return null;
            }

            JSONObject object = new JSONObject();
            reader.read(object, 0L);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into {@link JSONObject}
     *
     * @param in the JSON {@link InputStream} to be parsed
     * @return JSONObject
     */
    static JSONObject parseObject(InputStream in, Charset charset) {
        if (in == null) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(in, charset)) {
            if (reader.nextIfNull()) {
                return null;
            }

            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into {@link JSONObject}
     *
     * @param url the JSON {@link URL} to be parsed
     * @return JSONObject
     */
    static JSONObject parseObject(URL url) {
        if (url == null) {
            return null;
        }

        try (InputStream is = url.openStream()) {
            return parseObject(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JSONException("parseObject error", e);
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into {@link JSONObject}
     *
     * @param bytes    UTF8 encoded JSON byte array to parse
     * @param features features to be enabled in parsing
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    static JSONObject parseObject(byte[] bytes, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes)) {
            if (reader.nextIfNull()) {
                return null;
            }
            reader.context.config(features);
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into {@link JSONObject}
     *
     * @param bytes    UTF8 encoded JSON byte array to parse
     * @param offset   the index of the first byte to parse
     * @param length   the number of bytes to parse
     * @param features features to be enabled in parsing
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    static JSONObject parseObject(byte[] bytes, int offset, int length, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes, offset, length)) {
            if (reader.nextIfNull()) {
                return null;
            }
            reader.context.config(features);
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into {@link JSONObject}
     *
     * @param bytes    UTF8 encoded JSON byte array to parse
     * @param offset   the index of the first byte to parse
     * @param length   the number of bytes to parse
     * @param charset  specify {@link Charset} to parse
     * @param features features to be enabled in parsing
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    static JSONObject parseObject(byte[] bytes, int offset, int length, Charset charset, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes, offset, length, charset)) {
            if (reader.nextIfNull()) {
                return null;
            }
            reader.context.config(features);
            JSONObject object = new JSONObject();
            reader.read(object, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into Java Object
     *
     * @param text  the JSON {@link String} to be parsed
     * @param clazz specify the Class to be converted
     * @return Class
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Class<T> clazz) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            JSONReader.Context context = reader.context;

            boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into Java Object
     *
     * @param text     the JSON {@link String} to be parsed
     * @param clazz    specify the Class to be converted
     * @param filter   specify filter to be enabled
     * @param features features to be enabled in parsing
     * @return Class
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(
            String text,
            Class<T> clazz,
            Filter filter,
            JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            JSONReader.Context context = reader.context;
            reader.context.config(filter, features);

            boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into Java Object
     *
     * @param text     the JSON {@link String} to be parsed
     * @param type     specify the {@link Type} to be converted
     * @param format   the specified date format
     * @param filters  specify filters to be enabled
     * @param features features to be enabled in parsing
     * @return Class
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(
            String text,
            Type type,
            String format,
            Filter[] filters,
            JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            JSONReader.Context context = reader.context;
            context.setUtilDateFormat(format);
            context.config(filters, features);
            boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader<T> objectReader = context.provider.getObjectReader(type, fieldBased);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into Java Object
     *
     * @param text the JSON {@link String} to be parsed
     * @param type specify the {@link Type} to be converted
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Type type) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            ObjectReader<T> objectReader = reader.context.provider.getObjectReader(type);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into Java Object
     *
     * @param text          the JSON {@link String} to be parsed
     * @param typeReference specify the {@link TypeReference} to be converted
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T> T parseObject(String text, TypeReference typeReference, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            JSONReader.Context context = reader.context;
            context.config(features);
            Type type = typeReference.getType();
            boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader<T> objectReader = context.provider.getObjectReader(type, fieldBased);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into Java Object
     *
     * @param text          the JSON {@link String} to be parsed
     * @param typeReference specify the {@link TypeReference} to be converted
     * @param filter   specify filters to be enabled
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T> T parseObject(String text, TypeReference typeReference, Filter filter, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            JSONReader.Context context = reader.context;
            context.config(filter, features);
            Type type = typeReference.getType();
            boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader<T> objectReader = context.provider.getObjectReader(type, fieldBased);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param clazz    specify the Class to be converted
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Class<T> clazz, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            JSONReader.Context context = reader.context;
            context.config(features);
            boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;

            ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param offset   the index of the first byte to parse
     * @param length   the number of bytes to parse
     * @param clazz    specify the Class to be converted
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, int offset, int length, Class<T> clazz, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text, offset, length)) {
            JSONReader.Context context = reader.context;
            context.config(features);
            boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;

            ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param clazz    specify the Class to be converted
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Class<T> clazz, JSONReader.Context context) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(context, text)) {
            boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;

            ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param clazz    specify the Class to be converted
     * @param format   the specified date format
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Class<T> clazz, String format, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            JSONReader.Context context = reader.context;
            if (format != null && !format.isEmpty()) {
                context.setUtilDateFormat(format);
            }
            context.config(features);

            boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param type     specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Type type, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            reader.context.config(features);
            ObjectReader<T> objectReader = reader.getObjectReader(type);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param type     specify the {@link Type} to be converted
     * @param filter   specify filters to be enabled
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Type type, Filter filter, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            reader.context.config(filter, features);
            ObjectReader<T> objectReader = reader.getObjectReader(type);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse JSON {@link String} into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param type     specify the {@link Type} to be converted
     * @param format   the specified date format
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Type type, String format, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            JSONReader.Context context = reader.context;
            if (format != null && !format.isEmpty()) {
                context.setUtilDateFormat(format);
            }
            context.config(features);

            ObjectReader<T> objectReader = reader.getObjectReader(type);
            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into a Java object
     *
     * @param bytes UTF8 encoded JSON byte array to parse
     * @param type  specify the {@link Type} to be converted
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, Type type) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        JSONReader reader = JSONReader.of(bytes);
        ObjectReader<T> objectReader = reader.getObjectReader(type);
        T object = objectReader.readObject(reader, 0);
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    /**
     * Parse UTF8 encoded JSON byte array into a Java object
     *
     * @param bytes UTF8 encoded JSON byte array to parse
     * @param clazz specify the Class to be converted
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes)) {
            ObjectReader<T> objectReader = reader.getObjectReader(clazz);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into a Java object
     *
     * @param utf8Bytes UTF8 encoded JSON byte array to parse
     * @param clazz    specify the Class to be converted
     * @param filter   specify filter to be enabled
     * @param features features to be enabled in parsing
     * @return Class
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(
            byte[] utf8Bytes,
            Class<T> clazz,
            Filter filter,
            JSONReader.Feature... features) {
        if (utf8Bytes == null) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(utf8Bytes)) {
            JSONReader.Context context = reader.context;
            reader.context.config(filter, features);

            boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into a Java object
     *
     * @param utf8Bytes UTF8 encoded JSON byte array to parse
     * @param clazz    specify the Class to be converted
     * @param context specify the context use by JSONReader
     * @return Class
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(
            byte[] utf8Bytes,
            Class<T> clazz,
            JSONReader.Context context) {
        if (utf8Bytes == null) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(context, utf8Bytes)) {
            boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into a Java object
     *
     * @param utf8Bytes UTF8 encoded JSON byte array to parse
     * @param type     specify the {@link Type} to be converted
     * @param format   the specified date format
     * @param filters   specify filters to be enabled
     * @param features features to be enabled in parsing
     * @return Class
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(
            byte[] utf8Bytes,
            Type type,
            String format,
            Filter[] filters,
            JSONReader.Feature... features) {
        if (utf8Bytes == null) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(utf8Bytes)) {
            JSONReader.Context context = reader.context;
            context.setUtilDateFormat(format);
            context.config(filters, features);

            boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader<T> objectReader = context.provider.getObjectReader(type, fieldBased);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param bytes    UTF8 encoded JSON byte array to parse
     * @param clazz    specify the Class to be converted
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, Class<T> clazz, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes)) {
            reader.context.config(features);
            ObjectReader<T> objectReader = reader.getObjectReader(clazz);
            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param bytes    UTF8 encoded JSON byte array to parse
     * @param type     specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, Type type, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes)) {
            reader.context.config(features);
            ObjectReader<T> objectReader = reader.getObjectReader(type);
            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param bytes    UTF8 encoded JSON byte array to parse
     * @param type     specify the {@link Type} to be converted
     * @param filter   specify filters to be enabled
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, Type type, Filter filter, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes)) {
            reader.context.config(filter, features);
            ObjectReader<T> objectReader = reader.getObjectReader(type);
            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param bytes    UTF8 encoded JSON byte array to parse
     * @param type     specify the {@link Type} to be converted
     * @param format   the specified date format
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, Type type, String format, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes)) {
            JSONReader.Context context = reader.context;
            if (format != null && !format.isEmpty()) {
                context.setUtilDateFormat(format);
            }
            context.config(features);

            ObjectReader<T> objectReader = reader.getObjectReader(type);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse Reader into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param input    the JSON {@link InputStream} to be parsed
     * @param type     specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(Reader input, Type type, JSONReader.Feature... features) {
        if (input == null) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(input)) {
            if (reader.isEnd()) {
                return null;
            }

            reader.context.config(features);
            ObjectReader<T> objectReader = reader.getObjectReader(type);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 inputStream into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param input    the JSON {@link InputStream} to be parsed
     * @param type     specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(InputStream input, Type type, JSONReader.Feature... features) {
        if (input == null) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(input, StandardCharsets.UTF_8)) {
            if (reader.isEnd()) {
                return null;
            }

            reader.context.config(features);
            ObjectReader<T> objectReader = reader.getObjectReader(type);

            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 URL Resource into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param url      the JSON {@link URL} to be parsed
     * @param type     specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     * @throws JSONException if an I/O error occurs. In particular, a {@link JSONException} may be thrown if the output stream has been closed
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
     * Parse UTF8 URL Resource into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param url      the JSON {@link URL} to be parsed
     * @param function specify the {@link Function} to be converted
     * @param features features to be enabled in parsing
     * @throws JSONException if an I/O error occurs. In particular, a {@link JSONException} may be thrown if the output stream has been closed
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
            throw new JSONException("parseObject error", e);
        }
    }

    /**
     * Parse UTF8 inputStream into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param input    the JSON {@link InputStream} to be parsed
     * @param type     specify the {@link Type} to be converted
     * @param format   the specified date format
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(InputStream input, Type type, String format, JSONReader.Feature... features) {
        try (JSONReader reader = JSONReader.of(input, StandardCharsets.UTF_8)) {
            JSONReader.Context context = reader.context;
            if (format != null && !format.isEmpty()) {
                context.setUtilDateFormat(format);
            }
            context.config(features);

            ObjectReader<T> objectReader = reader.getObjectReader(type);
            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse UTF8 inputStream into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param input    the JSON {@link InputStream} to be parsed
     * @param charset  inputStream charset
     * @param type     specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(InputStream input, Charset charset, Type type, JSONReader.Feature... features) {
        try (JSONReader reader = JSONReader.of(input, charset)) {
            reader.context.config(features);
            ObjectReader<T> objectReader = reader.getObjectReader(type);
            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parses the JSON byte array of the specified {@link Charset} into a Java Object
     *
     * @param bytes   JSON byte array to parse
     * @param offset  the index of the first byte to parse
     * @param length  the number of bytes to parse
     * @param charset specify {@link Charset} to parse
     * @param type    specify the {@link Type} to be converted
     * @throws IndexOutOfBoundsException If the offset and the length arguments index characters outside the bounds of the bytes array
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, int offset, int length, Charset charset, Type type) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes, offset, length, charset)) {
            ObjectReader<T> objectReader = reader.getObjectReader(type);
            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parses the JSON byte array of the specified {@link Charset} into a Java Object
     *
     * @param bytes   JSON byte array to parse
     * @param offset  the index of the first byte to parse
     * @param length  the number of bytes to parse
     * @param charset specify {@link Charset} to parse
     * @param type    specify the {@link Class} to be converted
     * @param features features to be enabled in parsing
     * @throws IndexOutOfBoundsException If the offset and the length arguments index characters outside the bounds of the bytes array
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(byte[] bytes, int offset, int length, Charset charset, Class<T> type, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes, offset, length, charset)) {
            reader.context.config(features);
            ObjectReader<T> objectReader = reader.getObjectReader(type);
            T object = objectReader.readObject(reader, 0);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(object);
            }
            return object;
        }
    }

    /**
     * Parse {@link InputStream} into a Java object with specified {@link JSONReader.Feature}s enabled and consume it
     *
     * @param input    the JSON {@link InputStream} to be parsed
     * @param type     specify the {@link Type} to be converted
     * @param consumer the consumer of the parsing result object
     * @param features features to be enabled in parsing
     * @throws JSONException If the first byte cannot be read for any reason other than end of file, or if the input stream has been closed, or if some other I/O error occurs
     * @see JSON#parseObject(InputStream, Charset, char, Type, Consumer, JSONReader.Feature...)
     * @since 2.0.2
     */
    static <T> void parseObject(InputStream input, Type type, Consumer<T> consumer, JSONReader.Feature... features) {
        parseObject(input, StandardCharsets.UTF_8, '\n', type, consumer, features);
    }

    /**
     * Parse {@link InputStream} into a Java object with specified {@link JSONReader.Feature}s enabled and consume it
     *
     * @param input     the JSON {@link InputStream} to be parsed
     * @param charset   specify {@link Charset} to parse
     * @param delimiter specify the delimiter
     * @param type      specify the {@link Type} to be converted
     * @param consumer  the consumer of the parsing result object
     * @param features  features to be enabled in parsing
     * @throws JSONException If the first byte cannot be read for any reason other than end of file, or if the input stream has been closed, or if some other I/O error occurs
     * @since 2.0.2
     */
    @SuppressWarnings("unchecked")
    static <T> void parseObject(InputStream input, Charset charset, char delimiter, Type type, Consumer<T> consumer, JSONReader.Feature... features) {
        int identityHashCode = System.identityHashCode(Thread.currentThread());
        final AtomicReferenceFieldUpdater<JSONFactory.Cache, byte[]> byteUpdater;

        switch (identityHashCode & 3) {
            case 0:
                byteUpdater = JSONFactory.BYTES0_UPDATER;
                break;
            case 1:
                byteUpdater = JSONFactory.BYTES1_UPDATER;
                break;
            case 2:
                byteUpdater = JSONFactory.BYTES2_UPDATER;
                break;
            default:
                byteUpdater = JSONFactory.BYTES3_UPDATER;
                break;
        }

        byte[] bytes = byteUpdater.getAndSet(JSONFactory.CACHE, null);
        if (bytes == null) {
            bytes = new byte[8192];
        }

        int limit = 0, start = 0, end;
        ObjectReader<? extends T> objectReader = null;

        try {
            while (true) {
                int n = input.read(bytes, limit, bytes.length - limit);
                if (n == -1) {
                    break;
                }

                for (int i = 0; i < n; ++i) {
                    int j = limit + i;
                    if (bytes[j] == delimiter) {
                        end = j;

                        JSONReader jsonReader = JSONReader.of(bytes, start, end - start, charset);
                        jsonReader.context.config(features);
                        if (objectReader == null) {
                            objectReader = jsonReader.getObjectReader(type);
                        }

                        T object = objectReader.readObject(jsonReader);
                        if (jsonReader.resolveTasks != null) {
                            jsonReader.handleResolveTasks(object);
                        }

                        consumer.accept(
                                object
                        );
                        start = end + 1;
                    }
                }
                limit += n;

                if (limit == bytes.length) {
                    bytes = Arrays.copyOf(bytes, bytes.length + 8192);
                }
            }
        } catch (IOException e) {
            throw new JSONException("Interruption in reading", e);
        }
    }

    /**
     * Parse {@link Reader} into a Java object with specified {@link JSONReader.Feature}s enabled and consume it
     *
     * @param input     the JSON {@link Reader} to be parsed
     * @param delimiter specify the delimiter
     * @param type      specify the {@link Type} to be converted
     * @param consumer  the consumer of the parsing result object
     * @throws JSONException If the first byte cannot be read for any reason other than end of file, or if the input stream has been closed, or if some other I/O error occurs
     * @since 2.0.2
     */
    @SuppressWarnings("unchecked")
    static <T> void parseObject(Reader input, char delimiter, Type type, Consumer<T> consumer) {
        char[] chars = JSONFactory.CHARS_UPDATER.getAndSet(JSONFactory.CACHE, null);
        if (chars == null) {
            chars = new char[8192];
        }

        int limit = 0, start = 0, end;
        ObjectReader<? extends T> objectReader = null;

        try {
            while (true) {
                int n = input.read(chars, limit, chars.length - limit);
                if (n == -1) {
                    break;
                }

                for (int i = 0; i < n; ++i) {
                    int j = limit + i;
                    if (chars[j] == delimiter) {
                        end = j;

                        JSONReader jsonReader = JSONReader.of(chars, start, end - start);
                        if (objectReader == null) {
                            objectReader = jsonReader.getObjectReader(type);
                        }

                        consumer.accept(
                                objectReader.readObject(jsonReader)
                        );
                        start = end + 1;
                    }
                }
                limit += n;

                if (limit == chars.length) {
                    chars = Arrays.copyOf(chars, chars.length + 8192);
                }
            }
        } catch (IOException e) {
            throw new JSONException("Interruption in reading", e);
        }
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray}
     *
     * @param text the JSON {@link String} to be parsed
     */
    @SuppressWarnings("unchecked")
    static JSONArray parseArray(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray();
            reader.read(array);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            return array;
        }
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray}
     *
     * @param bytes the JSON {@link String} to be parsed
     */
    @SuppressWarnings("unchecked")
    static JSONArray parseArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray();
            reader.read(array);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            return array;
        }
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray}
     *
     * @param text     the JSON {@link String} to be parsed
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static JSONArray parseArray(String text, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            reader.context.config(features);
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray();
            reader.read(array);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            return array;
        }
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray}
     *
     * @param url      the JSON {@link URL} to be parsed
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static JSONArray parseArray(URL url, JSONReader.Feature... features) {
        if (url == null) {
            return null;
        }

        try (InputStream is = url.openStream()) {
            return parseArray(is, features);
        } catch (IOException e) {
            throw new JSONException("parseArray error", e);
        }
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray}
     *
     * @param in       the JSON {@link InputStream} to be parsed
     * @param features features to be enabled in parsing
     */
    static JSONArray parseArray(InputStream in, JSONReader.Feature... features) {
        try (JSONReader reader = JSONReader.of(in, StandardCharsets.UTF_8)) {
            if (reader.nextIfNull()) {
                return null;
            }
            reader.context.config(features);
            JSONArray array = new JSONArray();
            reader.read(array);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            return array;
        }
    }

    /**
     * Parse JSON {@link String} into {@link List}
     *
     * @param text     the JSON {@link String} to be parsed
     * @param type     specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     */
    static <T> List<T> parseArray(String text, Type type, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            reader.context.config(features);
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            return list;
        }
    }

    /**
     * Parse JSON {@link String} into {@link List}
     *
     * @param text     the JSON {@link String} to be parsed
     * @param type     specify the {@link Class} to be converted
     * @param features features to be enabled in parsing
     */
    static <T> List<T> parseArray(String text, Class<T> type, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text)) {
            reader.context.config(features);
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            return list;
        }
    }

    /**
     * Parse JSON {@link String} into {@link List}
     *
     * @param text     the JSON {@link String} to be parsed
     * @param types    specify some {@link Type}s to be converted
     * @param features features to be enabled in parsing
     */
    static <T> List<T> parseArray(String text, Type[] types, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        List<T> array = new ArrayList<>(types.length);

        try (JSONReader reader = JSONReader.of(text)) {
            reader.context.config(features);

            reader.startArray();
            for (Type itemType : types) {
                array.add(
                        reader.read(itemType)
                );
            }
            reader.endArray();
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(array);
            }
            return array;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into {@link List} with specified {@link JSONReader.Feature}s enabled
     *
     * @param bytes    UTF8 encoded JSON byte array to parse
     * @param type     specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     */
    static <T> List<T> parseArray(byte[] bytes, Type type, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes)) {
            reader.context.config(features);
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            return list;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into {@link List} with specified {@link JSONReader.Feature}s enabled
     *
     * @param bytes    UTF8 encoded JSON byte array to parse
     * @param type     specify the {@link Class} to be converted
     * @param features features to be enabled in parsing
     */
    static <T> List<T> parseArray(byte[] bytes, Class<T> type, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes)) {
            reader.context.config(features);
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            return list;
        }
    }

    /**
     * Parse UTF8 encoded JSON byte array into {@link List} with specified {@link JSONReader.Feature}s enabled
     *
     * @param bytes    UTF8 encoded JSON byte array to parse
     * @param offset  the index of the first byte to validate
     * @param length  the number of bytes to validate
     * @param charset specify {@link Charset} to validate
     * @param type     specify the {@link Class} to be converted
     * @param features features to be enabled in parsing
     */
    static <T> List<T> parseArray(byte[] bytes, int offset, int length, Charset charset, Class<T> type, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(bytes, offset, length, charset)) {
            reader.context.config(features);
            List<T> list = reader.readArray(type);
            if (reader.resolveTasks != null) {
                reader.handleResolveTasks(list);
            }
            return list;
        }
    }

    /**
     * Serialize Java Object to JSON {@link String}
     *
     * @param object Java Object to be serialized into JSON {@link String}
     */
    static String toJSONString(Object object) {
        try (JSONWriter writer = JSONWriter.of()) {
            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        } catch (NullPointerException | NumberFormatException ex) {
            throw new JSONException("toJSONString error", ex);
        }
    }

    /**
     * Serialize Java Object to JSON {@link String} with specified {@link JSONReader.Feature}s enabled
     *
     * @param object   Java Object to be serialized into JSON {@link String}
     * @param features features to be enabled in serialization
     */
    static String toJSONString(Object object, JSONWriter.Feature... features) {
        JSONWriter.Context writeContext = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);

        boolean pretty = (writeContext.features & JSONWriter.Feature.PrettyFormat.mask) != 0;
        JSONWriterUTF16 jsonWriter = JDKUtils.JVM_VERSION == 8 ? new JSONWriterUTF16JDK8(writeContext) : new JSONWriterUTF16(writeContext);

        try (JSONWriter writer = pretty ?
                new JSONWriterPretty(jsonWriter) : jsonWriter) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);
                Class<?> valueClass = object.getClass();

                boolean fieldBased = (writeContext.features & JSONWriter.Feature.FieldBased.mask) != 0;
                ObjectWriter<?> objectWriter = writeContext.provider.getObjectWriter(valueClass, valueClass, fieldBased);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        }
    }

    /**
     * Serialize Java Object to JSON {@link String} with specified {@link JSONReader.Feature}s enabled
     *
     * @param object   Java Object to be serialized into JSON {@link String}
     * @param filter   specify a filter to use in serialization
     * @param features features to be enabled in serialization
     */
    static String toJSONString(Object object, Filter filter, JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.of(features)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);
                if (filter != null) {
                    writer.context.configFilter(filter);
                }

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        }
    }

    /**
     * Serialize Java Object to JSON {@link String} with specified {@link JSONReader.Feature}s enabled
     *
     * @param object   Java Object to be serialized into JSON {@link String}
     * @param filters  specifies the filter to use in serialization
     * @param features features to be enabled in serialization
     */
    static String toJSONString(Object object, Filter[] filters, JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.of(features)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);
                if (filters != null && filters.length != 0) {
                    writer.context.configFilter(filters);
                }

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        }
    }

    /**
     * Serialize Java Object to JSON {@link String} with specified {@link JSONReader.Feature}s enabled
     *
     * @param object   Java Object to be serialized into JSON {@link String}
     * @param format   the specified date format
     * @param features features to be enabled in serialization
     */
    static String toJSONString(Object object, String format, JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.of(features)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);
                if (format != null && !format.isEmpty()) {
                    writer.context.setDateFormat(format);
                }

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        }
    }

    /**
     * Serialize Java Object to JSON {@link String} with specified {@link JSONReader.Feature}s enabled
     *
     * @param object   Java Object to be serialized into JSON {@link String}
     * @param format   the specified date format
     * @param filters  specifies the filter to use in serialization
     * @param features features to be enabled in serialization
     */
    static String toJSONString(Object object, String format, Filter[] filters, JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.of(features)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);
                if (format != null && !format.isEmpty()) {
                    writer.context.setDateFormat(format);
                }
                if (filters != null && filters.length != 0) {
                    writer.context.configFilter(filters);
                }

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        }
    }

    /**
     * Serialize Java Object to JSON byte array
     *
     * @param object Java Object to be serialized into JSON byte array
     */
    static byte[] toJSONBytes(Object object) {
        try (JSONWriter writer = JSONWriter.ofUTF8()) {
            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    /**
     * Serialize Java Object to JSON byte array
     *
     * @param object  Java Object to be serialized into JSON byte array
     * @param filters specifies the filter to use in serialization
     */
    static byte[] toJSONBytes(Object object, Filter... filters) {
        try (JSONWriter writer = JSONWriter.ofUTF8()) {
            if (filters != null && filters.length != 0) {
                writer.context.configFilter(filters);
            }

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    /**
     * Serialize Java Object to JSON byte array with specified {@link JSONReader.Feature}s enabled
     *
     * @param object   Java Object to be serialized into JSON byte array
     * @param features features to be enabled in serialization
     */
    static byte[] toJSONBytes(Object object, JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.ofUTF8(features)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    /**
     * Serialize Java Object to JSON byte array with specified {@link JSONReader.Feature}s enabled
     *
     * @param object   Java Object to be serialized into JSON byte array
     * @param filters  specifies the filter to use in serialization
     * @param features features to be enabled in serialization
     */
    static byte[] toJSONBytes(Object object, Filter[] filters, JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.ofUTF8(features)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);
                if (filters != null && filters.length != 0) {
                    writer.context.configFilter(filters);
                }

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    /**
     * Serialize Java Object to JSON byte array with specified {@link JSONReader.Feature}s enabled
     *
     * @param object   Java Object to be serialized into JSON byte array
     * @param format   the specified date format
     * @param filters  specifies the filter to use in serialization
     * @param features features to be enabled in serialization
     */
    static byte[] toJSONBytes(Object object, String format, Filter[] filters, JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.ofUTF8(features)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);
                if (format != null && !format.isEmpty()) {
                    writer.context.setDateFormat(format);
                }
                if (filters != null && filters.length != 0) {
                    writer.context.configFilter(filters);
                }

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    /**
     * Serialize Java Object to JSON and write to {@link OutputStream} with specified {@link JSONReader.Feature}s enabled
     *
     * @param out      {@link OutputStream} to be written
     * @param object   Java Object to be serialized into JSON
     * @param features features to be enabled in serialization
     * @throws JSONException if an I/O error occurs. In particular, a {@link JSONException} may be thrown if the output stream has been closed
     */
    static int writeTo(OutputStream out, Object object, JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.ofUTF8(features)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.flushTo(out);
        } catch (Exception e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    /**
     * Serialize Java Object to JSON and write to {@link OutputStream} with specified {@link JSONReader.Feature}s enabled
     *
     * @param out      {@link OutputStream} to be written
     * @param object   Java Object to be serialized into JSON
     * @param filters  specifies the filter to use in serialization
     * @param features features to be enabled in serialization
     * @throws JSONException if an I/O error occurs. In particular, a {@link JSONException} may be thrown if the output stream has been closed
     */
    static int writeTo(OutputStream out, Object object, Filter[] filters, JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.ofUTF8(features)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);
                if (filters != null && filters.length != 0) {
                    writer.context.configFilter(filters);
                }

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.flushTo(out);
        } catch (Exception e) {
            throw new JSONException("FASTJSON-" + JSON.VERSION + " write JSON error" + e.getMessage(), e);
        }
    }

    /**
     * Serialize Java Object to JSON and write to {@link OutputStream} with specified {@link JSONReader.Feature}s enabled
     *
     * @param out      {@link OutputStream} to be written
     * @param object   Java Object to be serialized into JSON
     * @param format   the specified date format
     * @param filters  specifies the filter to use in serialization
     * @param features features to be enabled in serialization
     * @throws JSONException if an I/O error occurs. In particular, a {@link JSONException} may be thrown if the output stream has been closed
     */
    static int writeTo(OutputStream out, Object object, String format, Filter[] filters, JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.ofUTF8(features)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);
                if (format != null && !format.isEmpty()) {
                    writer.context.setDateFormat(format);
                }
                if (filters != null && filters.length != 0) {
                    writer.context.configFilter(filters);
                }

                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.flushTo(out);
        } catch (Exception e) {
            throw new JSONException("FASTJSON-" + JSON.VERSION + " write JSON error" + e.getMessage(), e);
        }
    }

    /**
     * Verify the {@link String} is JSON Object
     *
     * @param text the {@link String} to validate
     * @return {@code true} or {@code false}
     */
    static boolean isValid(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        try (JSONReader jsonReader = JSONReader.of(text)) {
            jsonReader.skipValue();
            return jsonReader.isEnd();
        } catch (JSONException error) {
            return false;
        }
    }

    /**
     * Verify the {@link String} is JSON Object
     *
     * @param text the {@link String} to validate
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
     * Verify the byte array is JSON Object
     *
     * @param bytes the byte array to validate
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
     * Verify the byte array is JSON Object
     *
     * @param bytes the byte array to validate
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
     * Verify the byte array is JSON Array
     *
     * @param bytes the byte array to validate
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
     * Verify the byte array is JSON Object
     *
     * @param bytes   the byte array to validate
     * @param offset  the index of the first byte to validate
     * @param length  the number of bytes to validate
     * @param charset specify {@link Charset} to validate
     * @return {@code true} or {@code false}
     */
    static boolean isValid(byte[] bytes, int offset, int length, Charset charset) {
        if (bytes == null || bytes.length == 0) {
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
     * Convert Java object order to {@link JSONArray} or {@link JSONObject}
     *
     * @param object Java Object to be converted
     * @return Java Object
     */
    static Object toJSON(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof JSONObject || object instanceof JSONArray) {
            return object;
        }

        String str = toJSONString(object);
        return parse(str);
    }

    /**
     * Convert the Object to the target type
     *
     * @param clazz  converted goal class
     * @param object Java Object to be converted
     * @since 2.0.4
     */
    static <T> T to(Class<T> clazz, Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof JSONObject) {
            return ((JSONObject) object).to(clazz);
        }

        return TypeUtils.cast(object, clazz);
    }

    /**
     * Convert the Object to the target type
     *
     * @param object Java Object to be converted
     * @param clazz  converted goal class
     * @deprecated since 2.0.4, please use {@link #to(Class, Object)}
     */
    @Deprecated
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
    static ObjectReader register(Type type, ObjectReader<?> objectReader) {
        return JSONFactory.getDefaultObjectReaderProvider().register(type, objectReader);
    }

    /**
     * Register if absent an {@link ObjectReader} for {@link Type} in default {@link com.alibaba.fastjson2.reader.ObjectReaderProvider}
     *
     * @see JSONFactory#getDefaultObjectReaderProvider()
     * @see com.alibaba.fastjson2.reader.ObjectReaderProvider#register(Type, ObjectReader)
     * @since 2.0.6
     */
    static ObjectReader registerIfAbsent(Type type, ObjectReader<?> objectReader) {
        return JSONFactory.getDefaultObjectReaderProvider().registerIfAbsent(type, objectReader);
    }

    /**
     * Register an {@link ObjectReaderModule} in default {@link com.alibaba.fastjson2.reader.ObjectReaderProvider}
     *
     * @see JSONFactory#getDefaultObjectReaderProvider()
     * @see com.alibaba.fastjson2.reader.ObjectReaderProvider#register(ObjectReaderModule)
     */
    static boolean register(ObjectReaderModule objectReaderModule) {
        return JSONFactory.getDefaultObjectReaderProvider().register(objectReaderModule);
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
    static ObjectWriter register(Type type, ObjectWriter<?> objectWriter) {
        return JSONFactory.getDefaultObjectWriterProvider().register(type, objectWriter);
    }

    /**
     * Register if absent an {@link ObjectWriter} for {@link Type} in default {@link  com.alibaba.fastjson2.writer.ObjectWriterProvider}
     *
     * @see JSONFactory#getDefaultObjectWriterProvider()
     * @see com.alibaba.fastjson2.writer.ObjectWriterProvider#register(Type, ObjectWriter)
     * @since 2.0.6
     */
    static ObjectWriter registerIfAbsent(Type type, ObjectWriter<?> objectWriter) {
        return JSONFactory.getDefaultObjectWriterProvider().registerIfAbsent(type, objectWriter);
    }

    static void config(JSONReader.Feature... features) {
        for (JSONReader.Feature feature : features) {
            JSONFactory.defaultReaderFeatures |= feature.mask;
        }
    }

    static void config(JSONReader.Feature feature, boolean state) {
        if (state) {
            JSONFactory.defaultReaderFeatures |= feature.mask;
        } else {
            JSONFactory.defaultReaderFeatures &= ~feature.mask;
        }
    }

    static boolean isEnabled(JSONReader.Feature feature) {
        return (JSONFactory.defaultReaderFeatures & feature.mask) != 0;
    }

    static void config(JSONWriter.Feature... features) {
        for (JSONWriter.Feature feature : features) {
            JSONFactory.defaultWriterFeatures |= feature.mask;
        }
    }

    static void config(JSONWriter.Feature feature, boolean state) {
        if (state) {
            JSONFactory.defaultWriterFeatures |= feature.mask;
        } else {
            JSONFactory.defaultWriterFeatures &= ~feature.mask;
        }
    }

    static boolean isEnabled(JSONWriter.Feature feature) {
        return (JSONFactory.defaultWriterFeatures & feature.mask) != 0;
    }
}
