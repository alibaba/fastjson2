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
    String VERSION = "2.0.2";

    static Object parse(String str) {
        JSONReader reader = JSONReader.of(str);
        ObjectReader objectReader = reader.getObjectReader(Object.class);
        return objectReader.readObject(reader, 0);
    }

    static Object parse(String str, JSONReader.Feature... features) {
        JSONReader reader = JSONReader.of(str);
        reader.getContext().config(features);
        ObjectReader objectReader = reader.getObjectReader(Object.class);
        return objectReader.readObject(reader, 0);
    }

    static <T> T parseObject(String str, Class<T> objectClass) {
        JSONReader reader = JSONReader.of(str);

        JSONReader.Context context = reader.context;

        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(objectClass, fieldBased);
        return (T) objectReader.readObject(reader, 0);
    }

    static <T> T parseObject(String str, Type objectType) {
        JSONReader reader = JSONReader.of(str);
        ObjectReader objectReader = reader.context.provider.getObjectReader(objectType);
        return (T) objectReader.readObject(reader, 0);
    }

    static <T> T parseObject(String str, Class<T> objectClass, JSONReader.Feature... features) {
        JSONReader reader = JSONReader.of(str);

        JSONReader.Context context = reader.context;
        context.config(features);

        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;

        ObjectReader objectReader = context.provider.getObjectReader(objectClass, fieldBased);
        return (T) objectReader.readObject(reader, 0);
    }

    static <T> T parseObject(String str, Class<T> objectClass, String format, JSONReader.Feature... features) {
        JSONReader reader = JSONReader.of(str);

        JSONReader.Context context = reader.context;
        if (format != null) {
            context.setUtilDateFormat(format);
        }
        context.config(features);

        ObjectReader objectReader = context.provider.getObjectReader(objectClass);
        return (T) objectReader.readObject(reader, 0);
    }

    static <T> T parseObject(String str, Type objectType, JSONReader.Feature... features) {
        JSONReader reader = JSONReader.of(str);
        reader.getContext().config(features);
        ObjectReader objectReader = reader.getObjectReader(objectType);
        return (T) objectReader.readObject(reader, 0);
    }

    static <T> T parseObject(byte[] utf8Bytes, Type objectType) {
        JSONReader reader = JSONReader.of(utf8Bytes);
        ObjectReader objectReader = reader.getObjectReader(objectType);
        return (T) objectReader.readObject(reader, 0);
    }

    static <T> T parseObject(byte[] utf8Bytes, Class<T> objectClass) {
        JSONReader reader = JSONReader.of(utf8Bytes);
        ObjectReader objectReader = reader.getObjectReader(objectClass);
        return (T) objectReader.readObject(reader, 0);
    }

    static <T> T parseObject(byte[] utf8Bytes, Type objectClass, JSONReader.Feature... features) {
        JSONReader reader = JSONReader.of(utf8Bytes);
        reader.getContext().config(features);
        ObjectReader objectReader = reader.getObjectReader(objectClass);
        return (T) objectReader.readObject(reader, 0);
    }

    static <T> T parseObject(byte[] utf8Bytes, int off, int len, Charset charset, Type objectClass) {
        JSONReader reader = JSONReader.of(utf8Bytes, off, len, charset);
        ObjectReader objectReader = reader.getObjectReader(objectClass);
        return (T) objectReader.readObject(reader, 0);
    }

    static JSONObject parseObject(String str) {
        JSONReader reader = JSONReader.of(str);
        ObjectReader<JSONObject> objectReader = reader.getObjectReader(JSONObject.class);
        return objectReader.readObject(reader, 0);
    }

    static JSONObject parseObject(byte[] ut8Bytes) {
        JSONReader reader = JSONReader.of(ut8Bytes);
        ObjectReader<JSONObject> objectReader = reader.getObjectReader(JSONObject.class);
        return objectReader.readObject(reader, 0);
    }

    static JSONArray parseArray(String str) {
        JSONReader reader = JSONReader.of(str);
        ObjectReader<JSONArray> objectReader = reader.getObjectReader(JSONArray.class);
        return objectReader.readObject(reader, 0);
    }

    static <T> List<T> parseArray(String str, Type itemType) {
        ParameterizedTypeImpl paramType = new ParameterizedTypeImpl(new Type[]{itemType}, null, List.class);
        JSONReader reader = JSONReader.of(str);
        return reader.read(paramType);
    }

    static <T> List<T> parseArray(String str, Type[] itemTypes) {
        List array = new JSONArray(itemTypes.length);
        JSONReader jsonReader = JSONReader.of(str);
        jsonReader.startArray();
        for (Type itemType : itemTypes) {
            array.add(
                    jsonReader.read(itemType)
            );
        }
        jsonReader.endArray();
        return array;
    }

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

    static String toJSONString(Object object, JSONWriter.Feature... features) {
        JSONWriter.Context writeContext = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features);

        boolean pretty = (writeContext.features & JSONWriter.Feature.PrettyFormat.mask) != 0;
        JSONWriterUTF16 jsonWriterUTF16 = JDKUtils.JVM_VERSION == 8 ? new JSONWriterUTF16JDK8(writeContext) : new JSONWriterUTF16(writeContext);
        try (JSONWriter writer = pretty
                ? new JSONWriterPretty(jsonWriterUTF16)
                : jsonWriterUTF16
        ) {
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

    static int writeTo(
            OutputStream out
            , Object object
            , JSONWriter.Feature... features) {

        try (JSONWriter writer = JSONWriter.ofUTF8(features)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = writer.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            int len = writer.flushTo(out);
            return len;
        } catch (IOException e) {
            throw new JSONException("writeJSONString error", e);
        }
    }

    static int writeTo(
            OutputStream out
            , Object object
            , Filter[] filters
            , JSONWriter.Feature... features) {

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

            int len = writer.flushTo(out);
            return len;
        } catch (IOException e) {
            throw new JSONException("writeJSONString error", e);
        }
    }

    static boolean isValid(String str) {
        JSONReader jsonReader = JSONReader.of(str);
        try {
            jsonReader.skipValue();
        } catch (JSONException error) {
            return false;
        }
        return true;
    }

    static boolean isValidArray(String str) {
        JSONReader jsonReader = JSONReader.of(str);
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

    static boolean isValid(byte[] jsonBytes) {
        JSONReader jsonReader = JSONReader.of(jsonBytes);
        try {
            jsonReader.skipValue();
        } catch (JSONException error) {
            return false;
        }
        return true;
    }

    static boolean isValid(byte[] jsonBytes, int offset, int length, Charset charset) {
        JSONReader jsonReader = JSONReader.of(jsonBytes, offset, length, charset);
        try {
            jsonReader.skipValue();
        } catch (JSONException error) {
            return false;
        }
        return true;
    }

    static Object toJSON(Object javaObject) {
        if (javaObject instanceof JSONObject || javaObject instanceof JSONArray) {
            return javaObject;
        }

        String str = JSON.toJSONString(javaObject);
        return JSON.parse(str);
    }

    static <T> T toJavaObject(Object json, Class<T> clazz) {
        if (json instanceof JSONObject) {
            return ((JSONObject) json).toJavaObject(clazz);
        }

        return TypeUtils.cast(json, clazz);
    }
}