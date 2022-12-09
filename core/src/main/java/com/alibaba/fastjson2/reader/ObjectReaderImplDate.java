package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;

public class ObjectReaderImplDate
        extends DateTimeCodec
        implements ObjectReader {
    public static final ObjectReaderImplDate INSTANCE = new ObjectReaderImplDate(null, null);

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
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
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
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
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
                throw new JSONException(jsonReader.info("parse error : " + str), e);
            }
        }

        if (jsonReader.nextIfNullOrEmptyString()) {
            return null;
        }

        if ((formatUnixTime || formatUnixTime) && jsonReader.isString()) {
            millis = jsonReader.readInt64Value();
            if (formatUnixTime) {
                millis *= 1000L;
            }
        } else if (format != null) {
            ZonedDateTime zdt;
            if (yyyyMMddhhmmss19) {
                if (jsonReader.isSupportSmartMatch()) {
                    millis = jsonReader.readMillisFromString();
                } else {
                    millis = jsonReader.readMillis19();
                }
                if (millis != 0 || !jsonReader.wasNull()) {
                    return new Date(millis);
                }
                zdt = jsonReader.readZonedDateTime();
            } else {
                DateTimeFormatter formatter = getDateFormatter(jsonReader.getLocale());

                if (formatter != null) {
                    String str = jsonReader.readString();
                    if (str.isEmpty() || "null".equals(str)) {
                        return null;
                    }

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
            }

            if (zdt == null) {
                return null;
            }

            long seconds = zdt.toEpochSecond();
            int nanos = zdt.toLocalTime().getNano();
            if (seconds < 0 && nanos > 0) {
                millis = (seconds + 1) * 1000;
                long adjustment = nanos / 1000_000 - 1000;
                millis += adjustment;
            } else {
                millis = seconds * 1000L;
                millis += nanos / 1000_000;
            }
        } else {
            if (jsonReader.isTypeRedirect() && jsonReader.nextIfMatchIdent('"', 'v', 'a', 'l', '"')) {
                jsonReader.nextIfMatch(':');
                millis = jsonReader.readInt64Value();
                jsonReader.nextIfMatch('}');
                jsonReader.setTypeRedirect(false);
            } else {
                millis = jsonReader.readMillisFromString();
            }

            if (millis == 0 && jsonReader.wasNull()) {
                return null;
            }

            if (formatUnixTime) {
                millis *= 1000;
            }
        }

        return new Date(millis);
    }
}
