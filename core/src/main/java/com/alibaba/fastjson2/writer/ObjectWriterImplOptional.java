package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Optional;

/**
 * ObjectWriterImplOptional provides serialization support for {@link java.util.Optional} values to JSON format.
 * This writer unwraps Optional values and serializes the contained value, or writes null if the Optional is empty.
 *
 * <p>This class provides support for:
 * <ul>
 *   <li>Optional value unwrapping and serialization</li>
 *   <li>Empty Optional serialization as null</li>
 *   <li>Type-specific formatting for wrapped values</li>
 *   <li>Dynamic ObjectWriter resolution for wrapped values</li>
 *   <li>JSONB format support</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Serialize Optional with value
 * Optional<String> optional = Optional.of("Hello");
 * String json = JSON.toJSONString(optional); // "Hello"
 *
 * // Serialize empty Optional
 * Optional<String> empty = Optional.empty();
 * String json = JSON.toJSONString(empty); // null
 *
 * // Serialize Optional with complex object
 * Optional<User> userOpt = Optional.of(new User(1, "John"));
 * String json = JSON.toJSONString(userOpt); // {"id":1,"name":"John"}
 * }</pre>
 *
 * @since 2.0.0
 */
final class ObjectWriterImplOptional
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplOptional INSTANCE = new ObjectWriterImplOptional(null, null);

    Type valueType;
    long features;

    final String format;
    final Locale locale;

    public static ObjectWriterImplOptional of(String format, Locale locale) {
        if (format == null) {
            return INSTANCE;
        }

        return new ObjectWriterImplOptional(format, locale);
    }

    public ObjectWriterImplOptional(String format, Locale locale) {
        this.format = format;
        this.locale = locale;
    }

    public ObjectWriterImplOptional(Type valueType, String format, Locale locale) {
        this.valueType = valueType;
        this.format = format;
        this.locale = locale;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        Optional optional = (Optional) object;
        if (!optional.isPresent()) {
            jsonWriter.writeNull();
            return;
        }

        Object value = optional.get();
        ObjectWriter objectWriter = jsonWriter.getObjectWriter(value.getClass());
        objectWriter.writeJSONB(jsonWriter, value, fieldName, null, features);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        Optional optional = (Optional) object;
        if (!optional.isPresent()) {
            jsonWriter.writeNull();
            return;
        }

        Object value = optional.get();
        Class<?> valueClass = value.getClass();
        ObjectWriter valueWriter = null;
        if (format != null) {
            valueWriter = FieldWriter.getObjectWriter(null, null, format, locale, valueClass);
        }

        if (valueWriter == null) {
            valueWriter = jsonWriter.getObjectWriter(valueClass);
        }
        valueWriter.write(jsonWriter, value, fieldName, valueType, this.features);
    }
}
