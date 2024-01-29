package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;

class ObjectReaderImplZonedDateTime
        extends DateTimeCodec implements ObjectReader {
    static final ObjectReaderImplZonedDateTime INSTANCE = new ObjectReaderImplZonedDateTime(null, null);

    public static ObjectReaderImplZonedDateTime of(String format, Locale locale) {
        if (format == null) {
            return INSTANCE;
        }

        return new ObjectReaderImplZonedDateTime(format, locale);
    }

    private Function builder;

    public ObjectReaderImplZonedDateTime(Function builder) {
        super(null, null);
        this.builder = builder;
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

        ZonedDateTime zdt;
        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();
            if (formatUnixTime) {
                millis *= 1000;
            }

            Instant instant = Instant.ofEpochMilli(millis);
            zdt = ZonedDateTime.ofInstant(instant, context.getZoneId());
        } else {
            if (jsonReader.readIfNull()) {
                zdt = null;
            } else if (format == null || yyyyMMddhhmmss19 || formatISO8601) {
                zdt = jsonReader.readZonedDateTime();
            } else {
                String str = jsonReader.readString();
                if (formatMillis || formatUnixTime) {
                    long millis = Long.parseLong(str);
                    if (formatUnixTime) {
                        millis *= 1000L;
                    }
                    Instant instant = Instant.ofEpochMilli(millis);
                    zdt = ZonedDateTime.ofInstant(instant, context.getZoneId());
                } else {
                    DateTimeFormatter formatter = getDateFormatter(jsonReader.getLocale());
                    if (!formatHasHour) {
                        zdt = ZonedDateTime.of(
                                LocalDate.parse(str, formatter),
                                LocalTime.MIN,
                                context.getZoneId()
                        );
                    } else {
                        if (!formatHasDay) {
                            zdt = ZonedDateTime.of(
                                    LocalDate.of(1970, 1, 1),
                                    LocalTime.parse(str, formatter),
                                    context.getZoneId()
                            );
                        } else {
                            LocalDateTime localDateTime = LocalDateTime.parse(str, formatter);
                            zdt = ZonedDateTime.of(localDateTime, context.getZoneId());
                        }
                    }
                }
            }
        }

        if (builder != null && zdt != null) {
            return builder.apply(zdt);
        }

        return zdt;
    }
}
