package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public final class ObjectReaderImplInstant
        extends DateTimeCodec
        implements ObjectReader {
    public static final ObjectReaderImplInstant INSTANCE = new ObjectReaderImplInstant(null, null);

    public static ObjectReaderImplInstant of(String format, Locale locale) {
        if (format == null) {
            return INSTANCE;
        }

        return new ObjectReaderImplInstant(format, locale);
    }

    ObjectReaderImplInstant(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public Class getObjectClass() {
        return Instant.class;
    }

    @Override
    public Object createInstance(Map map, long features) {
        Number nano = (Number) map.get("nano");
        Number epochSecond = (Number) map.get("epochSecond");

        if (nano != null && epochSecond != null) {
            return Instant.ofEpochSecond(epochSecond.longValue(), nano.longValue());
        }

        if (epochSecond != null) {
            return Instant.ofEpochSecond(epochSecond.longValue());
        }

        Number epochMilli = (Number) map.get("epochMilli");
        if (epochMilli != null) {
            return Instant.ofEpochMilli(epochMilli.longValue());
        }

        throw new JSONException("can not create instant.");
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readInstant();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        JSONReader.Context context = jsonReader.getContext();

        if (jsonReader.isInt() && context.getDateFormat() == null) {
            long millis = jsonReader.readInt64Value();
            if (formatUnixTime) {
                millis *= 1000;
            }

            return Instant.ofEpochMilli(millis);
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (format == null || yyyyMMddhhmmss19 || formatISO8601 || jsonReader.isObject()) {
            return jsonReader.readInstant();
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
            return Instant.ofEpochMilli(millis);
        }

        DateTimeFormatter formatter = getDateFormatter(jsonReader.getLocale());
        if (!formatHasHour) {
            return ZonedDateTime.of(
                    LocalDate.parse(str, formatter),
                    LocalTime.MIN,
                    context.getZoneId()
            ).toInstant();
        }

        if (!formatHasDay) {
            return ZonedDateTime.of(
                    LocalDate.of(1970, 1, 1),
                    LocalTime.parse(str, formatter),
                    context.getZoneId()
            ).toInstant();
        }
        LocalDateTime localDateTime = LocalDateTime.parse(str, formatter);
        return ZonedDateTime.of(localDateTime, context.getZoneId())
                .toInstant();
    }
}
