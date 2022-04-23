package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiConsumer;

final class FieldReaderDateFunc<T> extends FieldReaderImpl<T> {
    final Method method;
    final BiConsumer<T, Date> function;
    final String format;
    volatile SimpleDateFormat formatter;
    static final AtomicReferenceFieldUpdater<FieldReaderDateFunc, SimpleDateFormat> FORMATTER_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(FieldReaderDateFunc.class, SimpleDateFormat.class, "formatter");

    ObjectReader dateReader;

    public FieldReaderDateFunc(
            String fieldName
            , Class fieldClass
            , int ordinal
            , long features
            , String format
            , Method method
            , BiConsumer<T, Date> function) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, null);
        this.method = method;
        this.function = function;
        this.format = format;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, Object value) {
        function.accept(object, (Date) value);
    }

    @Override
    public ObjectReader getObjectReader(JSONReader jsonReader) {
        if (dateReader == null) {
            dateReader = format == null
                    ? ObjectReaderBaseModule.UtilDateImpl.INSTANCE
                    : new ObjectReaderBaseModule.UtilDateImpl(format);
        }
        return dateReader;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        function.accept(object,
                (Date) readFieldValue(jsonReader));
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        Date fieldValue;
        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();
            fieldValue = new Date(millis);
        } else if (jsonReader.isNull()) {
            jsonReader.readNull();
            fieldValue = null;
        } else {
            if (format != null) {
                SimpleDateFormat formatter = FORMATTER_UPDATER.getAndSet(this, null);
                if (formatter == null) {
                    formatter = new SimpleDateFormat(format);
                }

                String str = null;
                try {
                    str = jsonReader.readString();
                    fieldValue = formatter.parse(str);
                } catch (ParseException e) {
                    throw new JSONException("parse date error, fieldName : " + fieldName, e);
                } finally {
                    FORMATTER_UPDATER.set(this, formatter);
                }
            } else {
                long millis = jsonReader.readMillisFromString();
                fieldValue = new Date(millis);
            }
        }

        return fieldValue;
    }
}
