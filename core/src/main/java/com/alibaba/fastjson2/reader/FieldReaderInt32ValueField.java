package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE_SUPPORT;
import static com.alibaba.fastjson2.util.UnsafeUtils.UNSAFE;

class FieldReaderInt32ValueField<T>
        extends FieldReaderObjectField<T> {
    final long fieldOffset;
    FieldReaderInt32ValueField(String fieldName, Class fieldType, int ordinal, String format, Integer defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, 0, format, defaultValue, schema, field);
        fieldOffset = UNSAFE_SUPPORT ? UnsafeUtils.objectFieldOffset(field) : 0;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        int fieldInt = jsonReader.readInt32Value();

        if (schema != null) {
            schema.assertValidate(fieldInt);
        }

        if (UNSAFE_SUPPORT) {
            UNSAFE.putInt(object, fieldOffset, fieldInt);
        } else {
            try {
                field.setInt(object, fieldInt);
            } catch (Exception e) {
                throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
            }
        }
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        int fieldInt = jsonReader.readInt32Value();
        accept(object, fieldInt);
    }

    @Override
    public void accept(T object, float value) {
        accept(object, Integer.valueOf((int) value));
    }

    @Override
    public void accept(T object, double value) {
        accept(object, Integer.valueOf((int) value));
    }

    @Override
    public void accept(T object, Object value) {
        int intValue = TypeUtils.toIntValue(value);

        if (schema != null) {
            schema.assertValidate(intValue);
        }

        if (UNSAFE_SUPPORT) {
            UNSAFE.putInt(object, fieldOffset, intValue);
        } else {
            try {
                field.setInt(object, intValue);
            } catch (Exception e) {
                throw new JSONException("set " + fieldName + " error", e);
            }
        }
    }

    @Override
    public void accept(T object, long value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        int intValue = (int) value;
        if (UNSAFE_SUPPORT) {
            UNSAFE.putInt(object, fieldOffset, intValue);
        } else {
            try {
                field.setInt(object, intValue);
            } catch (Exception e) {
                throw new JSONException("set " + fieldName + " error", e);
            }
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32Value();
    }
}
