package com.alibaba.fastjson2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import static com.alibaba.fastjson2.JSONReader.EOI;
import static com.alibaba.fastjson2.JSONReader.Feature.IgnoreCheckClose;

/**
 * This is the main entry point for fastjson2 JSON tree API.
 * It provides a set of static methods for JSON processing:
 * parsing JSON to {@link JSONObject} / {@link JSONArray} and serializing them back to JSON text.
 *
 * <p>Example usage:
 * <pre>
 * // 1. Parse JSON string to JSONObject
 * JSONObject jsonObject = JSON.parseObject("{\"id\":1,\"name\":\"John\"}");
 *
 * // 2. Parse JSON string to JSONArray
 * JSONArray jsonArray = JSON.parseArray("[1,2,3]");
 *
 * // 3. Serialize to JSON string
 * String jsonString = JSON.toJSONString(jsonObject, JSONWriter.Feature.PrettyFormat);
 * </pre>
 */
public final class JSON {
    public static final String VERSION = "2.0.63-slim";

    private JSON() {
    }

    /**
     * Parses the json string as a {@link JSONArray} or {@link JSONObject}.
     * Returns {@code null} if received {@link String} is {@code null} or empty or length is 0.
     *
     * @param text the specified text to be parsed
     * @return either {@link JSONArray} or {@link JSONObject} or null
     * @throws JSONException If a parsing error occurs
     */
    public static Object parse(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(text, context)) {
            Object object;
            char ch = reader.current();

            if (ch == '{') {
                object = reader.readObject();
            } else if (ch == '[') {
                object = reader.readArray();
            } else if (ch == '"' || ch == '\'') {
                object = reader.readString();
            } else if (ch == 't' || ch == 'f') {
                object = reader.readBoolValue();
            } else if (ch == 'n') {
                reader.readNull();
                object = null;
            } else {
                object = reader.readNumber();
            }

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses the json string as a {@link JSONArray} or {@link JSONObject}.
     *
     * @param text the specified text to be parsed
     * @param features the specified features is applied to parsing
     * @return either {@link JSONArray} or {@link JSONObject} or null
     * @throws JSONException If a parsing error occurs
     */
    public static Object parse(String text, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(text, context)) {
            Object object;
            char ch = reader.current();

            if (ch == '{') {
                object = reader.readObject();
            } else if (ch == '[') {
                object = reader.readArray();
            } else if (ch == '"' || ch == '\'') {
                object = reader.readString();
            } else if (ch == 't' || ch == 'f') {
                object = reader.readBoolValue();
            } else if (ch == 'n') {
                reader.readNull();
                object = null;
            } else {
                object = reader.readNumber();
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
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONObject parseObject(String text) {
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

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses the json string as a {@link JSONObject}.
     *
     * @param text the specified string to be parsed
     * @param features the specified features is applied to parsing
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONObject parseObject(String text, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(text, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0L);

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses the json string as a {@link JSONObject}.
     *
     * @param text the specified string to be parsed
     * @param offset the starting index of string
     * @param length the specified length of string
     * @param features the specified features is applied to parsing
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONObject parseObject(String text, int offset, int length, JSONReader.Feature... features) {
        if (text == null || text.isEmpty() || length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(text, offset, length, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0L);

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses the json string as a {@link JSONObject}.
     *
     * @param text the specified string to be parsed
     * @param context the specified custom context
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONObject parseObject(String text, JSONReader.Context context) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try (JSONReader reader = JSONReader.of(text, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0L);

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses the json string as a {@link JSONObject}.
     *
     * @param text the specified text to be parsed
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONObject parseObject(byte[] text) {
        if (text == null || text.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(text, 0, text.length, StandardCharsets.UTF_8, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0L);

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses the json string as a {@link JSONObject}.
     *
     * @param text the specified text to be parsed
     * @param features the specified features is applied to parsing
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONObject parseObject(byte[] text, JSONReader.Feature... features) {
        if (text == null || text.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(text, 0, text.length, StandardCharsets.UTF_8, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0L);

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses the json text as a {@link JSONObject}.
     *
     * @param text the specified text to be parsed
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONObject parseObject(char[] text) {
        if (text == null || text.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(text, 0, text.length, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0L);

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses the json text from a Reader as a {@link JSONObject}.
     *
     * @param input the specified text to be parsed
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONObject parseObject(Reader input) {
        if (input == null) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(input, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0L);

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses the json text from an InputStream as a {@link JSONObject}.
     *
     * @param input the specified text to be parsed
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONObject parseObject(InputStream input) {
        if (input == null) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(input, StandardCharsets.UTF_8, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0L);

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses the json text from an InputStream as a {@link JSONObject}.
     *
     * @param input the specified text to be parsed
     * @param charset the charset of input stream
     * @return {@link JSONObject} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONObject parseObject(InputStream input, Charset charset) {
        if (input == null) {
            return null;
        }
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(input, charset, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONObject object = new JSONObject();
            reader.read(object, 0L);

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    /**
     * Parses the json string as a {@link JSONArray}. Returns {@code null}
     * if received {@link String} is {@code null} or empty or its content is null.
     *
     * @param text the specified string to be parsed
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONArray parseArray(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(text, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray(reader.readArray());

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return array;
        }
    }

    /**
     * Parses the json string as a {@link JSONArray}.
     *
     * @param text the specified string to be parsed
     * @param features the specified features is applied to parsing
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONArray parseArray(String text, JSONReader.Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(text, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray(reader.readArray());

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
    public static JSONArray parseArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.UTF_8, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray(reader.readArray());

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return array;
        }
    }

    /**
     * Parses the json byte array as a {@link JSONArray}.
     *
     * @param bytes the specified UTF8 text to be parsed
     * @param features the specified features is applied to parsing
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONArray parseArray(byte[] bytes, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext(features);
        try (JSONReader reader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.UTF_8, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray(reader.readArray());

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return array;
        }
    }

    /**
     * Parses the json char array as a {@link JSONArray}.
     *
     * @param text the specified UTF8 text to be parsed
     * @return {@link JSONArray} or {@code null}
     * @throws JSONException If a parsing error occurs
     */
    public static JSONArray parseArray(char[] text) {
        if (text == null || text.length == 0) {
            return null;
        }

        final JSONReader.Context context = JSONFactory.createReadContext();
        try (JSONReader reader = JSONReader.of(text, 0, text.length, context)) {
            if (reader.nextIfNull()) {
                return null;
            }
            JSONArray array = new JSONArray(reader.readArray());

            if (reader.ch != EOI && (context.features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return array;
        }
    }

    /**
     * Serializes the specified object to the json string.
     *
     * @param object the specified object will be serialized
     * @return {@link String} that is not null
     * @throws JSONException If a serialization error occurs
     */
    public static String toJSONString(Object object) {
        final JSONWriter.Context context = JSONFactory.createWriteContext();
        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;
                writer.writeAny(object);
            }
            return writer.toString();
        } catch (JSONException e) {
            throw e;
        } catch (Exception e) {
            throw new JSONException("JSON#toJSONString cannot serialize '" + object + "'", e);
        }
    }

    /**
     * Serializes the specified object to the json string.
     *
     * @param object the specified object will be serialized
     * @param features the specified features is applied to serialization
     * @return {@link String} that is not null
     * @throws JSONException If a serialization error occurs
     */
    public static String toJSONString(Object object, JSONWriter.Feature... features) {
        final JSONWriter.Context context = JSONFactory.createWriteContext(features);
        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;
                writer.writeAny(object);
            }
            return writer.toString();
        } catch (JSONException e) {
            throw e;
        } catch (Exception e) {
            throw new JSONException("JSON#toJSONString cannot serialize '" + object + "'", e);
        }
    }

    /**
     * Serializes the specified object to the json string.
     *
     * @param object the specified object will be serialized
     * @param context the specified custom context
     * @return {@link String} that is not null
     * @throws JSONException If a serialization error occurs
     */
    public static String toJSONString(Object object, JSONWriter.Context context) {
        if (context == null) {
            context = JSONFactory.createWriteContext();
        }
        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;
                writer.writeAny(object);
            }
            return writer.toString();
        } catch (JSONException e) {
            throw e;
        } catch (Exception e) {
            throw new JSONException("JSON#toJSONString cannot serialize '" + object + "'", e);
        }
    }

    /**
     * Serializes the specified object to the json bytes.
     *
     * @param object the specified object will be serialized
     * @return byte[] that is not null
     * @throws JSONException If a serialization error occurs
     */
    public static byte[] toJSONBytes(Object object) {
        final JSONWriter.Context context = JSONFactory.createWriteContext();
        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;
                writer.writeAny(object);
            }
            return writer.getBytes();
        } catch (JSONException e) {
            throw e;
        } catch (Exception e) {
            throw new JSONException("JSON#toJSONBytes cannot serialize '" + object + "'", e);
        }
    }

    /**
     * Serializes the specified object to the json bytes.
     *
     * @param object the specified object will be serialized
     * @param features the specified features is applied to serialization
     * @return byte[] that is not null
     * @throws JSONException If a serialization error occurs
     */
    public static byte[] toJSONBytes(Object object, JSONWriter.Feature... features) {
        final JSONWriter.Context context = JSONFactory.createWriteContext(features);
        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;
                writer.writeAny(object);
            }
            return writer.getBytes();
        } catch (JSONException e) {
            throw e;
        } catch (Exception e) {
            throw new JSONException("JSON#toJSONBytes cannot serialize '" + object + "'", e);
        }
    }

    /**
     * Writes the specified object as json to the specified Writer.
     *
     * @param object the specified object will be serialized
     * @param to the specified Writer
     * @return the number of characters written
     * @throws JSONException If a serialization error occurs
     */
    public static int writeTo(Object object, java.io.Writer to) {
        final JSONWriter.Context context = JSONFactory.createWriteContext();
        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;
                writer.writeAny(object);
            }
            writer.flushTo(to);
            return 0;
        }
    }

    /**
     * Writes the specified object as json to the specified OutputStream.
     *
     * @param object the specified object will be serialized
     * @param to the specified OutputStream
     * @return the number of bytes written
     * @throws JSONException If a serialization error occurs
     */
    public static int writeTo(Object object, OutputStream to) {
        final JSONWriter.Context context = JSONFactory.createWriteContext();
        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.rootObject = object;
                writer.path = JSONWriter.Path.ROOT;
                writer.writeAny(object);
            }
            try {
                writer.flushTo(to);
            } catch (IOException e) {
                throw new JSONException(e.getMessage(), e);
            }
            return 0;
        }
    }

    /**
     * Converts the specified object to a {@link JSONArray} or {@link JSONObject}.
     * Returns {@code null} if received object is {@code null}.
     *
     * @param object the specified object to be converted
     * @return {@link JSONArray} or {@link JSONObject} or {@code null}
     */
    public static Object toJSON(Object object) {
        return toJSON(object, (JSONWriter.Feature[]) null);
    }

    /**
     * Converts the specified object to a {@link JSONArray} or {@link JSONObject}.
     * Returns {@code null} if received object is {@code null}.
     *
     * @param object the specified object to be converted
     * @param features the specified features is applied to serialization
     * @return {@link JSONArray} or {@link JSONObject} or {@code null}
     */
    public static Object toJSON(Object object, JSONWriter.Feature... features) {
        if (object == null) {
            return null;
        }
        if (object instanceof JSONObject || object instanceof JSONArray) {
            return object;
        }
        if (object instanceof Map) {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                jsonObject.put(
                        entry.getKey().toString(),
                        toJSON(entry.getValue(), features)
                );
            }
            return jsonObject;
        }
        if (object instanceof Collection) {
            JSONArray array = new JSONArray();
            for (Object item : (Collection) object) {
                array.add(toJSON(item, features));
            }
            return array;
        }
        if (object instanceof Object[]) {
            JSONArray array = new JSONArray();
            for (Object item : (Object[]) object) {
                array.add(toJSON(item, features));
            }
            return array;
        }
        return object;
    }

    /**
     * Checks whether the specified text is a valid JSON.
     *
     * @param text the specified text to be checked
     * @return true if the text is a valid JSON, false otherwise
     */
    public static boolean isValid(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        try {
            JSONReader reader = JSONReader.of(text);
            char ch = reader.current();
            if (ch == '{') {
                reader.readObject();
            } else if (ch == '[') {
                reader.readArray();
            } else if (ch == '"' || ch == '\'') {
                reader.readString();
            } else if (ch == 't' || ch == 'f') {
                reader.readBoolValue();
            } else if (ch == 'n') {
                reader.readNull();
            } else {
                reader.readNumber();
            }
            reader.close();
            return reader.ch == EOI;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether the specified text is a valid JSON object.
     *
     * @param text the specified text to be checked
     * @return true if the text is a valid JSON object, false otherwise
     */
    public static boolean isValidObject(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        try {
            JSONReader reader = JSONReader.of(text);
            reader.readObject();
            reader.close();
            return reader.ch == EOI;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether the specified text is a valid JSON array.
     *
     * @param text the specified text to be checked
     * @return true if the text is a valid JSON array, false otherwise
     */
    public static boolean isValidArray(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        try {
            JSONReader reader = JSONReader.of(text);
            reader.readArray();
            reader.close();
            return reader.ch == EOI;
        } catch (Exception e) {
            return false;
        }
    }
}
