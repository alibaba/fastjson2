package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.DateTimeCodec;

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
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readZonedDateTime();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();
            if (formatUnixTime) {
                millis *= 1000;
            }

            Instant instant = Instant.ofEpochMilli(millis);
            return ZonedDateTime.ofInstant(instant, jsonReader.getContext().getZoneId());
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.isString()) {
            DateTimeFormatter formatter = getDateFormatter(jsonReader.getLocale());
            if (formatter != null) {
                String str = jsonReader.readString();
                LocalDateTime ldt;
                if (!formatHasHour) {
                    ldt = LocalDateTime.of(LocalDate.parse(str, formatter), LocalTime.MIN);
                } else {
                    ldt = LocalDateTime.parse(str, formatter);
                }
                return ldt.atZone(jsonReader.getZoneId());
            }
        }

        return jsonReader.readZonedDateTime();
    }
}
