package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.DateTimeCodec;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.TypeUtils;

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
        return readDate(jsonReader);
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return readDate(jsonReader);
    }

    private Object readDate(JSONReader jsonReader) {
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

        if (jsonReader.nextIfNullOrEmptyString()) {
            return null;
        }

        if (jsonReader.current() == 'n') {
            return jsonReader.readNullOrNewDate();
        }

        long millis;
        if (useSimpleFormatter || locale != null) {
            String str = jsonReader.readString();
            try {
                SimpleDateFormat dateFormat;
                if (locale != null) {
                    dateFormat = new SimpleDateFormat(format, locale);
                } else {
                    dateFormat = new SimpleDateFormat(format);
                }
                return dateFormat.parse(str);
            } catch (ParseException e) {
                throw new JSONException(jsonReader.info("parse error : " + str), e);
            }
        }

        if ((formatUnixTime || formatMillis) && jsonReader.isString()) {
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
                            LocalDate localDate;
                            if (str.length() == 19 && jsonReader.isEnabled(JSONReader.Feature.SupportSmartMatch)) {
                                ldt = DateUtils.parseLocalDateTime(str, 0, str.length());
                            } else {
                                if (format.indexOf('-') != -1 && str.indexOf('-') == -1 && TypeUtils.isInteger(str)) {
                                    millis = Long.parseLong(str);
                                    return new Date(millis);
                                }

                                // For yyyy-MM-dd format, if the string contains time part, we should ignore it to be compatible with fastjson1
                                if (yyyyMMdd10 && str.length() > 10) {
                                    // Extract only the date part (first 10 characters)
                                    String datePart = str.substring(0, 10);
                                    localDate = LocalDate.parse(datePart, formatter);
                                } else {
                                    localDate = LocalDate.parse(str, formatter);
                                }
                                ldt = LocalDateTime.of(localDate, LocalTime.MIN);
                            }
                        }
                    } else {
                        if (str.length() == 19
                                && (yyyyMMddhhmm16
                                || jsonReader.isEnabled(JSONReader.Feature.SupportSmartMatch)
                                || "yyyy-MM-dd hh:mm:ss".equals(format))) {
                            int length = yyyyMMddhhmm16 ? 16 : 19;
                            ldt = DateUtils.parseLocalDateTime(str, 0, length);
                        } else {
                            if (formatHasDay) {
                                ldt = LocalDateTime.parse(str, formatter);
                            } else {
                                LocalTime localTime = LocalTime.parse(str, formatter);
                                ldt = LocalDateTime.of(LocalDate.MIN, localTime);
                            }
                        }
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
            if (jsonReader.isDate()) {
                return jsonReader.readDate();
            }

            if (jsonReader.isTypeRedirect() && jsonReader.nextIfMatchIdent('"', 'v', 'a', 'l', '"')) {
                jsonReader.nextIfMatch(':');
                millis = jsonReader.readInt64Value();
                jsonReader.nextIfObjectEnd();
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
