package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

class ObjectReaderImplLocalDate
        extends DateTimeCodec
        implements ObjectReader {
    static final ObjectReaderImplLocalDate INSTANCE = new ObjectReaderImplLocalDate(null, null);

    public ObjectReaderImplLocalDate(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public Class getObjectClass() {
        return LocalDate.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readLocalDate();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        JSONReader.Context context = jsonReader.getContext();

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (format == null || yyyyMMddhhmmss19 || formatISO8601 || jsonReader.isNumber()) {
            return jsonReader.readLocalDate();
        }

        String str = jsonReader.readString();
        if (str.isEmpty() || "null".equals(str)) {
            return null;
        }

        if (formatMillis || formatUnixTime) {
            long millis = Long.parseLong(str);
            if (formatUnixTime) {
                millis *= 1000L;
            }
            Instant instant = Instant.ofEpochMilli(millis);
            return LocalDateTime.ofInstant(instant, context.getZoneId()).toLocalDate();
        }

        DateTimeFormatter formatter = getDateFormatter(context.getLocale());

        if (!formatHasHour) {
            return LocalDate.parse(str, formatter);
        }

        if (!formatHasDay) {
            return LocalDate.of(1970, 1, 1);
        }

        return LocalDateTime
                .parse(str, formatter)
                .toLocalDate();
    }
}
