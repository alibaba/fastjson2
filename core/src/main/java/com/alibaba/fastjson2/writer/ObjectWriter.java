package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;
import com.alibaba.fastjson2.filter.*;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * ObjectWriter is responsible for serializing Java objects into JSON format.
 * It provides a set of methods for writing objects to JSON, handling field mapping,
 * and supporting various serialization features.
 *
 * <p>This interface supports various features including:
 * <ul>
 *   <li>Object serialization to JSON and JSONB formats</li>
 *   <li>Field mapping and value extraction</li>
 *   <li>Array mapping serialization</li>
 *   <li>Type information writing</li>
 *   <li>Custom feature configuration</li>
 *   <li>Filter support for property filtering</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Get an ObjectWriter for a specific type
 * ObjectWriter&lt;User&gt; writer = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(User.class);
 *
 * // Write to JSON string
 * User user = new User(1, "John");
 * String jsonString = writer.toJSONString(user);
 *
 * // Write to JSONWriter
 * try (JSONWriter jsonWriter = JSONWriter.of()) {
 *     writer.write(jsonWriter, user);
 *     String result = jsonWriter.toString();
 * }
 * </pre>
 *
 * @param <T> the type of objects that this ObjectWriter can serialize
 * @since 2.0.0
 */
public interface ObjectWriter<T> {
    /**
     * Gets the features enabled by this ObjectWriter.
     *
     * @return the enabled features as a bit mask
     */
    default long getFeatures() {
        return 0;
    }

    /**
     * Gets the list of FieldWriters associated with this ObjectWriter.
     *
     * @return the list of FieldWriters, or an empty list if none are available
     */
    default List<FieldWriter> getFieldWriters() {
        return Collections.emptyList();
    }

    /**
     * Gets the FieldWriter for the specified field hash code.
     *
     * @param hashCode the hash code of the field name
     * @return the FieldWriter for the field, or null if not found
     */
    default FieldWriter getFieldWriter(long hashCode) {
        return null;
    }

    /**
     * Gets the value of a field from the specified object.
     *
     * @param object the object from which to get the field value
     * @param fieldName the name of the field to get
     * @return the value of the field, or null if the field is not found
     */
    default Object getFieldValue(Object object, String fieldName) {
        FieldWriter fieldWriter = getFieldWriter(fieldName);
        if (fieldWriter == null) {
            return null;
        }
        return fieldWriter.getFieldValue(object);
    }

    /**
     * Gets the FieldWriter for the specified field name.
     *
     * @param name the name of the field
     * @return the FieldWriter for the field, or null if not found
     */
    default FieldWriter getFieldWriter(String name) {
        long nameHash = Fnv.hashCode64(name);
        FieldWriter fieldWriter = getFieldWriter(nameHash);
        if (fieldWriter == null) {
            long nameHashLCase = Fnv.hashCode64LCase(name);
            if (nameHashLCase != nameHash) {
                fieldWriter = getFieldWriter(nameHashLCase);
            }
        }
        return fieldWriter;
    }

    /**
     * Writes type information to the JSON output. This is used for polymorphic serialization
     * to include type information in the JSON output.
     *
     * @param jsonWriter the JSONWriter to which type information should be written
     * @return true if type information was written, false otherwise
     */
    default boolean writeTypeInfo(JSONWriter jsonWriter) {
        return false;
    }

    /**
     * Writes an object to the JSONWriter in JSONB format.
     *
     * @param jsonWriter the JSONWriter to which the object should be written
     * @param object the object to write
     * @param fieldName the name of the field being written
     * @param fieldType the type of the field being written
     * @param features the features to use for writing
     */
    default void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        writeJSONB((JSONWriterJSONB) jsonWriter, object, fieldName, fieldType, features);
    }

    default void writeJSONB(JSONWriterJSONB jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        write(jsonWriter, object, fieldName, fieldType, features);
    }

    /**
     * Writes an object to the JSONWriter in array mapping JSONB format.
     *
     * @param jsonWriter the JSONWriter to which the object should be written
     * @param object the object to write
     */
    default void writeArrayMappingJSONB(JSONWriter jsonWriter, Object object) {
        writeArrayMappingJSONB((JSONWriterJSONB) jsonWriter, object, null, null, 0);
    }

    default void writeArrayMappingJSONB(JSONWriterJSONB jsonWriter, Object object) {
        writeArrayMappingJSONB(jsonWriter, object, null, null, 0);
    }

    /**
     * Writes an object to the JSONWriter in array mapping JSONB format with additional parameters.
     *
     * @param jsonWriter the JSONWriter to which the object should be written
     * @param object the object to write
     * @param fieldName the name of the field being written
     * @param fieldType the type of the field being written
     * @param features the features to use for writing
     */
    default void writeArrayMappingJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        List<FieldWriter> fieldWriters = getFieldWriters();
        int size = fieldWriters.size();
        jsonWriter.startArray(size);
        for (int i = 0; i < size; ++i) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            fieldWriter.writeValue(jsonWriter, object);
        }
    }

    /**
     * Writes an object to the JSONWriter in array mapping format.
     *
     * @param jsonWriter the JSONWriter to which the object should be written
     * @param object the object to write
     * @param fieldName the name of the field being written
     * @param fieldType the type of the field being written
     * @param features the features to use for writing
     */
    default void writeArrayMapping(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.jsonb) {
            writeArrayMappingJSONB(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        List<FieldWriter> fieldWriters = getFieldWriters();
        jsonWriter.startArray();
        boolean hasFilter = hasFilter(jsonWriter);
        if (!hasFilter) {
            for (int i = 0, size = fieldWriters.size(); i < size; ++i) {
                if (i != 0) {
                    jsonWriter.writeComma();
                }
                FieldWriter fieldWriter = fieldWriters.get(i);
                fieldWriter.writeValue(jsonWriter, object);
            }
        } else {
            JSONWriter.Context ctx = jsonWriter.context;
            PropertyPreFilter propertyPreFilter = ctx.getPropertyPreFilter();
            ValueFilter valueFilter = ctx.getValueFilter();
            PropertyFilter propertyFilter = ctx.getPropertyFilter();

            for (int i = 0, size = fieldWriters.size(); i < size; ++i) {
                if (i != 0) {
                    jsonWriter.writeComma();
                }
                FieldWriter fieldWriter = fieldWriters.get(i);
                if (propertyPreFilter != null && !propertyPreFilter.process(jsonWriter, object, fieldWriter.fieldName)) {
                    jsonWriter.writeNull();
                    continue;
                }

                Object fieldValue = fieldWriter.getFieldValue(object);
                if (propertyFilter != null && !propertyFilter.apply(object, fieldWriter.fieldName, fieldValue)) {
                    jsonWriter.writeNull();
                    continue;
                }

                if (valueFilter != null) {
                    Object processValue = valueFilter.apply(object, fieldWriter.fieldName, fieldValue);
                    if (processValue == null) {
                        jsonWriter.writeNull();
                        continue;
                    }

                    ObjectWriter processValueWriter = fieldWriter.getObjectWriter(jsonWriter, processValue.getClass());
                    processValueWriter.write(jsonWriter, fieldValue);
                } else {
                    if (fieldValue == null) {
                        jsonWriter.writeNull();
                        continue;
                    }

                    ObjectWriter fieldValueWriter = fieldWriter.getObjectWriter(jsonWriter, fieldValue.getClass());
                    fieldValueWriter.write(jsonWriter, fieldValue);
                }
            }
        }

        jsonWriter.endArray();
    }

    /**
     * Checks if the JSONWriter has any filters enabled that would affect serialization.
     *
     * @param jsonWriter the JSONWriter to check
     * @return true if filters are enabled, false otherwise
     */
    default boolean hasFilter(JSONWriter jsonWriter) {
        return jsonWriter.hasFilter(JSONWriter.Feature.IgnoreNonFieldGetter.mask);
    }

    /**
     * Writes an object to the JSONWriter using default parameters.
     *
     * @param jsonWriter the JSONWriter to which the object should be written
     * @param object the object to write
     */
    default void write(JSONWriter jsonWriter, Object object) {
        write(jsonWriter, object, null, null, 0);
    }

    /**
     * Converts an object to its JSON string representation using the specified features.
     *
     * @param object the object to convert to JSON
     * @param features the JSON writer features to use
     * @return the JSON string representation of the object
     */
    default String toJSONString(T object, JSONWriter.Feature... features) {
        try (JSONWriter jsonWriter = JSONWriter.of(features)) {
            write(jsonWriter, object, null, null, 0);
            return jsonWriter.toString();
        }
    }

    /**
     * Writes an object to the JSONWriter with the given field name, field type, and features.
     *
     * @param jsonWriter the JSONWriter to which the object should be written
     * @param object the object to write
     * @param fieldName the name of the field being written
     * @param fieldType the type of the field being written
     * @param features the features to use for writing
     */
    void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features);
    default void writeUTF8(JSONWriterUTF8 jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        write(jsonWriter, object, fieldName, fieldType, features);
    }
    default void writeUTF16(JSONWriterUTF16 jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        write(jsonWriter, object, fieldName, fieldType, features);
    }

    /**
     * Writes an object to the JSONWriter with filter support using default parameters.
     *
     * @param jsonWriter the JSONWriter to which the object should be written
     * @param object the object to write
     */
    default void writeWithFilter(JSONWriter jsonWriter, Object object) {
        writeWithFilter(jsonWriter, object, null, null, 0);
    }

    /**
     * Writes an object to the JSONWriter with filter support.
     *
     * @param jsonWriter the JSONWriter to which the object should be written
     * @param object the object to write
     * @param fieldName the name of the field being written
     * @param fieldType the type of the field being written
     * @param features the features to use for writing
     * @throws UnsupportedOperationException if the method is not implemented
     */
    default void writeWithFilter(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the property filter for this ObjectWriter.
     *
     * @param propertyFilter the property filter to set
     */
    default void setPropertyFilter(PropertyFilter propertyFilter) {
    }

    /**
     * Sets the value filter for this ObjectWriter.
     *
     * @param valueFilter the value filter to set
     */
    default void setValueFilter(ValueFilter valueFilter) {
    }

    /**
     * Sets the name filter for this ObjectWriter.
     *
     * @param nameFilter the name filter to set
     */
    default void setNameFilter(NameFilter nameFilter) {
    }

    /**
     * Sets the property pre-filter for this ObjectWriter.
     *
     * @param propertyPreFilter the property pre-filter to set
     */
    default void setPropertyPreFilter(PropertyPreFilter propertyPreFilter) {
    }

    /**
     * Sets a filter for this ObjectWriter. The filter type is determined at runtime
     * and the appropriate setter method is called.
     *
     * @param filter the filter to set
     */
    default void setFilter(Filter filter) {
        if (filter instanceof PropertyFilter) {
            setPropertyFilter((PropertyFilter) filter);
        }

        if (filter instanceof ValueFilter) {
            setValueFilter((ValueFilter) filter);
        }

        if (filter instanceof NameFilter) {
            setNameFilter((NameFilter) filter);
        }
        if (filter instanceof PropertyPreFilter) {
            setPropertyPreFilter((PropertyPreFilter) filter);
        }
    }
}
