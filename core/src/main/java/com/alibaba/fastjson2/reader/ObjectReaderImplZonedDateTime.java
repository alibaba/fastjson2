package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

class ObjectReaderImplZonedDateTime
        extends DateTimeCodec implements ObjectReader {
    static final ObjectReaderImplZonedDateTime INSTANCE = new ObjectReaderImplZonedDateTime(null, null);

    public static ObjectReaderImplZonedDateTime of(String format, Locale locale) {
        if (format == null) {
            return INSTANCE;
        }

        return new ObjectReaderImplZonedDateTime(format, locale);
    }

    public ObjectReaderImplZonedDateTime(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public Class getObjectClass() {
        return ZonedDateTime.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readZonedDateTime();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        JSONReader.Context context = jsonReader.getContext();

        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();
            if (formatUnixTime) {
                millis *= 1000;
            }

            Instant instant = Instant.ofEpochMilli(millis);
            return ZonedDateTime.ofInstant(instant, context.getZoneId());
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (format == null || yyyyMMddhhmmss19 || formatISO8601) {
            return jsonReader.readZonedDateTime();
        }

        String str = jsonReader.readString();

        if (formatMillis || formatUnixTime) {
            long millis = Long.parseLong(str);
            if (formatUnixTime) {
                millis *= 1000L;
            }
            Instant instant = Instant.ofEpochMilli(millis);
            return ZonedDateTime.ofInstant(instant, context.getZoneId());
        }

        DateTimeFormatter formatter = getDateFormatter(jsonReader.getLocale());
        if (!formatHasHour) {
            return ZonedDateTime.of(
                    LocalDate.parse(str, formatter),
                    LocalTime.MIN,
                    context.getZoneId()
            );
        }

        if (!formatHasDay) {
            return ZonedDateTime.of(
                    LocalDate.of(1970, 1, 1),
                    LocalTime.parse(str, formatter),
                    context.getZoneId()
            );
        }
        LocalDateTime localDateTime = LocalDateTime.parse(str, formatter);
        return ZonedDateTime.of(localDateTime, context.getZoneId());
    }
}
