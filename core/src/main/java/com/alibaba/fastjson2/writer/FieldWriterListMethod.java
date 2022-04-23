package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

final class FieldWriterListMethod<T> extends FieldWriterList<T> {
    final Method method;

    protected FieldWriterListMethod(
            String fieldName
            , Type itemType
            , int ordinal
            , long features
            , String format
            , Method method
            , Type fieldType
            , Class fieldClass
    ) {

        super(fieldName, itemType, ordinal, features, format, fieldType, fieldClass);
        this.method = method;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return method.invoke(object);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new JSONException("invoke getter method error, " + name, e);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        List value;
        try {
            value = (List) getFieldValue(object);
        } catch (JSONException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeArrayNull();
                return true;
            } else {
                return false;
            }
        }

        writeList(jsonWriter, true, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        List value = (List) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        writeList(jsonWriter, false, value);
    }
}
