package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.*;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public interface ObjectWriter<T> {
    default long getFeatures() {
        return 0;
    }

    default List<FieldWriter> getFieldWriters() {
        return Collections.emptyList();
    }

    default FieldWriter getFieldWriter(long hashCode) {
        return null;
    }

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

    default boolean writeTypeInfo(JSONWriter jsonWriter) {
        return false;
    }

    default void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        write(jsonWriter, object, fieldName, fieldType, features);
    }

    default void writeArrayMappingJSONB(JSONWriter jsonWriter, Object object) {
        writeArrayMappingJSONB(jsonWriter, object, null, null, 0);
    }

    default void writeArrayMappingJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        List<FieldWriter> fieldWriters = getFieldWriters();
        int size = fieldWriters.size();
        jsonWriter.startArray(size);
        for (int i = 0; i < size; ++i) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            fieldWriter.writeValue(jsonWriter, object);
        }
    }

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

    default boolean hasFilter(JSONWriter jsonWriter) {
        return jsonWriter.hasFilter(JSONWriter.Feature.IgnoreNonFieldGetter.mask);
    }

    default void write(JSONWriter jsonWriter, Object object) {
        write(jsonWriter, object, null, null, 0);
    }

    void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features);

    default void writeWithFilter(JSONWriter jsonWriter, Object object) {
        writeWithFilter(jsonWriter, object, null, null, 0);
    }

    default void writeWithFilter(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        throw new UnsupportedOperationException();
    }

    default void setPropertyFilter(PropertyFilter propertyFilter) {
    }

    default void setValueFilter(ValueFilter valueFilter) {
    }

    default void setNameFilter(NameFilter nameFilter) {
    }

    default void setPropertyPreFilter(PropertyPreFilter propertyPreFilter) {
    }

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
