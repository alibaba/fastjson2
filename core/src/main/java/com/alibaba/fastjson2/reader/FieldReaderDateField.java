package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Field;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

final class FieldReaderDateField<T>
        extends FieldReaderObjectField<T> {
    private ObjectReaderImplDate dateReader;
    DateTimeFormatter formatter;
    final Locale locale;
    final boolean useSimpleFormatter;

    boolean formatUnixtime;
    boolean formatMillis;

    FieldReaderDateField(String fieldName, Class fieldType, int ordinal, long features, String format, Locale locale, Date defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
        this.locale = locale;
        this.useSimpleFormatter = "yyyyMMddHHmmssSSSZ".equals(format);
        this.formatUnixtime = "unixtime".equals(format);
        this.formatMillis = "millis".equals(format);
    }

    @Override
    public ObjectReader getObjectReader(JSONReader jsonReader) {
        if (dateReader == null) {
            dateReader = format == null
                    ? ObjectReaderImplDate.INSTANCE
                    : new ObjectReaderImplDate(format, locale);
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
                getObjectReader(jsonReader);
                if (dateReader.useSimpleFormatter) {
                    fieldValue = (Date) dateReader.readObject(jsonReader, features);
                } else {
                    String str = jsonReader.readString();

                    Locale locale = jsonReader.getContext().getLocale();
                    DateTimeFormatter formatter = getFormatter(locale);
                    LocalDateTime ldt;
                    if (format.indexOf("HH") == -1) {
                        ldt = LocalDateTime.of(LocalDate.parse(str, formatter), LocalTime.MIN);
                    } else {
                        ldt = LocalDateTime.parse(str, formatter);
                    }
                    ZonedDateTime zdt = ldt.atZone(jsonReader.getContext().getZoneId());
                    long millis = zdt.toInstant().toEpochMilli();
                    fieldValue = new java.util.Date(millis);
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
            String str = (String) value;

            long millis;
            if ((format == null || formatUnixtime || formatMillis) && IOUtils.isNumber(str)) {
                millis = Long.parseLong(str);
                if (formatUnixtime) {
                    millis *= 1000L;
                }
            } else {
                DateTimeFormatter formatter = getFormatter(null);
                LocalDateTime ldt;
                if (format.indexOf("HH") == -1) {
                    ldt = LocalDateTime.of(LocalDate.parse(str, formatter), LocalTime.MIN);
                } else {
                    ldt = LocalDateTime.parse(str, formatter);
                }

                ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
                millis = zdt.toInstant().toEpochMilli();
            }
            value = new java.util.Date(millis);
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
