package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.Feature.BeanToArray;

public class FieldWriterObjectFinal<T>
        extends FieldWriterObject<T> {
    final Type fieldType;
    final Class fieldClass;
    volatile ObjectWriter objectWriter;
    final boolean refDetect;

    public FieldWriterObjectFinal(
            String name,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Function function
    ) {
        super(name, ordinal, features, format, locale, label, fieldType, fieldClass, field, method, function);
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
        this.refDetect = !ObjectWriterProvider.isNotReferenceDetect(fieldClass);
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (fieldClass != valueClass) {
            return super.getObjectWriter(jsonWriter, valueClass);
        }

        // see FieldWriterObject#getObjectWriter: the common path pays one feature bit test only
        if ((jsonWriter.getFeatures(features) & JSONWriter.Feature.SortFieldNamesAlphabetically.mask) != 0) {
            ObjectWriter sortedWriter = getSortedVariantObjectWriter(jsonWriter, valueClass);
            if (sortedWriter != null) {
                return sortedWriter;
            }
        }

        if (objectWriter != null) {
            return objectWriter;
        } else {
            return objectWriter = super.getObjectWriter(jsonWriter, valueClass);
        }
    }

    /**
     * Resolves the writer for a value written under
     * {@link JSONWriter.Feature#SortFieldNamesAlphabetically}; called only when the caller saw the
     * feature bit. See {@link FieldWriterObject#getObjectWriter} for the storage rules.
     *
     * @param jsonWriter the writer being used
     * @param valueClass the runtime class of the value
     * @return the sorted-variant writer, or null when this field must keep the regular resolution
     */
    private ObjectWriter getSortedVariantObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (writeUsing
                || format != null
                || BeanUtils.SUPER.equals(fieldName)) {
            return null;
        }
        long mergedFeatures = jsonWriter.getFeatures(features);
        if (!ObjectWriterProvider.isFieldNamesSorted(mergedFeatures)) {
            return null;
        }
        return jsonWriter.getContext().getProvider()
                .getObjectWriter(valueClass, valueClass, mergedFeatures);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Object value;
        try {
            value = getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) == 0) {
                return false;
            }
            writeFieldName(jsonWriter);
            if (fieldClass.isArray()) {
                jsonWriter.writeArrayNull();
            } else if (fieldClass == StringBuffer.class || fieldClass == StringBuilder.class) {
                jsonWriter.writeStringNull();
            } else {
                jsonWriter.writeObjectNull(fieldClass);
            }
            return true;
        }

        ObjectWriter valueWriter = getObjectWriter(jsonWriter, fieldClass);

        if (unwrapped
                && writeWithUnwrapped(jsonWriter, value, features, refDetect, valueWriter)) {
            return true;
        }

        writeFieldName(jsonWriter);
        if (jsonWriter.jsonb) {
            valueWriter.writeJSONB(jsonWriter, value, fieldName, fieldType, features);
        } else {
            valueWriter.write(jsonWriter, value, fieldName, fieldType, features);
        }

        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Object value = getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        boolean refDetect = this.refDetect && jsonWriter.isRefDetect();

        if (refDetect) {
            if (value == object) {
                jsonWriter.writeReference("..");
                return;
            }

            String refPath = jsonWriter.setPath(fieldName, value);
            if (refPath != null) {
                jsonWriter.writeReference(refPath);
                jsonWriter.popPath(value);
                return;
            }
        }

        ObjectWriter valueWriter = getObjectWriter(jsonWriter, fieldClass);

        boolean beanToArray = (jsonWriter.getFeatures(features) & BeanToArray.mask) != 0;
        if (jsonWriter.jsonb) {
            if (beanToArray) {
                valueWriter.writeArrayMappingJSONB(jsonWriter, value, fieldName, fieldType, features);
            } else {
                valueWriter.writeJSONB(jsonWriter, value, fieldName, fieldType, features);
            }
        } else {
            if (beanToArray) {
                valueWriter.writeArrayMapping(jsonWriter, value, fieldName, fieldType, features);
            } else {
                valueWriter.write(jsonWriter, value, fieldName, fieldType, features);
            }
        }

        if (refDetect) {
            jsonWriter.popPath(value);
        }
    }
}
