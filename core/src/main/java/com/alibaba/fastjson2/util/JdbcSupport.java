package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.codec.DateTimeCodec;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderImplDate;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class JdbcSupport {
    static Class CLASS_STRUCT;
    static volatile boolean CLASS_STRUCT_ERROR;

    static Class CLASS_CLOB;
    static volatile boolean CLASS_CLOB_ERROR;

    static Constructor CONSTRUCTOR_TIMESTAMP;
    static volatile boolean CONSTRUCTOR_TIMESTAMP_ERROR;

    static Constructor CONSTRUCTOR_DATE;
    static volatile boolean CONSTRUCTOR_DATE_ERROR;

    static Constructor CONSTRUCTOR_TIME;
    static volatile boolean CONSTRUCTOR_TIME_ERROR;

    public static ObjectReader createTimeReader(Class objectClass, String format, Locale locale) {
        return new TimeReader(format, locale);
    }

    public static ObjectReader createTimestampReader(Class objectClass, String format, Locale locale) {
        return new TimestampReader(format, locale);
    }

    public static ObjectReader createDateReader(Class objectClass, String format, Locale locale) {
        return new DateReader(format, locale);
    }

    public static ObjectWriter createTimeWriter(String format) {
        if (format == null) {
            return TimeWriter.INSTANCE;
        }

        return new TimeWriter(format);
    }

    public static Object createTimestamp(long millis) {
        if (CONSTRUCTOR_TIMESTAMP == null && !CONSTRUCTOR_TIMESTAMP_ERROR) {
            try {
                Class<?> clazz = Class.forName("java.sql.Timestamp");
                CONSTRUCTOR_TIMESTAMP = clazz.getConstructor(long.class);
            } catch (Throwable e) {
                CONSTRUCTOR_TIMESTAMP_ERROR = true;
            }
        }

        if (CONSTRUCTOR_TIMESTAMP == null) {
            throw new JSONException("class java.sql.Timestamp not found");
        }

        try {
            return CONSTRUCTOR_TIMESTAMP.newInstance(millis);
        } catch (Exception e) {
            throw new JSONException("create java.sql.Timestamp error", e);
        }
    }

    public static Object createDate(long millis) {
        if (CONSTRUCTOR_DATE == null && !CONSTRUCTOR_DATE_ERROR) {
            try {
                Class<?> clazz = Class.forName("java.sql.Date");
                CONSTRUCTOR_DATE = clazz.getConstructor(long.class);
            } catch (Throwable e) {
                CONSTRUCTOR_DATE_ERROR = true;
            }
        }

        if (CONSTRUCTOR_DATE == null) {
            throw new JSONException("class java.sql.Date not found");
        }

        try {
            return CONSTRUCTOR_DATE.newInstance(millis);
        } catch (Exception e) {
            throw new JSONException("create java.sql.Date error", e);
        }
    }

    public static Object createTime(long millis) {
        if (CONSTRUCTOR_TIME == null && !CONSTRUCTOR_TIME_ERROR) {
            try {
                Class<?> clazz = Class.forName("java.sql.Time");
                CONSTRUCTOR_TIME = clazz.getConstructor(long.class);
            } catch (Throwable e) {
                CONSTRUCTOR_TIME_ERROR = true;
            }
        }

        if (CONSTRUCTOR_TIME == null) {
            throw new JSONException("class java.sql.Time not found");
        }

        try {
            return CONSTRUCTOR_TIME.newInstance(millis);
        } catch (Exception e) {
            throw new JSONException("create java.sql.Time error", e);
        }
    }

    public static ObjectWriter createClobWriter(Class objectClass) {
        return new ClobWriter(objectClass);
    }

    public static ObjectWriter createTimestampWriter(Class objectClass, String format) {
        return new TimestampWriter(format);
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
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            java.sql.Clob clob = (java.sql.Clob) object;
            Reader reader;
            try {
                reader = clob.getCharacterStream();
            } catch (SQLException e) {
                throw new JSONException("Clob.getCharacterStream error", e);
            }
            jsonWriter.writeString(reader);
        }
    }

    static class TimeReader
            extends ObjectReaderImplDate {
        public TimeReader(String format, Locale locale) {
            super(format, locale);
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
                return new Time(millis);
            }

            if (jsonReader.readIfNull()) {
                return null;
            }

            if (formatISO8601 || formatMillis) {
                long millis = jsonReader.readMillisFromString();
                return new Time(millis);
            }

            if (formatUnixTime) {
                long seconds = jsonReader.readInt64();
                return new Time(seconds * 1000L);
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
                    millis = 0;
                } else {
                    if (str.length() == 9 && str.charAt(8) == 'Z') {
                        LocalTime localTime = DateUtils.parseLocalTime(
                                str.charAt(0),
                                str.charAt(1),
                                str.charAt(2),
                                str.charAt(3),
                                str.charAt(4),
                                str.charAt(5),
                                str.charAt(6),
                                str.charAt(7)
                        );
                        millis = LocalDateTime.of(DateUtils.LOCAL_DATE_19700101, localTime)
                                .atZone(DateUtils.DEFAULT_ZONE_ID)
                                .toInstant()
                                .toEpochMilli();
                    } else {
                        if (str.isEmpty() || "null".equals(str)) {
                            return null;
                        }
                        return Time.valueOf(str);
                    }
                }
            }

            return new Time(millis);
        }
    }

    static class TimeWriter
            extends DateTimeCodec
            implements ObjectWriter {
        public static final TimeWriter INSTANCE = new TimeWriter(null);

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
        public TimestampWriter(String format) {
            super(format);
        }

        @Override
        public void writeJSONB(JSONWriterJSONB jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            Timestamp date = (Timestamp) object;

            if (format != null) {
                write(jsonWriter, object, fieldName, fieldType, features);
                return;
            }

            LocalDateTime localDateTime = date.toLocalDateTime();
            jsonWriter.writeLocalDateTime(localDateTime);
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            JSONWriter.Context ctx = jsonWriter.context;

            Timestamp date = (Timestamp) object;

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

                int nanos = date.getNanos();

                int year = zdt.getYear();
                int month = zdt.getMonthValue();
                int dayOfMonth = zdt.getDayOfMonth();
                int hour = zdt.getHour();
                int minute = zdt.getMinute();
                int second = zdt.getSecond();
                if (nanos % 1000_000 == 0) {
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
        public TimestampReader(String format, Locale locale) {
            super(format, locale);
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

            byte type = jsonReader.getType();
            if (type == JSONB.Constants.BC_LOCAL_DATETIME) {
                LocalDateTime ldt = jsonReader.readLocalDateTime();
                Instant instant = ldt.atZone(jsonReader.getContext().getZoneId()).toInstant();
                return createTimestamp(
                        instant.toEpochMilli(),
                        instant.getNano());
            }

            return readObject(jsonReader, fieldType, fieldName, features);
        }

        Object createTimestamp(long millis, int nanos) {
            Timestamp timestamp = new Timestamp(millis);
            if (nanos != 0) {
                timestamp.setNanos(nanos);
            }
            return timestamp;
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

            if (jsonReader.nextIfNullOrEmptyString()) {
                return null;
            }

            if (format == null || formatISO8601 || formatMillis) {
                LocalDateTime localDateTime = jsonReader.readLocalDateTime();
                if (localDateTime != null) {
                    return Timestamp.valueOf(localDateTime);
                }

                if (jsonReader.wasNull()) {
                    return null;
                }

                long millis = jsonReader.readMillisFromString();
                if (millis == 0 && jsonReader.wasNull()) {
                    return null;
                }
                return new Timestamp(millis);
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
        public DateReader(String format, Locale locale) {
            super(format, locale);
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

                return new java.sql.Date(millis);
            }

            if (jsonReader.readIfNull()) {
                return null;
            }

            if (formatUnixTime) {
                if (jsonReader.isString()) {
                    String str = jsonReader.readString();
                    long millis = Long.parseLong(str);
                    millis *= 1000L;
                    return new java.sql.Date(millis);
                }
            }

            if (format == null || formatISO8601 || formatMillis) {
                LocalDateTime localDateTime = jsonReader.readLocalDateTime();
                if (localDateTime != null) {
                    return java.sql.Date.valueOf(localDateTime.toLocalDate());
                }

                if (jsonReader.wasNull()) {
                    return null;
                }

                long millis = jsonReader.readMillisFromString();
                if (millis == 0 && jsonReader.wasNull()) {
                    return null;
                }
                return new java.sql.Date(millis);
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

            return new java.sql.Date(
                    instant.toEpochMilli()
            );
        }
    }

    public static boolean isStruct(Class objectClass) {
        if (CLASS_STRUCT == null && !CLASS_STRUCT_ERROR) {
            try {
                CLASS_STRUCT = Class.forName("java.sql.Struct");
            } catch (Throwable e) {
                CLASS_STRUCT_ERROR = true;
            }
        }

        return CLASS_STRUCT != null && CLASS_STRUCT.isAssignableFrom(objectClass);
    }
}
