package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

abstract class FieldReaderDateTimeCodec<T>
        extends FieldReader<T> {
    DateTimeFormatter formatter;

    ObjectReader dateReader;
    final boolean useSimpleFormatter;
    final boolean formatISO8601;
    final boolean formatUnixTime;
    final boolean formatMillis;
    final boolean formatHasDay;
    final boolean formatHasHour;
    final boolean yyyyMMddhhmmss19;

    public FieldReaderDateTimeCodec(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            Field field
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field);
        this.useSimpleFormatter = "yyyyMMddHHmmssSSSZ".equals(format);
        this.yyyyMMddhhmmss19 = "yyyy-MM-dd HH:mm:ss".equals(format);

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
    public Object readFieldValue(JSONReader jsonReader) {
        Object fieldValue;
        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();
            if (formatUnixTime) {
                millis *= 1000L;
            }
            fieldValue = apply(millis);
        } else if (jsonReader.isNull()) {
            jsonReader.readNull();
            return null;
        } else if (useSimpleFormatter) {
            String str = jsonReader.readString();
            try {
                Date date = new SimpleDateFormat(format).parse(str);
                fieldValue = apply(date);
            } catch (ParseException e) {
                throw new JSONException(jsonReader.info("parse error : " + str), e);
            }
        } else if (formatISO8601) {
            ZonedDateTime zdt = jsonReader.readZonedDateTime();
            fieldValue = apply(zdt);
        } else {
            long millis;
            if (yyyyMMddhhmmss19) {
                if ((jsonReader.features(features) & JSONReader.Feature.SupportSmartMatch.mask) != 0 && jsonReader.isString()) {
                    millis = jsonReader.readMillisFromString();
                } else {
                    millis = jsonReader.readMillis19();
                }
                return apply(millis);
            } else if (format != null) {
                String str = jsonReader.readString();
                if ((formatUnixTime || formatMillis) && IOUtils.isNumber(str)) {
                    millis = Long.parseLong(str);
                    if (formatUnixTime) {
                        millis *= 1000L;
                    }
                    return apply(millis);
                } else {
                    boolean number = false;
                    switch (format) {
                        case "yyyy-MM-dd":
                        case "yyyy-mm-dd":
                        case "yyyy-MM-dd HH:mm:ss":
                        case "yyyy-MM-dd'T'HH:mm:ss":
                            int subIndex = str.indexOf('-');
                            number = (subIndex == -1 || subIndex == 0) && TypeUtils.isInteger(str);
                            break;
                        default:
                            break;
                    }
                    if (number) {
                        millis = Long.parseLong(str);
                        if (formatUnixTime) {
                            millis *= 1000L;
                        }
                        return apply(millis);
                    }

                    DateTimeFormatter formatter = getFormatter(jsonReader.getLocale());
                    LocalDateTime ldt;
                    if (!formatHasHour) {
                        LocalDate localDate;
                        localDate = LocalDate.parse(str, formatter);
                        ldt = LocalDateTime.of(localDate, LocalTime.MIN);
                    } else {
                        ldt = LocalDateTime.parse(str, formatter);
                    }

                    ZonedDateTime zdt = ldt.atZone(jsonReader.getContext().getZoneId());
                    fieldValue = apply(zdt);
                }
            } else {
                millis = jsonReader.readMillisFromString();
                fieldValue = apply(millis);
            }
        }

        return fieldValue;
    }

    protected DateTimeFormatter getFormatter(Locale locale) {
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

    @Override
    public abstract ObjectReader getObjectReader(JSONReader jsonReader);

    public abstract ObjectReader getObjectReader(JSONReader.Context context);

    protected abstract void accept(T object, Date value);

    protected abstract void acceptNull(T object);

    protected abstract void accept(T object, Instant value);

    protected abstract void accept(T object, LocalDateTime ldt);

    protected abstract void accept(T object, ZonedDateTime zdt);

    protected abstract Object apply(Date value);

    protected abstract Object apply(Instant value);

    protected abstract Object apply(ZonedDateTime zdt);

    protected abstract Object apply(LocalDateTime zdt);

    protected abstract Object apply(long millis);

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            acceptNull(object);
            return;
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                acceptNull(object);
                return;
            }

            if ((format == null || formatUnixTime || formatMillis) && IOUtils.isNumber(str)) {
                long millis = Long.parseLong(str);
                if (formatUnixTime) {
                    millis *= 1000L;
                }
                accept(object, millis);
                return;
            } else {
                value = DateUtils.parseDate(str, format, DateUtils.DEFAULT_ZONE_ID);
            }
        }

        if (value instanceof Date) {
            accept(object, (Date) value);
        } else if (value instanceof Instant) {
            accept(object, (Instant) value);
        } else if (value instanceof Long) {
            accept(object, ((Long) value).longValue());
        } else if (value instanceof LocalDateTime) {
            accept(object, (LocalDateTime) value);
        } else if (value instanceof ZonedDateTime) {
            accept(object, (ZonedDateTime) value);
        } else {
            throw new JSONException("not support value " + value.getClass());
        }
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        java.util.Date fieldValue;
        try {
            if (jsonReader.isInt() && (format == null || formatUnixTime || formatMillis)) {
                long millis = jsonReader.readInt64Value();
                if (formatUnixTime) {
                    millis *= 1000L;
                }
                accept(object, millis);
                return;
            } else if (jsonReader.isNull()) {
                jsonReader.readNull();
                fieldValue = null;
            } else if (useSimpleFormatter) {
                String str = jsonReader.readString();
                try {
                    fieldValue = new SimpleDateFormat(format).parse(str);
                } catch (ParseException e) {
                    throw new JSONException(jsonReader.info("parse error : " + str), e);
                }
            } else {
                if (format != null) {
                    String str = jsonReader.readString();
                    if (str.isEmpty() || "null".equals(str)) {
                        fieldValue = null;
                    } else {
                        long millis;
                        if ((formatUnixTime || formatMillis) && IOUtils.isNumber(str)) {
                            millis = Long.parseLong(str);
                            if (formatUnixTime) {
                                millis *= 1000L;
                            }
                        } else {
                            Locale locale = jsonReader.getContext().getLocale();
                            DateTimeFormatter formatter = getFormatter(locale);

                            LocalDateTime ldt;
                            if (!formatHasHour) {
                                ldt = LocalDateTime.of(LocalDate.parse(str, formatter), LocalTime.MIN);
                            } else {
                                try {
                                    ldt = LocalDateTime.parse(str, formatter);
                                } catch (DateTimeParseException e) {
                                    if (jsonReader.isSupportSmartMatch(features)) {
                                        ldt = DateUtils.parseZonedDateTime(str)
                                                .toLocalDateTime();
                                    } else {
                                        throw e;
                                    }
                                }
                            }

                            ZonedDateTime zdt = ldt.atZone(jsonReader.getContext().getZoneId());
                            millis = zdt.toInstant().toEpochMilli();
                        }
                        fieldValue = new java.util.Date(millis);
                    }
                } else if (jsonReader.nextIfNullOrEmptyString()) {
                    fieldValue = null;
                } else {
                    long millis = jsonReader.readMillisFromString();
                    fieldValue = new java.util.Date(millis);
                }
            }
        } catch (Exception e) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                fieldValue = null;
            } else {
                throw e;
            }
        }

        accept(object, fieldValue);
    }

    public boolean supportAcceptType(Class valueClass) {
        return valueClass == Date.class
                || valueClass == String.class;
    }
}
