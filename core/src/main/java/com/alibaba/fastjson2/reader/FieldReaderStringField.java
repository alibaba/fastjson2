package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE_SUPPORT;
import static com.alibaba.fastjson2.util.UnsafeUtils.UNSAFE;

class FieldReaderStringField<T>
        extends FieldReaderObjectField<T> {
    final boolean trim;
    final long fieldOffset;

    FieldReaderStringField(String fieldName, Class fieldType, int ordinal, long features, String format, String defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
        trim = "trim".equals(format) || (features & JSONReader.Feature.TrimString.mask) != 0;
        fieldOffset = UNSAFE_SUPPORT ? UnsafeUtils.objectFieldOffset(field) : 0;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        String fieldValue = jsonReader.readString();
        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        if (UNSAFE_SUPPORT) {
            UNSAFE.putObject(object, fieldOffset, fieldValue);
        } else {
            try {
                field.set(object, fieldValue);
            } catch (Exception e) {
                throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
            }
        }
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        String fieldValue = jsonReader.readString();
        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        accept(object, fieldValue);
    }

    @Override
    public String readFieldValue(JSONReader jsonReader) {
        String fieldValue = jsonReader.readString();
        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }
        return fieldValue;
    }

    @Override
    public void accept(T object, Object value) {
        String fieldValue;
        if (value != null && !(value instanceof String)) {
            fieldValue = value.toString();
        } else {
            fieldValue = (String) value;
        }

        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        if (UNSAFE_SUPPORT) {
            UNSAFE.putObject(object, fieldOffset, fieldValue);
        } else {
            try {
                field.set(object, fieldValue);
            } catch (Exception e) {
                throw new JSONException("set " + fieldName + " error", e);
            }
        }
    }
}
