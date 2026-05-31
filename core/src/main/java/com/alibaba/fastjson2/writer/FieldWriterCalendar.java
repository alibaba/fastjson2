package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Locale;

import static com.alibaba.fastjson2.JSONWriter.MASK_IGNORE_ERROR_GETTER;
import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_MAP_NULL_VALUE;

final class FieldWriterCalendar<T>
        extends FieldWriterDate<T> {
    FieldWriterCalendar(
            String name,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Object function
    ) {
        super(name, ordinal, features, format, locale, label, fieldType, fieldClass, field, method, function);
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
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        Calendar value;
        try {
            value = (Calendar) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            if ((features & MASK_WRITE_MAP_NULL_VALUE) != 0) {
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

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (format == null) {
            return dateWriter = ObjectWriterImplCalendar.INSTANCE;
        }
        return dateWriter = new ObjectWriterImplCalendar(format, null);
    }
}
