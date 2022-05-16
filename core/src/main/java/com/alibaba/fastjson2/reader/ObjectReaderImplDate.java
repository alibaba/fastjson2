package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class ObjectReaderImplDate extends DateTimeCodec implements ObjectReader {
    static final ObjectReaderImplDate INSTANCE = new ObjectReaderImplDate(null, null);

    public static ObjectReaderImplDate of(String format, Locale locale) {
        if (format == null) {
            return INSTANCE;
        }
        return new ObjectReaderImplDate(format, locale);
    }

    public ObjectReaderImplDate(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public Class getObjectClass() {
        return Date.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();
            if (formatUnixTime) {
                millis *= 1000;
            }
            return new Date(millis);
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        return readDate(jsonReader);
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();
            if (formatUnixTime) {
                millis *= 1000;
            }
            return new Date(millis);
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        return readDate(jsonReader);
    }

    private Object readDate(JSONReader jsonReader) {
        long millis;
        if (useSimpleFormatter) {
            String str = jsonReader.readString();
            try {
                return new SimpleDateFormat(format).parse(str);
            } catch (ParseException e) {
                throw new JSONException("parse error : " + str, e);
            }
        }

        if (format != null) {
            DateTimeFormatter formatter = getDateFormatter(jsonReader.getLocale());

            ZonedDateTime zdt;
            if (formatter != null) {
                String str = jsonReader.readString();

                LocalDateTime ldt;
                if (!formatHasHour) {
                    if (!formatHasDay) {
                        TemporalAccessor parsed = formatter.parse(str);
                        int year = parsed.get(ChronoField.YEAR);
                        int month = parsed.get(ChronoField.MONTH_OF_YEAR);
                        int dayOfYear = 1;
                        ldt = LocalDateTime.of(
                                LocalDate.of(year, month, dayOfYear),
                                LocalTime.MIN
                        );
                    } else {
                        ldt = LocalDateTime.of(
                                LocalDate.parse(str, formatter),
                                LocalTime.MIN
                        );
                    }
                } else {
                    ldt = LocalDateTime.parse(str, formatter);
                }
                zdt = ldt.atZone(jsonReader.getContext().getZoneId());
            } else {
                zdt = jsonReader.readZonedDateTime();
            }

            millis = zdt.toInstant().toEpochMilli();
        } else {
            millis = jsonReader.readMillisFromString();
            if (millis == -1) {
                return null;
            }

            if (formatUnixTime) {
                millis *= 1000;
            }
        }

        return new Date(millis);
    }
}
