package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

final class FieldWriterDateMethod<T>
        extends FieldWriterDate<T> {
    protected FieldWriterDateMethod(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Class fieldClass,
            Method method
    ) {
        super(fieldName, ordinal, features, format, label, fieldClass, fieldClass, null, method);
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return method.invoke(object);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new JSONException("invoke getter method error, " + fieldName, e);
        }
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Date value = (Date) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }
        writeDate(jsonWriter, false, value.getTime());
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Date value = (Date) getFieldValue(object);

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }

        writeDate(jsonWriter, value.getTime());
        return true;
    }
}
