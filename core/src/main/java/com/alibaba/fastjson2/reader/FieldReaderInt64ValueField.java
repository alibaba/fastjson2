package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE_SUPPORT;
import static com.alibaba.fastjson2.util.UnsafeUtils.UNSAFE;

class FieldReaderInt64ValueField<T>
        extends FieldReaderObjectField<T> {
    final long fieldOffset;
    FieldReaderInt64ValueField(String fieldName, Class fieldType, int ordinal, long features, String format, Long defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
        fieldOffset = UNSAFE_SUPPORT ? UnsafeUtils.objectFieldOffset(field) : 0;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        long fieldLong = jsonReader.readInt64Value();

        if (schema != null) {
            schema.assertValidate(fieldLong);
        }

        if (UNSAFE_SUPPORT) {
            UNSAFE.putLong(object, fieldOffset, fieldLong);
        } else {
            try {
                field.setLong(object, fieldLong);
            } catch (Exception e) {
                throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
            }
        }
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        readFieldValue(jsonReader, object);
    }

    @Override
    public void accept(T object, float value) {
        accept(object, Long.valueOf((long) value));
    }

    @Override
    public void accept(T object, double value) {
        accept(object, Long.valueOf((long) value));
    }

    @Override
    public void accept(T object, Object value) {
        long longValue = TypeUtils.toLongValue(value);

        if (schema != null) {
            schema.assertValidate(longValue);
        }

        if (UNSAFE_SUPPORT) {
            UNSAFE.putLong(object, fieldOffset, longValue);
        } else {
            try {
                field.setLong(object, longValue);
            } catch (Exception e) {
                throw new JSONException("set " + fieldName + " error", e);
            }
        }
    }
}
