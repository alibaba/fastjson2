package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Method;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderDateFunc<T>
        extends FieldReaderImpl<T> {
    final Method method;
    final BiConsumer<T, Date> function;
    DateTimeFormatter formatter;

    ObjectReader dateReader;
    boolean formatUnixtime;
    boolean formatMillis;

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
        this.formatUnixtime = "unixtime".equals(format);
        this.formatMillis = "millis".equals(format);
    }

    @Override
    public Method getMethod() {
        return method;
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
            if (formatUnixtime) {
                millis *= 1000L;
            }
            fieldValue = new Date(millis);
        } else if (jsonReader.isNull()) {
            jsonReader.readNull();
            fieldValue = null;
        } else {
            long millis;
            if (format != null) {
                String str = jsonReader.readString();
                if ((formatUnixtime || formatMillis) && IOUtils.isNumber(str)) {
                    millis = Long.parseLong(str);
                    if (formatUnixtime) {
                        millis *= 1000L;
                    }
                } else {
                    if (formatter == null) {
                        String format = this.format.replaceAll("aa", "a");
                        formatter = DateTimeFormatter.ofPattern(format);
                    }
                    LocalDateTime ldt;
                    if (format.indexOf("HH") == -1) {
                        ldt = LocalDateTime.of(LocalDate.parse(str, formatter), LocalTime.MIN);
                    } else {
                        ldt = LocalDateTime.parse(str, formatter);
                    }

                    ZonedDateTime zdt = ldt.atZone(jsonReader.getContext().getZoneId());
                    millis = zdt.toInstant().toEpochMilli();
                }
            } else {
                millis = jsonReader.readMillisFromString();
            }
            fieldValue = new Date(millis);
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
