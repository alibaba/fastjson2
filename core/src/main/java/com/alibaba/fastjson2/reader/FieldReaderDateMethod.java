package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

final class FieldReaderDateMethod<T> extends FieldReaderObjectMethod<T> {
    final String format;
    volatile SimpleDateFormat formatter;
    static final AtomicReferenceFieldUpdater<FieldReaderDateMethod, SimpleDateFormat> FORMATTER_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(FieldReaderDateMethod.class, SimpleDateFormat.class, "formatter");

    ObjectReader dateReader;

    FieldReaderDateMethod(String fieldName, String format, Class fieldClass, Method method) {
        super(fieldName, fieldClass, method);
        this.format = format;
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
    public void accept(T object, Object value) {
        try {
            method.invoke(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        java.util.Date fieldValue;
        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();
            fieldValue = new java.util.Date(millis);
        } else if (jsonReader.isNull()) {
            jsonReader.readNull();
            fieldValue = null;
        } else {
            if (format != null) {
                SimpleDateFormat formatter = FORMATTER_UPDATER.getAndSet(this, null);
                if (formatter == null) {
                    formatter = new SimpleDateFormat(format);
                }
                try {
                    String str = jsonReader.readString();
                    fieldValue = formatter.parse(str);
                } catch (ParseException e) {
                    throw new JSONException("parse date error, fieldName : " + fieldName, e);
                } finally {
                    FORMATTER_UPDATER.set(this, formatter);
                }
            } else {
                long millis = jsonReader.readMillisFromString();
                fieldValue = new java.util.Date(millis);
            }
        }

        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
