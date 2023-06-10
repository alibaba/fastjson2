package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.DateTimeCodec;
import com.alibaba.fastjson2.time.DateTimeFormatter;
import com.alibaba.fastjson2.time.LocalDateTime;
import com.alibaba.fastjson2.time.ZonedDateTime;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

final class ObjectReaderImplCalendar
        extends DateTimeCodec
        implements ObjectReader {
    static final ObjectReaderImplCalendar INSTANCE = new ObjectReaderImplCalendar(null, null);

    public static ObjectReaderImplCalendar of(String format, Locale locale) {
        if (format == null) {
            return INSTANCE;
        }

        return new ObjectReaderImplCalendar(format, locale);
    }

    public ObjectReaderImplCalendar(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public Class getObjectClass() {
        return Calendar.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();

            if (formatUnixTime) {
                millis *= 1000;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(millis);
            return calendar;
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        long millis = jsonReader.readMillisFromString();
        if (formatUnixTime) {
            millis *= 1000;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
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

        if (formatUnixTime || formatMillis) {
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
                    Calendar calendar = Calendar.getInstance(jsonReader.context.getTimeZone());
                    calendar.setTimeInMillis(millis);
                    return calendar;
                }
                zdt = jsonReader.readZonedDateTime();
            } else {
                DateTimeFormatter formatter = getDateFormatter(jsonReader.getLocale());

                if (formatter != null) {
                    if (jsonReader.jsonb && !jsonReader.isString()) {
                        return jsonReader.readCalendar();
                    }

                    String str = jsonReader.readString();
                    if (str.isEmpty() || "null".equals(str)) {
                        return null;
                    }

                    LocalDateTime ldt;
                    if (!formatHasHour) {
                        if (!formatHasDay) {
                            return formatter.parseDate(str, jsonReader.getZoneId());
                        } else {
                            if (str.length() == 19
                                    && jsonReader.isEnabled(JSONReader.Feature.SupportSmartMatch)
                            ) {
                                ldt = DateUtils.parseLocalDateTime(str, 0, str.length());
                            } else {
                                if (format.indexOf('-') != -1 && str.indexOf('-') == -1 && TypeUtils.isInteger(str)) {
                                    millis = Long.parseLong(str);
                                    Calendar calendar = Calendar.getInstance(jsonReader.context.getTimeZone());
                                    calendar.setTimeInMillis(millis);
                                    return calendar;
                                }

                                Date date = formatter.parseDate(str, jsonReader.getZoneId());
                                Calendar calendar = Calendar.getInstance(jsonReader.context.getTimeZone());
                                calendar.setTime(date);
                                return calendar;
                            }
                        }
                    } else {
                        if (str.length() == 19 && (yyyyMMddhhmm16 || jsonReader.isEnabled(JSONReader.Feature.SupportSmartMatch))) {
                            int length = yyyyMMddhhmm16 ? 16 : 19;
                            ldt = DateUtils.parseLocalDateTime(str, 0, length);
                        } else {
                            try {
                                Date date = new SimpleDateFormat(format).parse(str);
                                Calendar calendar = Calendar.getInstance(jsonReader.context.getTimeZone());
                                calendar.setTime(date);
                                return calendar;
                            } catch (ParseException e) {
                                throw new JSONException("parse format '" + format + "'", e);
                            }
                        }
                    }
                    zdt = ZonedDateTime.of(ldt, jsonReader.context.getZoneId());
                } else {
                    zdt = jsonReader.readZonedDateTime();
                }
            }

            if (zdt == null) {
                return null;
            }

            long seconds = zdt.toEpochSecond();
            int nanos = zdt.dateTime.time.nano;
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
                jsonReader.nextIfObjectEnd();
                jsonReader.setTypeRedirect(false);
            } else {
                return jsonReader.readCalendar();
            }

            if (millis == 0 && jsonReader.wasNull()) {
                return null;
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar;
    }
}
