package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

public interface JSON {
    /**
     * FASTJSON2 version name
     */
    String VERSION = "2.0.2";

    /**
     * Parse JSON {@link String} into Object
     *
     * @param text the JSON {@link String} to be parsed
     */
    static Object parse(String text) {
        JSONReader reader = JSONReader.of(text);
        ObjectReader objectReader = reader.getObjectReader(Object.class);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into Object with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param features features to be enabled in parsing
     */
    static Object parse(String text, JSONReader.Feature... features) {
        JSONReader reader = JSONReader.of(text);
        reader.getContext().config(features);
        ObjectReader objectReader = reader.getObjectReader(Object.class);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into JSONObject
     *
     * @param text the JSON {@link String} to be parsed
     */
    static JSONObject parseObject(String text) {
        JSONReader reader = JSONReader.of(text);
        ObjectReader<JSONObject> objectReader = reader.getObjectReader(JSONObject.class);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse UTF8 encoded JSON byte array into a JSONObject
     *
     * @param bytes UTF8 encoded JSON byte array to parse
     */
    static JSONObject parseObject(byte[] bytes) {
        JSONReader reader = JSONReader.of(bytes);
        ObjectReader<JSONObject> objectReader = reader.getObjectReader(JSONObject.class);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into Java object
     *
     * @param text  the JSON {@link String} to be parsed
     * @param clazz specify the Class to be converted
     */
    static <T> T parseObject(String text, Class<T> clazz) {
        JSONReader reader = JSONReader.of(text);
        JSONReader.Context context = reader.context;

        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(clazz, fieldBased);
        return (T) objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into Java object
     *
     * @param text the JSON {@link String} to be parsed
     * @param type specify the {@link Type} to be converted
     */
    static <T> T parseObject(String text, Type type) {
        JSONReader reader = JSONReader.of(text);
        ObjectReader objectReader = reader.context.provider.getObjectReader(type);
        return (T) objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into Java object
     *
     * @param text          the JSON {@link String} to be parsed
     * @param typeReference specify the {@link TypeReference} to be converted
     */
    static <T> T parseObject(String text, TypeReference typeReference) {
        JSONReader reader = JSONReader.of(text);
        ObjectReader objectReader = reader.context.provider.getObjectReader(typeReference.getType());
        return (T) objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param clazz    specify the Class to be converted
     * @param features features to be enabled in parsing
     */
    static <T> T parseObject(String text, Class<T> clazz, JSONReader.Feature... features) {
        JSONReader reader = JSONReader.of(text);

        JSONReader.Context context = reader.context;
        context.config(features);

        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;

        ObjectReader objectReader = context.provider.getObjectReader(clazz, fieldBased);
        return (T) objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param clazz    specify the Class to be converted
     * @param format   the specified date format
     * @param features features to be enabled in parsing
     */
    static <T> T parseObject(String text, Class<T> clazz, String format, JSONReader.Feature... features) {
        JSONReader reader = JSONReader.of(text);

        JSONReader.Context context = reader.context;
        if (format != null) {
            context.setUtilDateFormat(format);
        }
        context.config(features);

        ObjectReader objectReader = context.provider.getObjectReader(clazz);
        return (T) objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param text     the JSON {@link String} to be parsed
     * @param type     specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     */
    static <T> T parseObject(String text, Type type, JSONReader.Feature... features) {
        JSONReader reader = JSONReader.of(text);
        reader.getContext().config(features);
        ObjectReader objectReader = reader.getObjectReader(type);
        return (T) objectReader.readObject(reader, 0);
    }

    /**
     * Parse UTF8 encoded JSON byte array into a Java object
     *
     * @param bytes UTF8 encoded JSON byte array to parse
     * @param type  specify the {@link Type} to be converted
     */
    static <T> T parseObject(byte[] bytes, Type type) {
        JSONReader reader = JSONReader.of(bytes);
        ObjectReader objectReader = reader.getObjectReader(type);
        return (T) objectReader.readObject(reader, 0);
    }

    /**
     * Parse UTF8 encoded JSON byte array into a Java object
     *
     * @param bytes UTF8 encoded JSON byte array to parse
     * @param clazz specify the Class to be converted
     */
    static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        JSONReader reader = JSONReader.of(bytes);
        ObjectReader objectReader = reader.getObjectReader(clazz);
        return (T) objectReader.readObject(reader, 0);
    }

    /**
     * Parse UTF8 encoded JSON byte array into a Java object with specified {@link JSONReader.Feature}s enabled
     *
     * @param bytes    UTF8 encoded JSON byte array to parse
     * @param type     specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     */
    static <T> T parseObject(byte[] bytes, Type type, JSONReader.Feature... features) {
        JSONReader reader = JSONReader.of(bytes);
        reader.getContext().config(features);
        ObjectReader objectReader = reader.getObjectReader(type);
        return (T) objectReader.readObject(reader, 0);
    }

    /**
     * Parses the JSON byte array of the specified {@link Charset} into a Java object
     *
     * @param bytes   JSON byte array to parse
     * @param offset  the index of the first byte to parse
     * @param length  the number of bytes to parse
     * @param charset specify {@link Charset} to parse
     * @param type    specify the {@link Type} to be converted
     * @throws IndexOutOfBoundsException If the offset and the length arguments index characters outside the bounds of the bytes array
     */
    static <T> T parseObject(byte[] bytes, int offset, int length, Charset charset, Type type) {
        JSONReader reader = JSONReader.of(bytes, offset, length, charset);
        ObjectReader objectReader = reader.getObjectReader(type);
        return (T) objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into JSONArray
     *
     * @param text the JSON {@link String} to be parsed
     */
    static JSONArray parseArray(String text) {
        JSONReader reader = JSONReader.of(text);
        ObjectReader<JSONArray> objectReader = reader.getObjectReader(JSONArray.class);
        return objectReader.readObject(reader, 0);
    }

    /**
     * Parse JSON {@link String} into Java object
     *
     * @param text the JSON {@link String} to be parsed
     * @param type specify the {@link Type} to be converted
     */
    static <T> List<T> parseArray(String text, Type type) {
        ParameterizedTypeImpl paramType = new ParameterizedTypeImpl(new Type[]{type}, null, List.class);
        JSONReader reader = JSONReader.of(text);
        return reader.read(paramType);
    }


    /**
     * Parse JSON {@link String} into Java object
     *
     * @param text  the JSON {@link String} to be parsed
     * @param types specify some {@link Type}s to be converted
     */
    static <T> List<T> parseArray(String text, Type[] types) {
        List array = new JSONArray(types.length);
        JSONReader jsonReader = JSONReader.of(text);
        jsonReader.startArray();
        for (Type itemType : types) {
            array.add(jsonReader.read(itemType));
        }
        jsonReader.endArray();
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
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
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
                ObjectWriter objectWriter = writeContext.provider.getObjectWriter(valueClass, valueClass, fieldBased);
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
                    JSONWriter.Context context = writer.getContext();
                    context.configFilter(filters);
                }

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
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
                    JSONWriter.Context context = writer.getContext();
                    context.configFilter(new Filter[]{filter});
                }

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
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

                JSONWriter.Context context = writer.getContext();
                context.setDateFormat(format);

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        }
    }

    /**
     * Serialize Java objects to JSON byte array
     *
     * @param object Java Object to be serialized into JSON {@link String}
     */
    static byte[] toJSONBytes(Object object) {
        try (JSONWriter writer = JSONWriter.ofUTF8()) {
            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    /**
     * Serialize Java objects to JSON byte array
     *
     * @param object  Java Object to be serialized into JSON {@link String}
     * @param filters specifies the filter to use in serialization
     */
    static byte[] toJSONBytes(Object object, Filter... filters) {
        try (JSONWriter writer = JSONWriter.ofUTF8()) {
            JSONWriter.Context context = writer.getContext();

            if (filters.length != 0) {
                context.configFilter(filters);
            }

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    /**
     * Serialize Java objects to JSON byte array with specified {@link JSONReader.Feature}s enabled
     *
     * @param object   Java Object to be serialized into JSON {@link String}
     * @param features features to be enabled in serialization
     */
    static byte[] toJSONBytes(Object object, JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.ofUTF8(features)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.getBytes();
        }
    }

    /**
     * Serialize Java objects to JSON byte array with specified {@link JSONReader.Feature}s enabled
     *
     * @param object   Java Object to be serialized into JSON {@link String}
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
                    JSONWriter.Context context = writer.getContext();
                    context.configFilter(filters);
                }

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
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
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
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
                    JSONWriter.Context context = writer.getContext();
                    context.configFilter(filters);
                }

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
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
     */
    static boolean isValid(String text) {
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
     */
    static boolean isValidArray(String text) {
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
     */
    static boolean isValid(byte[] bytes) {
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
     */
    static boolean isValidArray(byte[] bytes) {
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
     */
    static boolean isValid(byte[] bytes, int offset, int length, Charset charset) {
        JSONReader jsonReader = JSONReader.of(bytes, offset, length, charset);
        try {
            jsonReader.skipValue();
        } catch (JSONException error) {
            return false;
        }
        return true;
    }

    /**
     * Convert Java object order to JSON array or JSONObject
     *
     * @param object Java Object to be converted
     */
    static Object toJSON(Object object) {
        if (object instanceof JSONObject || object instanceof JSONArray) {
            return object;
        }

        String str = JSON.toJSONString(object);
        return JSON.parse(str);
    }

    /**
     * Convert this {@link JSONObject} to the target type
     *
     * @param object Java Object to be converted
     * @param clazz  converted goal class
     */
    static <T> T toJavaObject(Object object, Class<T> clazz) {
        if (object instanceof JSONObject) {
            return ((JSONObject) object).toJavaObject(clazz);
        }

        return TypeUtils.cast(object, clazz);
    }
}