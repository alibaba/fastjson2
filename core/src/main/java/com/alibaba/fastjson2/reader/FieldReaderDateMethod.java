package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

final class FieldReaderDateMethod<T> extends FieldReaderObjectMethod<T> {
    DateTimeFormatter formatter;
    ObjectReaderBaseModule.UtilDateImpl dateReader;

    FieldReaderDateMethod(String fieldName, Class fieldClass, int ordinal, long features, String format, Locale locale, Method method) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, null, method);
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
            if (value instanceof String) {
                String str = (String) value;

                if (format != null) {
                    DateTimeFormatter formatter = getFormatter(null);
                    LocalDateTime ldt;
                    if (format.indexOf("HH") == -1) {
                        ldt = LocalDateTime.of(LocalDate.parse(str, formatter), LocalTime.MIN);
                    } else {
                        ldt = LocalDateTime.parse(str, formatter);
                    }

                    ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
                    long millis = zdt.toInstant().toEpochMilli();
                    value = new java.util.Date(millis);
                }
            }

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
                Locale locale = jsonReader.getContext().getLocale();
                DateTimeFormatter formatter = getFormatter(locale);
                String str = jsonReader.readString();

                LocalDateTime ldt;
                if (format.indexOf("HH") == -1) {
                    ldt = LocalDateTime.of(LocalDate.parse(str, formatter), LocalTime.MIN);
                } else {
                    ldt = LocalDateTime.parse(str, formatter);
                }

                ZonedDateTime zdt = ldt.atZone(jsonReader.getContext().getZoneId());
                long millis = zdt.toInstant().toEpochMilli();
                fieldValue = new java.util.Date(millis);
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

    private DateTimeFormatter getFormatter(Locale locale) {
        if (formatter != null && locale == null) {
            return formatter;
        }

        String format = this.format.replaceAll("aa", "a");

        if (locale != null && locale != Locale.getDefault()) {
            return DateTimeFormatter.ofPattern(format, locale);
        }

        if (this.locale != null) {
            return formatter = DateTimeFormatter.ofPattern(format, this.locale);
        }

        return formatter = DateTimeFormatter.ofPattern(format);
    }
}
