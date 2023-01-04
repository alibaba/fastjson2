package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderImplDate;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class JdbcSupport {
    static Class CLASS_CLOB;
    static volatile boolean CLASS_CLOB_ERROR;

    public static ObjectReader createTimeReader(Class objectClass, String format, Locale locale) {
        return new TimeReader(objectClass, format, locale);
    }

    public static ObjectReader createTimestampReader(Class objectClass, String format, Locale locale) {
        return new TimestampReader(objectClass, format, locale);
    }

    public static ObjectReader createDateReader(Class objectClass, String format, Locale locale) {
        return new DateReader(objectClass, format, locale);
    }

    public static ObjectWriter createTimeWriter(String format) {
        if (format == null) {
            return TimeWriter.INSTANCE;
        }

        return new TimeWriter(format);
    }

    public static ObjectWriter createClobWriter(Class objectClass) {
        return new ClobWriter(objectClass);
    }

    public static ObjectWriter createTimestampWriter(Class objectClass, String format) {
        return new TimestampWriter(objectClass, format);
    }

    public static boolean isClob(Class objectClass) {
        if (CLASS_CLOB == null && !CLASS_CLOB_ERROR) {
            try {
                CLASS_CLOB = Class.forName("java.sql.Clob");
            } catch (Throwable e) {
                CLASS_CLOB_ERROR = true;
            }
        }

        return CLASS_CLOB != null && CLASS_CLOB.isAssignableFrom(objectClass);
    }

    static class ClobWriter
            implements ObjectWriter {
        final Class objectClass;
        final Method getCharacterStream;

        public ClobWriter(Class objectClass) {
            if (CLASS_CLOB == null && !CLASS_CLOB_ERROR) {
                try {
                    CLASS_CLOB = Class.forName("java.sql.Clob");
                } catch (Throwable e) {
                    CLASS_CLOB_ERROR = true;
                }
            }

            if (CLASS_CLOB == null) {
                throw new JSONException("class java.sql.Clob not found");
            }

            this.objectClass = objectClass;
            try {
                getCharacterStream = CLASS_CLOB.getMethod("getCharacterStream");
            } catch (Throwable e) {
                throw new JSONException("getMethod getCharacterStream error", e);
            }
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            Reader reader;
            try {
                reader = (Reader) getCharacterStream.invoke(object);
            } catch (Throwable e) {
                throw new JSONException("invoke method getCharacterStream error", e);
            }

            jsonWriter.writeString(reader);
        }
    }

    static class TimeReader
            extends ObjectReaderImplDate {
        final Constructor constructor;
        final Method methodValueOf;

        public TimeReader(Class objectClass, String format, Locale locale) {
            super(format, locale);
            try {
                constructor = objectClass.getConstructor(long.class);
                methodValueOf = objectClass.getMethod("valueOf", String.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("illegal stat", e);
            }
        }

        Object createTime(long millis) {
            try {
                return constructor.newInstance(millis);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new JSONException("create java.sql.Timestamp error", e);
            }
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            return readObject(jsonReader, fieldType, fieldName, features);
        }

        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.isInt()) {
                long millis = jsonReader.readInt64Value();

                if (formatUnixTime) {
                    millis *= 1000;
                }
                return createTime(millis);
            }

            if (jsonReader.readIfNull()) {
                return null;
            }

            if (formatISO8601 || formatMillis) {
                long millis = jsonReader.readMillisFromString();
                return createTime(millis);
            }

            if (formatUnixTime) {
                long seconds = jsonReader.readInt64();
                return createTime(seconds * 1000L);
            }

            long millis;
            if (format != null) {
                DateTimeFormatter formatter = getDateFormatter(jsonReader.getLocale());

                ZonedDateTime zdt;
                if (formatter != null) {
                    String str = jsonReader.readString();
                    if (str.isEmpty()) {
                        return null;
                    }

                    LocalDateTime ldt;
                    if (!formatHasHour) {
                        ldt = LocalDateTime.of(
                                LocalDate.parse(str, formatter),
                                LocalTime.MIN
                        );
                    } else if (!formatHasDay) {
                        ldt = LocalDateTime.of(
                                LocalDate.of(1970, 1, 1),
                                LocalTime.parse(str, formatter)
                        );
                    } else {
                        ldt = LocalDateTime.parse(str, formatter);
                    }
                    zdt = ldt.atZone(jsonReader.getContext().getZoneId());
                } else {
                    zdt = jsonReader.readZonedDateTime();
                }
                millis = zdt.toInstant().toEpochMilli();
            } else {
                String str = jsonReader.readString();
                if ("0000-00-00".equals(str) || "0000-00-00 00:00:00".equals(str)) {
                    return createTime(0);
                }

                if (str.isEmpty() || "null".equals(str)) {
                    return null;
                }

                try {
                    return methodValueOf.invoke(null, str);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new JSONException(jsonReader.info("invoke java.sql.Time.valueOf error"), e);
                }
            }

            return createTime(millis);
        }
    }

    static class TimeWriter
            extends DateTimeCodec
            implements ObjectWriter {
        public static TimeWriter INSTANCE = new TimeWriter(null);

        public TimeWriter(String format) {
            super(format);
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            JSONWriter.Context context = jsonWriter.context;
            if (formatUnixTime || context.isDateFormatUnixTime()) {
                long millis = ((Date) object).getTime();
                long seconds = millis / 1000;
                jsonWriter.writeInt64(seconds);
                return;
            }

            if (formatMillis || context.isDateFormatMillis()) {
                long millis = ((Date) object).getTime();
                jsonWriter.writeInt64(millis);
                return;
            }

            if (formatISO8601 || context.isDateFormatISO8601()) {
                ZoneId zoneId = context.getZoneId();
                long millis = ((Date) object).getTime();
                Instant instant = Instant.ofEpochMilli(millis);
                ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
                int offsetSeconds = zdt.getOffset().getTotalSeconds();

                int year = zdt.getYear();
                int month = zdt.getMonthValue();
                int dayOfMonth = zdt.getDayOfMonth();
                int hour = zdt.getHour();
                int minute = zdt.getMinute();
                int second = zdt.getSecond();
                int nano = 0;
                jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nano, offsetSeconds, true);
                return;
            }

            DateTimeFormatter dateFormatter = null;
            if (format != null && !format.contains("dd")) {
                dateFormatter = getDateFormatter();
            }

            if (dateFormatter == null) {
                String format = context.getDateFormat();
                if (format != null && !format.contains("dd")) {
                    dateFormatter = context.getDateFormatter();
                }
            }

            if (dateFormatter == null) {
                jsonWriter.writeString(object.toString());
                return;
            }

            Date time = (Date) object;

            ZoneId zoneId = context.getZoneId();
            Instant instant = Instant.ofEpochMilli(time.getTime());
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);

            String str = dateFormatter.format(zdt);
            jsonWriter.writeString(str);
        }
    }

    static class TimestampWriter
            extends DateTimeCodec
            implements ObjectWriter {
        final Method methodGetNano;
        final Method methodToLocalDateTime;

        public TimestampWriter(Class objectClass, String format) {
            super(format);
            try {
                methodGetNano = objectClass.getMethod("getNanos");
                methodToLocalDateTime = objectClass.getMethod("toLocalDateTime");
            } catch (NoSuchMethodException e) {
                throw new JSONException("illegal stat", e);
            }
        }

        @Override
        public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            Date date = (Date) object;

            int nanos;
            nanos = getNanos(object);

            if (nanos == 0) {
                jsonWriter.writeMillis(date.getTime());
                return;
            }

            LocalDateTime localDateTime;
            try {
                localDateTime = (LocalDateTime) methodToLocalDateTime.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new JSONException("localDateTime error", e);
            }
            jsonWriter.writeLocalDateTime(localDateTime);
        }

        private int getNanos(Object object) {
            try {
                return (Integer) methodGetNano.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new JSONException("getNanos error", e);
            }
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            JSONWriter.Context ctx = jsonWriter.context;

            Date date = (Date) object;

            if (formatUnixTime || ctx.isDateFormatUnixTime()) {
                long millis = date.getTime();
                jsonWriter.writeInt64(millis / 1000L);
                return;
            }

            ZoneId zoneId = ctx.getZoneId();
            Instant instant = date.toInstant();
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
            int offsetSeconds = zdt.getOffset().getTotalSeconds();

            if ((formatISO8601 || ctx.isDateFormatISO8601()) && (zdt.getNano() % 1000_000 == 0)) {
                int year = zdt.getYear();
                int month = zdt.getMonthValue();
                int dayOfMonth = zdt.getDayOfMonth();
                int hour = zdt.getHour();
                int minute = zdt.getMinute();
                int second = zdt.getSecond();
                int nano = zdt.getNano();
                int millis = nano / 1000_000;
                jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, millis, offsetSeconds, true);
                return;
            }

            DateTimeFormatter dateFormatter = getDateFormatter();
            if (dateFormatter == null) {
                dateFormatter = ctx.getDateFormatter();
            }

            if (dateFormatter == null) {
                if (formatMillis || ctx.isDateFormatMillis()) {
                    long millis = date.getTime();
                    jsonWriter.writeInt64(millis);
                    return;
                }

                int nanos = getNanos(date);

                if (nanos == 0) {
                    jsonWriter.writeInt64(date.getTime());
                    return;
                }

                int year = zdt.getYear();
                int month = zdt.getMonthValue();
                int dayOfMonth = zdt.getDayOfMonth();
                int hour = zdt.getHour();
                int minute = zdt.getMinute();
                int second = zdt.getSecond();
                if (nanos == 0) {
                    jsonWriter.writeDateTime19(year, month, dayOfMonth, hour, minute, second);
                } else if (nanos % 1000_000 == 0) {
                    jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nanos / 1000_000, offsetSeconds, false);
                } else {
                    jsonWriter.writeLocalDateTime(zdt.toLocalDateTime());
                }
            } else {
                String str = dateFormatter.format(zdt);
                jsonWriter.writeString(str);
            }
        }
    }

    static class TimestampReader
            extends ObjectReaderImplDate {
        final Constructor constructor;
        final Method methodSetNanos;
        final Method methodValueOf;

        public TimestampReader(Class objectClass, String format, Locale locale) {
            super(format, locale);
            try {
                constructor = objectClass.getConstructor(long.class);
                methodSetNanos = objectClass.getMethod("setNanos", int.class);
                methodValueOf = objectClass.getMethod("valueOf", LocalDateTime.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("illegal stat", e);
            }
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.isInt()) {
                long millis = jsonReader.readInt64Value();

                if (formatUnixTime) {
                    millis *= 1000;
                }

                return createTimestamp(millis, 0);
            }

            if (jsonReader.readIfNull()) {
                return null;
            }

            return readObject(jsonReader, fieldType, fieldName, features);
        }

        Object createTimestamp(long millis, int nanos) {
            try {
                Object timestamp = constructor.newInstance(millis);
                if (nanos != 0) {
                    methodSetNanos.invoke(timestamp, nanos);
                }
                return timestamp;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new JSONException("create java.sql.Timestamp error", e);
            }
        }

        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.isInt()) {
                long millis = jsonReader.readInt64Value();

                if (formatUnixTime) {
                    millis *= 1000L;
                }

                return createTimestamp(millis, 0);
            }

            if (jsonReader.readIfNull()) {
                return null;
            }

            if (format == null || formatISO8601 || formatMillis) {
                LocalDateTime localDateTime = jsonReader.readLocalDateTime();
                if (localDateTime != null) {
                    try {
                        return methodValueOf.invoke(null, localDateTime);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new JSONException("invoke java.sql.Timestamp.valueOf error", e);
                    }
                }

                if (jsonReader.wasNull()) {
                    return null;
                }

                long millis = jsonReader.readMillisFromString();
                return createTimestamp(millis, 0);
            }

            String str = jsonReader.readString();
            if (str.isEmpty()) {
                return null;
            }

            DateTimeFormatter dateFormatter = getDateFormatter();

            Instant instant;
            if (!formatHasHour) {
                LocalDate localDate = LocalDate.parse(str, dateFormatter);
                LocalDateTime ldt = LocalDateTime.of(localDate, LocalTime.MIN);
                instant = ldt.atZone(jsonReader.getContext().getZoneId()).toInstant();
            } else {
                LocalDateTime ldt = LocalDateTime.parse(str, dateFormatter);
                instant = ldt.atZone(jsonReader.getContext().getZoneId()).toInstant();
            }

            long millis = instant.toEpochMilli();
            int nanos = instant.getNano();

            return createTimestamp(millis, nanos);
        }
    }

    static class DateReader
            extends ObjectReaderImplDate {
        final Constructor constructor;
        final Method methodValueOf;

        public DateReader(Class objectClass, String format, Locale locale) {
            super(format, locale);
            try {
                constructor = objectClass.getConstructor(long.class);
                methodValueOf = objectClass.getMethod("valueOf", LocalDate.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("illegal stat", e);
            }
        }

        Object createDate(long millis) {
            try {
                return constructor.newInstance(millis);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new JSONException("create java.sql.Date error", e);
            }
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            return readObject(jsonReader, fieldType, fieldName, features);
        }

        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.isInt()) {
                long millis = jsonReader.readInt64Value();

                if (formatUnixTime) {
                    millis *= 1000L;
                }

                return createDate(millis);
            }

            if (jsonReader.readIfNull()) {
                return null;
            }

            if (formatUnixTime) {
                if (jsonReader.isString()) {
                    String str = jsonReader.readString();
                    long millis = Long.parseLong(str);
                    millis *= 1000L;
                    return createDate(millis);
                }
            }

            if (format == null || formatISO8601 || formatMillis) {
                LocalDateTime localDateTime = jsonReader.readLocalDateTime();
                if (localDateTime != null) {
                    try {
                        return methodValueOf.invoke(null, localDateTime.toLocalDate());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new JSONException(jsonReader.info("invoke method java.sql.Date.valueOf error"), e);
                    }
                }

                if (jsonReader.wasNull()) {
                    return null;
                }

                long millis = jsonReader.readMillisFromString();
                return createDate(millis);
            }

            String str = jsonReader.readString();
            if (str.isEmpty()) {
                return null;
            }

            DateTimeFormatter dateFormatter = getDateFormatter();

            Instant instant;
            if (format != null && !formatHasHour) {
                LocalDate localDate = LocalDate.parse(str, dateFormatter);
                LocalDateTime ldt = LocalDateTime.of(localDate, LocalTime.MIN);
                instant = ldt.atZone(jsonReader.getContext().getZoneId()).toInstant();
            } else {
                LocalDateTime ldt = LocalDateTime.parse(str, dateFormatter);
                instant = ldt.atZone(jsonReader.getContext().getZoneId()).toInstant();
            }

            return createDate(
                    instant.toEpochMilli()
            );
        }
    }
}
