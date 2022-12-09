package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

abstract class FieldReaderImplDate<T>
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

    public FieldReaderImplDate(
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
        } else if (useSimpleFormatter) {
            String str = jsonReader.readString();
            try {
                fieldValue = new SimpleDateFormat(format).parse(str);
            } catch (ParseException e) {
                throw new JSONException(jsonReader.info("parse error : " + str), e);
            }
        } else if (formatISO8601) {
            ZonedDateTime zdt = jsonReader.readZonedDateTime();
            long millis = zdt.toInstant().toEpochMilli();
            fieldValue = new Date(millis);
        } else {
            long millis;
            if (yyyyMMddhhmmss19) {
                if ((jsonReader.features(features) & JSONReader.Feature.SupportSmartMatch.mask) != 0 && jsonReader.isString()) {
                    millis = jsonReader.readMillisFromString();
                } else {
                    millis = jsonReader.readMillis19();
                }
            } else if (format != null) {
                String str = jsonReader.readString();
                if ((formatUnixTime || formatMillis) && IOUtils.isNumber(str)) {
                    millis = Long.parseLong(str);
                    if (formatUnixTime) {
                        millis *= 1000L;
                    }
                } else {
                    DateTimeFormatter formatter = getFormatter(jsonReader.getLocale());
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
    public ObjectReader getObjectReader(JSONReader jsonReader) {
        if (dateReader == null) {
            dateReader = format == null
                    ? ObjectReaderImplDate.INSTANCE
                    : new ObjectReaderImplDate(format, locale);
        }
        return dateReader;
    }

    public ObjectReader getObjectReader(JSONReader.Context context) {
        if (dateReader == null) {
            dateReader = format == null
                    ? ObjectReaderImplDate.INSTANCE
                    : new ObjectReaderImplDate(format, locale);
        }
        return dateReader;
    }

    public abstract void accept(T object, Date value);

    @Override
    public void accept(T object, long value) {
        accept(object, new Date(value));
    }

    @Override
    public void accept(T object, Object value) {
        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                accept(object, null);
                return;
            }

            if ((format == null || formatUnixTime || formatMillis) && IOUtils.isNumber(str)) {
                long millis = Long.parseLong(str);
                if (formatUnixTime) {
                    millis *= 1000L;
                }
                value = new java.util.Date(millis);
            } else {
                String jsonStr = JSON.toJSONString(str);
                value = JSON.parseObject(jsonStr, Date.class, format);
            }
        }

        accept(object, (Date) value);
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
                fieldValue = new java.util.Date(millis);
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
}
