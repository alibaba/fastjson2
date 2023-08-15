package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ObjectReaderImplOffsetTime
        extends DateTimeCodec implements ObjectReader {
    static final ObjectReaderImplOffsetTime INSTANCE = new ObjectReaderImplOffsetTime(null, null);

    public static ObjectReaderImplOffsetTime of(String format, Locale locale) {
        if (format == null) {
            return INSTANCE;
        }

        return new ObjectReaderImplOffsetTime(format, locale);
    }

    public ObjectReaderImplOffsetTime(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public Class getObjectClass() {
        return OffsetDateTime.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return readObject(jsonReader, fieldType, fieldName, features);
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
            ZoneId zoneId = context.getZoneId();
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, zoneId);
            return OffsetTime.of(ldt.toLocalTime(), zoneId.getRules().getOffset(instant));
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (format == null) {
            return jsonReader.readOffsetTime();
        }

        String str = jsonReader.readString();
        ZoneId zoneId = context.getZoneId();
        if (formatMillis || formatUnixTime) {
            long millis = Long.parseLong(str);
            if (formatUnixTime) {
                millis *= 1000L;
            }
            Instant instant = Instant.ofEpochMilli(millis);
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, zoneId);
            return OffsetDateTime.of(ldt, zoneId.getRules().getOffset(instant));
        }

        DateTimeFormatter formatter = getDateFormatter(jsonReader.getLocale());
        if (!formatHasHour) {
            LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(str, formatter), LocalTime.MIN);
            return OffsetDateTime.of(ldt, zoneId.getRules().getOffset(ldt));
        }

        if (!formatHasDay) {
            return ZonedDateTime.of(
                    LocalDate.of(1970, 1, 1),
                    LocalTime.parse(str, formatter),
                    zoneId
            );
        }
        LocalDateTime ldt = LocalDateTime.parse(str, formatter);
        return OffsetDateTime.of(ldt, zoneId.getRules().getOffset(ldt));
    }
}
