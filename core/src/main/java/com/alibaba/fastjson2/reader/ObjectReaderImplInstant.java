package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.DateTimeCodec;

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
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readInstant();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isString()) {
            if (format != null) {
                DateTimeFormatter formatter = getDateFormatter(jsonReader.getLocale());
                if (formatter != null) {
                    String str = jsonReader.readString();

                    LocalDateTime ldt;
                    if (!formatHasHour) {
                        ldt = LocalDateTime.of(
                                LocalDate.parse(str, formatter),
                                LocalTime.MIN
                        );
                    } else {
                        ldt = LocalDateTime.parse(str, formatter);
                    }
                    ZonedDateTime zdt = ldt.atZone(jsonReader.getContext().getZoneId());
                    return zdt.toInstant();
                }
            }
        }

        return jsonReader.readInstant();
    }
}
