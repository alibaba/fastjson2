package com.alibaba.fastjson2.util;

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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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

    public static ObjectWriter createTimeWriter() {
        return new TimeWriter();
    }

    public static ObjectWriter createTimestampWriter() {
        return TimestampWriter.INSTANCE;
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
                return new java.sql.Time(millis);
            }

            if (jsonReader.readIfNull()) {
                return null;
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
                String str =jsonReader.readString();
                return java.sql.Time.valueOf(str);
            }

            return new java.sql.Time(millis);
        }
    }

    static class TimeWriter implements ObjectWriter {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            jsonWriter.writeString(object.toString());
        }
    }

    static class TimestampWriter implements ObjectWriter {
        static final TimestampWriter INSTANCE = new TimestampWriter();

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
            int nanos = date.getNanos();

            if (nanos == 0) {
                jsonWriter.writeInt64(date.getTime());
                return;
            }

            ZoneId zoneId = ctx.getZoneId();
            Instant instant = date.toInstant();
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
            int offsetSeconds = zdt.getOffset().getTotalSeconds();

            if (ctx.isDateFormatISO8601()) {
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

            String dateFormat = ctx.getDateFormat();
            if (dateFormat == null) {
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
                String str = ctx.getDateFormatter().format(zdt);
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
                return new java.sql.Timestamp(millis);
            }

            if (jsonReader.readIfNull()) {
                return null;
            }

            if (format == null) {
                LocalDateTime localDateTime = jsonReader.readLocalDateTime();
                if (localDateTime != null) {
                    return java.sql.Timestamp.valueOf(localDateTime);
                }

                long millis = jsonReader.readMillisFromString();
                return new java.sql.Timestamp(millis);
            }

            SimpleDateFormat formatter = FORMATTER_UPDATER.getAndSet(this, null);
            if (formatter == null) {
                formatter = new SimpleDateFormat(format);
            }

            String str = null;
            try {
                str = jsonReader.readString();
                java.util.Date utilDate = formatter.parse(str);
                long millis = utilDate.getTime();
                return new java.sql.Timestamp(millis);
            } catch (ParseException e) {
                throw new JSONException("parse date error, format " + format + ", input " + str, e);
            } finally {
                FORMATTER_UPDATER.set(this, formatter);
            }
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
                return new java.sql.Date(millis);
            }

            if (jsonReader.readIfNull()) {
                return null;
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
                millis = jsonReader.readMillisFromString();
            }

            return new java.sql.Date(millis);
        }
    }
}
