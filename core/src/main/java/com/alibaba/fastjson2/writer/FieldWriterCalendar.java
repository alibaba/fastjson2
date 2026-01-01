package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.function.Function;

final class FieldWriterCalendar<T>
        extends FieldWriterDate<T> {
    FieldWriterCalendar(
            String fieldName,
            int ordinal,
            long features,
            String dateTimeFormat,
            String label,
            Field field,
            Method method,
            Function<T, Calendar> function
    ) {
        super(fieldName, ordinal, features, dateTimeFormat, label, Calendar.class, Calendar.class, field, method, function);
    }

    @Override
    public Object getFieldValue(T object) {
        return propertyAccessor.getObject(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Calendar value = (Calendar) propertyAccessor.getObject(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        long millis = value.getTimeInMillis();
        writeDate(jsonWriter, false, millis);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T o) {
        Calendar value = (Calendar) propertyAccessor.getObject(o);

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
