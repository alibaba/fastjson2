package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

class FieldReaderAnySetter<T> extends FieldReaderObjectMethod<T> implements FieldReaderReadOnly<T> {
    volatile ObjectReader itemReader;

    FieldReaderAnySetter(
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Method method) {
        super("$$any$$", fieldType, fieldClass, ordinal, features, format, null, null, method);
    }

    @Override
    public ObjectReader getItemObjectReader(JSONReader jsonReader) {
        if (itemReader != null) {
            return itemReader;
        }
        return itemReader = jsonReader.getObjectReader(fieldType);
    }

    @Override
    public void accept(T object, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processExtra(JSONReader jsonReader, Object object) {
        String name = jsonReader.getFieldName();

        ObjectReader itemObjectReader = getItemObjectReader(jsonReader);
        Object value = itemObjectReader.readObject(jsonReader, 0);

        try {
            method.invoke(object, name, value);
        } catch (Exception e) {
            throw new JSONException("any set error", e);
        }
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (fieldObjectReader == null) {
            fieldObjectReader = jsonReader
                    .getContext()
                    .getObjectReader(fieldType);
        }

        Object value;
        if (jsonReader.isJSONB()) {
            value = fieldObjectReader.readJSONBObject(jsonReader, features);
        } else {
            value = fieldObjectReader.readObject(jsonReader, features);
        }

        accept(object, value);
    }
}
