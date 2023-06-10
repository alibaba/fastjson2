package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.BiConsumer;
import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Locale;

final class FieldReaderDate<T>
        extends FieldReaderDateTimeCodec<T> {
    final BiConsumer<T, Date> function;

    public FieldReaderDate(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            Field field,
            Method method,
            BiConsumer<T, Date> function
    ) {
        super(
                fieldName,
                fieldType,
                fieldClass,
                ordinal,
                features,
                format,
                locale,
                defaultValue,
                method,
                field,
                ObjectReaderImplDate.of(format, locale)
        );
        this.function = function;
    }

    @Override
    protected void acceptNull(T object) {
        accept(object, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Date date = (Date) dateReader.readObject(jsonReader, fieldType, fieldName, features);
        accept(object, date);
    }

    @Override
    protected void accept(T object, Date value) {
        if (function != null) {
            function.accept(object, value);
            return;
        }

        if (object == null) {
            throw new JSONException("set " + fieldName + " error, object is null");
        }

        if (method != null) {
            try {
                method.invoke(object, value);
            } catch (Exception e) {
                throw new JSONException("set " + fieldName + " error", e);
            }
            return;
        }

        if (fieldOffset != -1) {
            JDKUtils.UNSAFE.putObject(object, fieldOffset, value);
            return;
        }

        try {
            field.set(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, long value) {
        accept(object, new Date(value));
    }

    @Override
    protected Object apply(Date value) {
        return value;
    }

    @Override
    protected Object apply(long millis) {
        return new Date(millis);
    }
}
