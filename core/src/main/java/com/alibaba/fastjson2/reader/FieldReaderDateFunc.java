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
    final boolean formatISO8601;
    final boolean formatUnixTime;
    final boolean formatMillis;
    final boolean formatHasDay;
    final boolean formatHasHour;

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

        boolean formatUnixTime = false, formatISO8601 = false, formatMillis = false, hasDay = false, hasHour = false;
        if (format != null) {
            switch (format) {
                case "unixtime":
                    formatUnixTime = true;
                    break;
                case "iso8601":
                    formatISO8601 = true;
                    break;
                case "millis":
                    formatMillis = true;
                    break;
                default:
                    hasDay = format.indexOf('d') != -1;
                    hasHour = format.indexOf('H') != -1
                            || format.indexOf('h') != -1
                            || format.indexOf('K') != -1
                            || format.indexOf('k') != -1;
                    break;
            }
        }
        this.formatUnixTime = formatUnixTime;
        this.formatMillis = formatMillis;
        this.formatISO8601 = formatISO8601;

        this.formatHasDay = hasDay;
        this.formatHasHour = hasHour;
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
            if ((format == null || formatUnixTime || formatMillis) && IOUtils.isNumber(str)) {
                millis = Long.parseLong(str);
                if (formatUnixTime) {
                    millis *= 1000L;
                }
            } else {
                DateTimeFormatter formatter = getFormatter(null);
                LocalDateTime ldt;
                if (!formatHasHour) {
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
            if (formatUnixTime) {
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
                if ((formatUnixTime || formatMillis) && IOUtils.isNumber(str)) {
                    millis = Long.parseLong(str);
                    if (formatUnixTime) {
                        millis *= 1000L;
                    }
                } else {
                    if (formatter == null) {
                        String format = this.format.replaceAll("aa", "a");
                        formatter = DateTimeFormatter.ofPattern(format);
                    }
                    LocalDateTime ldt;
                    if (!formatHasHour) {
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
