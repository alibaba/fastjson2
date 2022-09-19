package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

class FieldReaderObjectMethod<T>
        extends FieldReaderImpl<T>
        implements FieldReaderObject<T, Object> {
    ObjectReader fieldObjectReader;

    FieldReaderObjectMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Locale locale, Object defaultValue, JSONSchema schema, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, null);
    }

    @Override
    public ObjectReader getInitReader() {
        return fieldObjectReader;
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null && (features & JSONReader.Feature.IgnoreSetNullValue.mask) != 0) {
            return;
        }

        if (schema != null) {
            schema.assertValidate(value);
        }

        if (fieldClass == char.class) {
            char charValue;
            if (value instanceof String) {
                charValue = ((String) value).charAt(0);
            } else if (value instanceof Character) {
                charValue = (Character) value;
            } else {
                throw new JSONException("cast to char error");
            }
            value = charValue;
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
    public ObjectReader getObjectReader(JSONReader jsonReader) {
        if (fieldObjectReader != null) {
            return fieldObjectReader;
        }

        ObjectReader formattedObjectReader = FieldReaderObject.createFormattedObjectReader(fieldType, fieldClass, format, null);
        if (formattedObjectReader != null) {
            return fieldObjectReader = formattedObjectReader;
        }

        return fieldObjectReader = jsonReader.getObjectReader(fieldType);
    }

    public ObjectReader getObjectReader(JSONReader.Context context) {
        if (fieldObjectReader != null) {
            return fieldObjectReader;
        }

        ObjectReader formattedObjectReader = FieldReaderObject.createFormattedObjectReader(fieldType, fieldClass, format, null);
        if (formattedObjectReader != null) {
            return fieldObjectReader = formattedObjectReader;
        }

        return fieldObjectReader = context.getObjectReader(fieldType);
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
            if (jsonReader.isJSONB()) {
                ObjectReader autoTypeReader = checkObjectAutoType(jsonReader);
                if (autoTypeReader != null) {
                    value = autoTypeReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
                } else {
                    value = objectReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
                }
            } else {
                value = objectReader.readObject(jsonReader, fieldType, fieldName, features);
            }
        } catch (JSONException ex) {
            throw new JSONException(jsonReader.info("read field error : " + fieldName), ex);
        }

        accept(object, value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        if (fieldObjectReader == null) {
            fieldObjectReader = getObjectReader(jsonReader);
        }

        return jsonReader.isJSONB()
                ? fieldObjectReader.readJSONBObject(jsonReader, fieldType, fieldName, features)
                : fieldObjectReader.readObject(jsonReader, fieldType, fieldName, features);
    }
}
