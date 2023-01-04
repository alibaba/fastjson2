package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

class ObjectReaderImplLocalDateTime
        extends DateTimeCodec
        implements ObjectReader {
    static final ObjectReaderImplLocalDateTime INSTANCE = new ObjectReaderImplLocalDateTime(null, null);

    public ObjectReaderImplLocalDateTime(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public Class getObjectClass() {
        return LocalDateTime.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readLocalDateTime();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        JSONReader.Context context = jsonReader.getContext();

        if (jsonReader.isInt()) {
            DateTimeFormatter formatter = getDateFormatter();
            if (formatter != null) {
                String str = jsonReader.readString();
                return LocalDateTime.parse(str, formatter);
            }

            long millis = jsonReader.readInt64Value();

            if (formatUnixTime) {
                millis *= 1000;
            }

            Instant instant = Instant.ofEpochMilli(millis);
            ZoneId zoneId = context.getZoneId();
            return LocalDateTime.ofInstant(instant, zoneId);
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (format == null || yyyyMMddhhmmss19 || formatISO8601) {
            return jsonReader.readLocalDateTime();
        }

        String str = jsonReader.readString();
        if (str.isEmpty()) {
            return null;
        }

        if (formatMillis || formatUnixTime) {
            long millis = Long.parseLong(str);
            if (formatUnixTime) {
                millis *= 1000L;
            }
            Instant instant = Instant.ofEpochMilli(millis);
            return LocalDateTime.ofInstant(instant, context.getZoneId());
        }

        DateTimeFormatter formatter = getDateFormatter(context.getLocale());
        if (!formatHasHour) {
            return LocalDateTime.of(
                    LocalDate.parse(str, formatter),
                    LocalTime.MIN
            );
        }
        if (!formatHasDay) {
            return LocalDateTime.of(
                    LocalDate.of(1970, 1, 1),
                    LocalTime.parse(str, formatter)
            );
        }
        return LocalDateTime.parse(str, formatter);
    }
}
