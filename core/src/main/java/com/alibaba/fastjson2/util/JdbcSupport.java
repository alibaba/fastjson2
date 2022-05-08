package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.codec.DateTimeCodec;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderBaseModule;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class JdbcSupport {
    public static ObjectReader createTimeReader(String format) {
        if (format == null || format.isEmpty()) {
            return TimeReader.INSTANCE;
        }

        return new TimeReader(format);
    }

    public static ObjectReader createTimestampReader(String format) {
        if (format == null || format.isEmpty()) {
            return TimestampReader.INSTANCE;
        }

        return new TimestampReader(format);
    }

    public static ObjectReader createDateReader(String format) {
        if (format == null || format.isEmpty()) {
            return DateReader.INSTANCE;
        }

        return new DateReader(format);
    }

    public static ObjectWriter createTimeWriter(String format) {
        if (format == null) {
            return TimeWriter.INSTANCE;
        }

        return new TimeWriter(format);
    }

    public static ObjectWriter createTimestampWriter(String format) {
        if (format == null) {
            return TimestampWriter.INSTANCE;
        }

        return new TimestampWriter(format);
    }

    static class TimeReader extends ObjectReaderBaseModule.UtilDateImpl {
        static final TimeReader INSTANCE = new TimeReader(null);

        public TimeReader(String format) {
            super(format);
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            return readObject(jsonReader, features);
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            if (jsonReader.isInt()) {
                long millis = jsonReader.readInt64Value();

                if (formatUnixTime) {
                    millis *= 1000;
                }
                return new java.sql.Time(millis);
            }

            if (jsonReader.readIfNull()) {
                return null;
            }

            if (formatISO8601 || formatMillis) {
                long millis = jsonReader.readMillisFromString();
                return new java.sql.Time(millis);
            }

            if (formatUnixTime) {
                long seconds = jsonReader.readInt64();
                return new java.sql.Time(seconds * 1000L);
            }

            long millis;
            if (format != null) {
                SimpleDateFormat formatter = FORMATTER_UPDATER.getAndSet(this, null);
                if (formatter == null) {
                    formatter = new SimpleDateFormat(format);
                }

                String str = null;
                try {
                    str = jsonReader.readString();
                    java.util.Date utilDate = formatter.parse(str);
                    millis = utilDate.getTime();
                } catch (ParseException e) {
                    throw new JSONException("parse date error, format " + format + ", input " + str, e);
                } finally {
                    FORMATTER_UPDATER.set(this, formatter);
                }
            } else {
                String str = jsonReader.readString();
                if ("0000-00-00".equals(str)) {
                    return new java.sql.Time(0);
                }
                return java.sql.Time.valueOf(str);
            }

            return new java.sql.Time(millis);
        }
    }

    static class TimeWriter extends DateTimeCodec implements ObjectWriter {
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

            JSONWriter.Context context = jsonWriter.getContext();
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
                long millis = ((java.sql.Time) object).getTime();
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
                jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nano, offsetSeconds);
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

            java.sql.Time time = (java.sql.Time) object;

            ZoneId zoneId = context.getZoneId();
            Instant instant = Instant.ofEpochMilli(time.getTime());
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);

            String str = dateFormatter.format(zdt);
            jsonWriter.writeString(str);
        }
    }

    static class TimestampWriter extends DateTimeCodec implements ObjectWriter {
        static final TimestampWriter INSTANCE = new TimestampWriter(null);

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
            int nanos = date.getNanos();

            if (nanos == 0) {
                jsonWriter.writeMillis(date.getTime());
                return;
            }

            jsonWriter.writeLocalDateTime(date.toLocalDateTime());
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            JSONWriter.Context ctx = jsonWriter.getContext();

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

            if (formatISO8601 || ctx.isDateFormatISO8601()) {
                int year = zdt.getYear();
                int month = zdt.getMonthValue();
                int dayOfMonth = zdt.getDayOfMonth();
                int hour = zdt.getHour();
                int minute = zdt.getMinute();
                int second = zdt.getSecond();
                int nano = zdt.getNano() / 1000_000;
                jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nano, offsetSeconds);
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
                    jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nanos / 1000_000, offsetSeconds);
                } else {
                    jsonWriter.writeLocalDateTime(zdt.toLocalDateTime());
                }
            } else {
                String str = dateFormatter.format(zdt);
                jsonWriter.writeString(str);
            }
        }
    }

    static class TimestampReader extends ObjectReaderBaseModule.UtilDateImpl {
        public static TimestampReader INSTANCE = new TimestampReader(null);

        public TimestampReader(String format) {
            super(format);
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            if (jsonReader.isInt()) {
                long millis = jsonReader.readInt64Value();

                if (formatUnixTime) {
                    millis *= 1000;
                }

                return new Timestamp(millis);
            }

            if (jsonReader.readIfNull()) {
                return null;
            }

            return readObject(jsonReader, features);
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            if (jsonReader.isInt()) {
                long millis = jsonReader.readInt64Value();

                if (formatUnixTime) {
                    millis *= 1000L;
                }

                return new java.sql.Timestamp(millis);
            }

            if (jsonReader.readIfNull()) {
                return null;
            }

            if (format == null || formatISO8601 || formatMillis) {
                LocalDateTime localDateTime = jsonReader.readLocalDateTime();
                if (localDateTime != null) {
                    return java.sql.Timestamp.valueOf(localDateTime);
                }

                long millis = jsonReader.readMillisFromString();
                return new java.sql.Timestamp(millis);
            }

            String str = jsonReader.readString();

            DateTimeFormatter dateFormatter = getDateFormatter();

            Instant instant;
            if (format.indexOf("HH") == -1) {
                LocalDate localDate = LocalDate.parse(str, dateFormatter);
                LocalDateTime ldt = LocalDateTime.of(localDate, LocalTime.MIN);
                instant = ldt.atZone(jsonReader.getContext().getZoneId()).toInstant();
            } else {
                LocalDateTime ldt = LocalDateTime.parse(str, dateFormatter);
                instant = ldt.atZone(jsonReader.getContext().getZoneId()).toInstant();
            }

            long millis = instant.toEpochMilli();
            Timestamp timestamp = new Timestamp(millis);

            int nanos = instant.getNano();
            if (nanos != 0) {
                timestamp.setNanos(nanos);
            }

            return timestamp;
        }
    }

    static class DateReader extends ObjectReaderBaseModule.UtilDateImpl {
        public static DateReader INSTANCE = new DateReader(null);

        public DateReader(String format) {
            super(format);
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            return readObject(jsonReader, features);
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
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

                long millis = jsonReader.readMillisFromString();
                return new java.sql.Date(millis);
            }

            String str = jsonReader.readString();

            DateTimeFormatter dateFormatter = getDateFormatter();

            Instant instant;
            if (format != null && format.indexOf("HH") == -1) {
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
}
