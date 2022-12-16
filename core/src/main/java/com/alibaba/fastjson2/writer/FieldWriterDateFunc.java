package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.function.Function;

final class FieldWriterDateFunc<T>
        extends FieldWriterDate<T> {
    Function<T, Date> function;

    protected FieldWriterDateFunc(
            String fieldName,
            int ordinal,
            long features,
            String dateTimeFormat,
            String label,
            Method method,
            Function<T, Date> function
    ) {
        super(fieldName, ordinal, features, dateTimeFormat, label, Date.class, Date.class, null, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.apply(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Date value = function.apply(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }
        writeDate(jsonWriter, false, value.getTime());
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Date value = function.apply(object);

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
