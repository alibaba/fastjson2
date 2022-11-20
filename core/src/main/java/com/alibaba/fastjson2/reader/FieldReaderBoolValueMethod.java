package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class FieldReaderBoolValueMethod<T>
        extends FieldReaderObject<T> {
    FieldReaderBoolValueMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Boolean defaultValue, JSONSchema schema, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, null, defaultValue, schema, method, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        boolean fieldValue = jsonReader.readBoolValue();
        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        boolean fieldValue = jsonReader.readBoolValue();
        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        boolean booleanValue = TypeUtils.toBooleanValue(value);

        try {
            method.invoke(object, booleanValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readBool();
    }
}
