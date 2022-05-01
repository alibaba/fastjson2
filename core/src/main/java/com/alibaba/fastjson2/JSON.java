package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;

public interface JSON {
    /**
     * FASTJSON2 version name
     */
    String VERSION = "2.0.2";

    /**
     * Parse JSON {@link String} into {@link JSONArray} or {@link JSONObject}
     *
     * @param text the JSON {@link String} to be parsed
     * @return Object
     */
    static Object parse(String text) {
        if (text == null) {
            return null;
        }
        JSONReader reader = JSONReader.of(text);
        ObjectReader<?> objectReader = reader.getObjectReader(Object.class);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray} or {@link JSONObject} with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param features features to be enabled in parsing
     * @return Object
     */
    static Object parse(String text, JSONReader.Feature... features) {
        if (text == null) {
            return null;
        }
        JSONReader reader = JSONReader.of(text);
        reader.context.config(features);
        ObjectReader<?> objectReader = reader.getObjectReader(Object.class);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into {@link JSONObject}
     *
     * @param text the JSON {@link String} to be parsed
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    static JSONObject parseObject(String text) {
        if (text == null) {
            return null;
        }
        JSONReader reader = JSONReader.of(text);
        ObjectReader<JSONObject> objectReader = reader.getObjectReader(JSONObject.class);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into {@link JSONObject}
     *
     * @param text the JSON {@link String} to be parsed
     * @param features features to be enabled in parsing
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    static JSONObject parseObject(String text, JSONReader.Feature... features) {
        if (text == null) {
            return null;
        }
        JSONReader reader = JSONReader.of(text);
        reader.context.config(features);
        ObjectReader<JSONObject> objectReader = reader.getObjectReader(JSONObject.class);
        return objectReader.readObject(reader, 0);
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
        JSONReader reader = JSONReader.of(bytes);
        ObjectReader<JSONObject> objectReader = reader.getObjectReader(JSONObject.class);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse UTF8 encoded JSON byte array into {@link JSONObject}
     *
     * @param bytes UTF8 encoded JSON byte array to parse
     * @param features features to be enabled in parsing
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    static JSONObject parseObject(byte[] bytes, JSONReader.Feature... features) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        JSONReader reader = JSONReader.of(bytes);
        reader.context.config(features);
        ObjectReader<JSONObject> objectReader = reader.getObjectReader(JSONObject.class);
        return objectReader.readObject(reader, 0);
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
        if (text == null) {
            return null;
        }
        JSONReader reader = JSONReader.of(text);
        JSONReader.Context context = reader.context;

        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into Java Object
     *
     * @param text the JSON {@link String} to be parsed
     * @param type specify the {@link Type} to be converted
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(String text, Type type) {
        if (text == null || text.length() == 0) {
            return null;
        }
        JSONReader reader = JSONReader.of(text);
        ObjectReader<T> objectReader = reader.context.provider.getObjectReader(type);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into Java Object
     *
     * @param text          the JSON {@link String} to be parsed
     * @param typeReference specify the {@link TypeReference} to be converted
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T> T parseObject(String text, TypeReference typeReference) {
        if (text == null || text.length() == 0) {
            return null;
        }
        JSONReader reader = JSONReader.of(text);
        Type type = typeReference.getType();
        ObjectReader<T> objectReader = reader.context.provider.getObjectReader(type);
        return objectReader.readObject(reader, 0);
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
        if (text == null || text.length() == 0) {
            return null;
        }
        JSONReader reader = JSONReader.of(text);

        JSONReader.Context context = reader.context;
        context.config(features);
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;

        ObjectReader<T> objectReader = context.provider.getObjectReader(clazz, fieldBased);
        return objectReader.readObject(reader, 0);
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
        if (text == null || text.length() == 0) {
            return null;
        }
        JSONReader reader = JSONReader.of(text);

        JSONReader.Context context = reader.context;
        if (format != null) {
            context.setUtilDateFormat(format);
        }
        context.config(features);

        ObjectReader<T> objectReader = context.provider.getObjectReader(clazz);
        return objectReader.readObject(reader, 0);
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
        if (text == null || text.length() == 0) {
            return null;
        }
        JSONReader reader = JSONReader.of(text);
        reader.context.config(features);
        ObjectReader<T> objectReader = reader.getObjectReader(type);
        return objectReader.readObject(reader, 0);
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
        return objectReader.readObject(reader, 0);
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
        JSONReader reader = JSONReader.of(bytes);
        ObjectReader<T> objectReader = reader.getObjectReader(clazz);
        return objectReader.readObject(reader, 0);
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
        JSONReader reader = JSONReader.of(bytes);
        reader.getContext().config(features);
        ObjectReader<T> objectReader = reader.getObjectReader(type);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse UTF8 inputStream into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param input
     * @param type     specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(InputStream input, Type type, JSONReader.Feature... features) {
        JSONReader reader = JSONReader.of(input, StandardCharsets.UTF_8);
        reader.getContext().config(features);
        ObjectReader<T> objectReader = reader.getObjectReader(type);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse UTF8 inputStream into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param input
     * @param charset  inputStream charset
     * @param type     specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static <T> T parseObject(InputStream input, Charset charset, Type type, JSONReader.Feature... features) {
        JSONReader reader = JSONReader.of(input, charset);
        reader.getContext().config(features);
        ObjectReader<T> objectReader = reader.getObjectReader(type);
        return objectReader.readObject(reader, 0);
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
        JSONReader reader = JSONReader.of(bytes, offset, length, charset);
        ObjectReader<T> objectReader = reader.getObjectReader(type);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray}
     *
     * @param text the JSON {@link String} to be parsed
     */
    @SuppressWarnings("unchecked")
    static JSONArray parseArray(String text) {
        if (text == null || text.length() == 0) {
            return null;
        }
        JSONReader reader = JSONReader.of(text);
        ObjectReader<JSONArray> objectReader = reader.getObjectReader(JSONArray.class);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray}
     *
     * @param text the JSON {@link String} to be parsed
     * @param features features to be enabled in parsing
     */
    @SuppressWarnings("unchecked")
    static JSONArray parseArray(String text, JSONReader.Feature... features) {
        if (text == null || text.length() == 0) {
            return null;
        }
        JSONReader reader = JSONReader.of(text);
        reader.context.config(features);
        ObjectReader<JSONArray> objectReader = reader.getObjectReader(JSONArray.class);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into {@link List}
     *
     * @param text the JSON {@link String} to be parsed
     * @param type specify the {@link Type} to be converted
     */
    static <T> List<T> parseArray(String text, Type type) {
        if (text == null || text.length() == 0) {
            return null;
        }
        ParameterizedTypeImpl paramType = new ParameterizedTypeImpl(new Type[]{type}, null, List.class);
        JSONReader reader = JSONReader.of(text);
        return reader.read(paramType);
    }

    /**
     * Parse JSON {@link String} into {@link List}
     *
     * @param text  the JSON {@link String} to be parsed
     * @param types specify some {@link Type}s to be converted
     */
    static <T> List<T> parseArray(String text, Type[] types) {
        if (text == null || text.length() == 0) {
            return null;
        }
        List<T> array = new ArrayList<>(types.length);
        JSONReader reader = JSONReader.of(text);

        reader.startArray();
        for (Type itemType : types) {
            array.add(
                    reader.read(itemType)
            );
        }
        reader.endArray();

        return array;
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
     * @param format   the specified date format
     * @param features features to be enabled in serialization
     */
    static String toJSONString(Object object, String format, JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.of(features)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);
                writer.context.setDateFormat(format);

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
        } catch (IOException e) {
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
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    /**
     * Verify the {@link String} is JSON Object
     *
     * @param text the {@link String} to validate
     * @return T/F
     */
    static boolean isValid(String text) {
        if (text == null || text.length() == 0) {
            return false;
        }

        JSONReader jsonReader = JSONReader.of(text);
        try {
            jsonReader.skipValue();
        } catch (JSONException error) {
            return false;
        }
        return true;
    }

    /**
     * Verify the {@link String} is JSON Array
     *
     * @param text the {@link String} to validate
     * @return T/F
     */
    static boolean isValidArray(String text) {
        if (text == null || text.length() == 0) {
            return false;
        }

        JSONReader jsonReader = JSONReader.of(text);
        try {
            if (!jsonReader.isArray()) {
                return false;
            }
            jsonReader.skipValue();
        } catch (JSONException error) {
            return false;
        }
        return true;
    }

    /**
     * Verify the byte array is JSON Object
     *
     * @param bytes the byte array to validate
     * @return T/F
     */
    static boolean isValid(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        JSONReader jsonReader = JSONReader.of(bytes);
        try {
            jsonReader.skipValue();
        } catch (JSONException error) {
            return false;
        }
        return true;
    }

    /**
     * Verify the byte array is JSON Array
     *
     * @param bytes the byte array to validate
     * @return T/F
     */
    static boolean isValidArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        JSONReader jsonReader = JSONReader.of(bytes);
        try {
            if (!jsonReader.isArray()) {
                return false;
            }
            jsonReader.skipValue();
        } catch (JSONException error) {
            return false;
        }
        return true;
    }

    /**
     * Verify the byte array is JSON Object
     *
     * @param bytes   the byte array to validate
     * @param offset  the index of the first byte to validate
     * @param length  the number of bytes to validate
     * @param charset specify {@link Charset} to validate
     * @return T/F
     */
    static boolean isValid(byte[] bytes, int offset, int length, Charset charset) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        JSONReader jsonReader = JSONReader.of(bytes, offset, length, charset);
        try {
            jsonReader.skipValue();
        } catch (JSONException error) {
            return false;
        }
        return true;
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

        String str = JSON.toJSONString(object);
        return JSON.parse(str);
    }

    /**
     * Convert the Object to the target type
     *
     * @param object Java Object to be converted
     * @param clazz  converted goal class
     */
    static <T> T toJavaObject(Object object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        if (object instanceof JSONObject) {
            return ((JSONObject) object).toJavaObject(clazz);
        }

        return TypeUtils.cast(object, clazz);
    }

    static void mixIn(Class target, Class mixinSource) {
        JSONFactory.defaultObjectWriterProvider.mixIn(target, mixinSource);
        JSONFactory.getDefaultObjectReaderProvider().mixIn(target, mixinSource);
    }

    static boolean register(Type type, ObjectReader objectReader) {
        return JSONFactory.getDefaultObjectReaderProvider().register(type, objectReader);
    }

    static boolean register(Type type, ObjectWriter objectReader) {
        return JSONFactory.defaultObjectWriterProvider.register(type, objectReader);
    }

    static void parseObject(InputStream input, Type type, Consumer consumer, JSONReader.Feature... features) {
        parseObject(input, StandardCharsets.UTF_8, '\n', type, consumer, features);
    }

    static void parseObject(InputStream input, Charset charset, char delimiter, Type type, Consumer consumer, JSONReader.Feature... features) {
        ObjectReader objectReader = null;

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

        int limit = 0, start = 0, end = -1;
        try {
            for (; ; ) {
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

                        Object object = objectReader.readObject(jsonReader);
                        consumer.accept(object);
                        start = end + 1;
                    }
                }
                limit += n;

                if (limit == bytes.length) {
                    bytes = Arrays.copyOf(bytes, bytes.length + 8192);
                }
            }
        } catch (IOException ioe) {
            throw new JSONException("read error", ioe);
        }
    }

    static void parseObject(Reader input, char delimiter, Type type, Consumer consumer) {
        ObjectReader objectReader = null;
        char[] chars = JSONFactory.CHARS_UPDATER.getAndSet(JSONFactory.CACHE, null);
        if (chars == null) {
            chars = new char[8192];
        }

        int limit = 0, start = 0, end = -1;
        try {
            for (; ; ) {
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

                        Object object = objectReader.readObject(jsonReader);
                        consumer.accept(object);
                        start = end + 1;
                    }
                }
                limit += n;

                if (limit == chars.length) {
                    chars = Arrays.copyOf(chars, chars.length + 8192);
                }
            }
        } catch (IOException ioe) {
            throw new JSONException("read error", ioe);
        }
    }
}
