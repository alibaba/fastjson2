package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

final class FieldReaderDateField<T> extends FieldReaderObjectField<T> {
    private ObjectReaderBaseModule.UtilDateImpl dateReader;
    volatile SimpleDateFormat formatter;
    static final AtomicReferenceFieldUpdater<FieldReaderDateField, SimpleDateFormat> FORMATTER_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(FieldReaderDateField.class, SimpleDateFormat.class, "formatter");

    FieldReaderDateField(String fieldName, Class fieldType, int ordinal, long features, String format, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, field);
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
        Date fieldValue;
        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();

            getObjectReader(jsonReader);
            if (dateReader.formatUnixTime) {
                millis *= 1000;
            }

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

        try {
            field.set(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        if (value instanceof String) {
            JSONReader jsonReader = JSONReader.of(
                    JSON.toJSONString(value));
            value = getObjectReader(jsonReader)
                    .readObject(jsonReader);
        } else if (value instanceof Integer) {
            long millis = ((Integer) value).intValue();
            if (dateReader.formatUnixTime) {
                millis *= 1000;
            }
            value = new Date(millis);
        }

        try {
            field.set(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
