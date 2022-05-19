package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Method;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderDateFunc<T> extends FieldReaderImpl<T> {
    final Method method;
    final BiConsumer<T, Date> function;
    DateTimeFormatter formatter;

    ObjectReader dateReader;

    public FieldReaderDateFunc(
            String fieldName,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Date defaultValue,
            JSONSchema schema,
            Method method,
            BiConsumer<T, Date> function) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, schema);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, Object value) {
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

        function.accept(object, (Date) value);
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
                if (formatter == null) {
                    String format = this.format.replaceAll("aa", "a");
                    formatter = DateTimeFormatter.ofPattern(format);
                }
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
                fieldValue = new Date(millis);
            }
        }

        return fieldValue;
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
