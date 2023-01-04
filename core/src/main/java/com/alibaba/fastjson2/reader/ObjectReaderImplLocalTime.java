package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

class ObjectReaderImplLocalTime
        extends DateTimeCodec
        implements ObjectReader {
    static final ObjectReaderImplLocalTime INSTANCE = new ObjectReaderImplLocalTime(null, null);

    public ObjectReaderImplLocalTime(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public Class getObjectClass() {
        return LocalTime.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readLocalTime();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        JSONReader.Context context = jsonReader.getContext();

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();

            if (formatUnixTime) {
                millis *= 1000;
            }

            Instant instant = Instant.ofEpochMilli(millis);
            ZoneId zoneId = context.getZoneId();
            return LocalDateTime.ofInstant(instant, zoneId)
                    .toLocalTime();
        }

        if (format == null || jsonReader.isNumber()) {
            return jsonReader.readLocalTime();
        }

        if (yyyyMMddhhmmss19 || formatISO8601) {
            return jsonReader
                    .readLocalDateTime()
                    .toLocalTime();
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
            return LocalDateTime.ofInstant(instant, context.getZoneId()).toLocalTime();
        }

        DateTimeFormatter formatter = getDateFormatter(context.getLocale());
        if (formatHasDay) {
            return LocalDateTime
                    .parse(str, formatter)
                    .toLocalTime();
        }

        return LocalTime
                .parse(str, formatter);
    }
}
