package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;
import com.alibaba.fastjson2.reader.ObjectReaderImplDate;
import com.alibaba.fastjson2.time.*;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

public class JdbcSupport {
    public static final class ClobWriter
            implements ObjectWriter {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            Clob clob = (Clob) object;
            Reader reader;
            try {
                reader = clob.getCharacterStream();
            } catch (SQLException e) {
                throw new JSONException("get getCharacterStream error", e);
            }
            jsonWriter.writeString(reader);
        }
    }

    public static final class TimeReader
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
                                formatter.parseLocalDate(str),
                                LocalTime.MIN
                        );
                    } else if (!formatHasDay) {
                        ldt = LocalDateTime.of(
                                LocalDate.of(1970, 1, 1),
                                formatter.parseLocalTime(str)
                        );
                    } else {
                        ldt = formatter.parseLocalDateTime(str);
                    }
                    zdt = ZonedDateTime.of(ldt, jsonReader.context.getZoneId());
                } else {
                    zdt = jsonReader.readZonedDateTime();
                }
                millis = zdt.toInstant().toEpochMilli();
            } else {
                String str = jsonReader.readString();
                if ("0000-00-00".equals(str) || "0000-00-00 00:00:00".equals(str)) {
                    return new Time(0);
                }

                if (str.isEmpty() || "null".equals(str)) {
                    return null;
                }

                return Time.valueOf(str);
            }

            return new Time(millis);
        }
    }

    public static final class TimeWriter
            extends DateTimeCodec
            implements ObjectWriter {
        public static final TimeWriter INSTANCE = new TimeWriter(null);

        public TimeWriter(String format) {
            super(format);
        }

        public static TimeWriter of(String format) {
            if (format == null) {
                return TimeWriter.INSTANCE;
            }

            return new TimeWriter(format);
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
                long millis = ((Time) object).getTime();
                jsonWriter.writeInt64(millis);
                return;
            }

            if (formatISO8601 || context.isDateFormatISO8601()) {
                Instant instant = Instant.ofEpochMilli(((Time) object).getTime());
                ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.UTC);
                int offsetSeconds = zdt.offsetSeconds;

                int year = zdt.dateTime.date.year;
                int month = zdt.dateTime.date.monthValue;
                int dayOfMonth = zdt.dateTime.date.dayOfMonth;
                int hour = zdt.dateTime.time.hour;
                int minute = zdt.dateTime.time.minute;
                int second = zdt.dateTime.time.second;
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

    public static final class TimestampWriter
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

            Timestamp ts = (Timestamp) object;
            long millis = ts.getTime();

            int nanos = ts.getNanos();
            if (nanos == 0) {
                jsonWriter.writeMillis(millis);
                return;
            }

            jsonWriter.writeInstant(millis, nanos);
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            JSONWriter.Context ctx = jsonWriter.context;

            Timestamp ts = (Timestamp) object;

            if (formatUnixTime || ctx.isDateFormatUnixTime()) {
                long millis = ts.getTime();
                jsonWriter.writeInt64(millis / 1000L);
                return;
            }

            ZoneId zoneId = ctx.getZoneId();
            Instant instant = Instant.of(ts);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
            int offsetSeconds = zoneId.getOffsetTotalSeconds(instant);

            if ((formatISO8601 || ctx.isDateFormatISO8601()) && (zdt.dateTime.time.nano % 1000_000 == 0)) {
                int year = zdt.dateTime.date.year;
                int month = zdt.dateTime.date.monthValue;
                int dayOfMonth = zdt.dateTime.date.dayOfMonth;
                int hour = zdt.dateTime.time.hour;
                int minute = zdt.dateTime.time.minute;
                int second = zdt.dateTime.time.second;
                int nano = zdt.dateTime.time.nano;
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
                    long millis = ts.getTime();
                    jsonWriter.writeInt64(millis);
                    return;
                }

                int nanos = ts.getNanos();

                if (nanos == 0) {
                    jsonWriter.writeInt64(ts.getTime());
                    return;
                }

                int year = zdt.dateTime.date.year;
                int month = zdt.dateTime.date.monthValue;
                int dayOfMonth = zdt.dateTime.date.dayOfMonth;
                int hour = zdt.dateTime.time.hour;
                int minute = zdt.dateTime.time.minute;
                int second = zdt.dateTime.time.second;
                if (nanos % 1000_000 == 0) {
                    jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, nanos / 1000_000, offsetSeconds, false);
                } else {
                    jsonWriter.writeLocalDateTime(zdt.dateTime);
                }
            } else {
                String str = dateFormatter.format(zdt);
                jsonWriter.writeString(str);
            }
        }
    }

    public static final class TimestampReader
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

            return readObject(jsonReader, fieldType, fieldName, features);
        }

        Object createTimestamp(long millis, int nanos) {
            Timestamp ts = new Timestamp(millis);
            if (nanos != 0) {
                ts.setNanos(nanos);
            }
            return ts;
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
                    return localDateTime.toTimestamp();
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
                LocalDate localDate = dateFormatter.parseLocalDate(str);
                LocalDateTime ldt = LocalDateTime.of(localDate, LocalTime.MIN);
                instant = ZonedDateTime.of(ldt, jsonReader.context.getZoneId()).toInstant();
            } else {
                LocalDateTime ldt = DateUtils.parseLocalDateTime(str, 0, str.length());
                instant = ldt.toInstant(jsonReader.context.getZoneId());
            }

            long millis = instant.toEpochMilli();
            int nanos = instant.nanos;

            return createTimestamp(millis, nanos);
        }
    }

    public static final class DateReader
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
                    Instant instant = localDateTime.toInstant(jsonReader.getZoneId());
                    return new java.sql.Date(instant.toEpochMilli());
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
                LocalDate localDate = dateFormatter.parseLocalDate(str);
                LocalDateTime ldt = LocalDateTime.of(localDate, LocalTime.MIN);
                instant = ZonedDateTime.of(ldt, jsonReader.context.getZoneId()).toInstant();
            } else {
                LocalDateTime ldt = dateFormatter.parseLocalDateTime(str);
                instant = ZonedDateTime.of(ldt, jsonReader.context.getZoneId()).toInstant();
            }

            return new java.sql.Date(
                    instant.toEpochMilli()
            );
        }
    }
}
