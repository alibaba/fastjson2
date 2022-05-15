package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONSchema;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

class FieldReaderObjectMethod<T>
        extends FieldReaderImpl<T>
        implements FieldReaderObject<T, Object> {

    final Method method;
    ObjectReader fieldObjectReader;

    FieldReaderObjectMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, null);
        this.method = method;
    }

    FieldReaderObjectMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Object defaultValue, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, defaultValue);
        this.method = method;
    }

    FieldReaderObjectMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Locale locale, Object defaultValue, JSONSchema schema, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema);
        this.method = method;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null && (features & JSONReader.Feature.IgnoreSetNullValue.mask) != 0) {
            return;
        }

        if (schema != null) {
            schema.validate(value);
        }

        try {
            method.invoke(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error, " + getClass().getName(), e);
        }
    }

    @Override
    public String toString() {
        if (method != null) {
            return method.getName();
        }
        return fieldName;
    }

    @Override
    public ObjectReader getFieldObjectReader(JSONReader.Context context) {
        if (fieldObjectReader == null) {
            fieldObjectReader = context
                    .getObjectReader(fieldType);
        }
        return fieldObjectReader;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        ObjectReader objectReader;
        if (this.fieldObjectReader != null) {
            objectReader = this.fieldObjectReader;
        } else {
            ObjectReader formattedObjectReader = FieldReaderObject.createFormattedObjectReader(fieldType, fieldClass, format, locale);
            if (formattedObjectReader != null) {
                objectReader = this.fieldObjectReader = formattedObjectReader;
            } else {
                objectReader = this.fieldObjectReader = jsonReader.getContext().getObjectReader(fieldType);
            }
        }

        if (jsonReader.isReference()) {
            String reference = jsonReader.readReference();
            if ("..".equals(reference)) {
                accept(object, object);
            } else {
                addResolveTask(jsonReader, object, reference);
            }
            return;
        }

        Object value;
        try {
            value = jsonReader.isJSONB()
                    ? objectReader.readJSONBObject(jsonReader, features)
                    : objectReader.readObject(jsonReader, features);
        } catch (JSONException ex) {
            throw new JSONException("read field error : " + fieldName, ex);
        }

        accept(object, value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        if (fieldObjectReader == null) {
            fieldObjectReader = jsonReader
                    .getContext()
                    .getObjectReader(fieldType);
        }

        return jsonReader.isJSONB()
                ? fieldObjectReader.readJSONBObject(jsonReader, features)
                : fieldObjectReader.readObject(jsonReader, features);
    }
}
