package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.function.Function;

final class FieldWriterCalendarFunc<T> extends FieldWriterDate<T> {
    final Method method;
    final Function<T, Calendar> function;

    FieldWriterCalendarFunc(String fieldName, int ordinal, long features, String dateTimeFormat, Method method, Function<T, Calendar> function) {
        super(fieldName, ordinal, features, dateTimeFormat, Calendar.class, Calendar.class);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    public Object getFieldValue(T object) {
        return function.apply(object);
    }

    public void writeValue(JSONWriter jsonWriter, T object) {
        Calendar value = function.apply(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        long millis = value.getTimeInMillis();
        writeDate(jsonWriter, false, millis);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T o) {
        Calendar value = function.apply(o);

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

        writeDate(jsonWriter, value.getTimeInMillis());
        return true;
    }
}
