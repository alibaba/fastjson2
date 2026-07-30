package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.sql.SQLException;
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
        return null;
    }

    public static ObjectReader createTimestampReader(Class objectClass, String format, Locale locale) {
        return null;
    }

    public static ObjectReader createDateReader(Class objectClass, String format, Locale locale) {
        return null;
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
        public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
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
