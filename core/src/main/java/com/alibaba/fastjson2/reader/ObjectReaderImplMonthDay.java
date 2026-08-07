package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.MonthDay;

final class ObjectReaderImplMonthDay
        extends ObjectReaderPrimitive<MonthDay> {
    static final ObjectReaderImplMonthDay INSTANCE = new ObjectReaderImplMonthDay();

    ObjectReaderImplMonthDay() {
        super(MonthDay.class);
    }

    @Override
    public MonthDay readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.isInt()) {
            return readMonthDayFromNumber(jsonReader);
        }

        String str = jsonReader.readString();
        if (str == null || str.isEmpty()) {
            return null;
        }
        return parseMonthDayString(jsonReader, str);
    }

    @Override
    public MonthDay readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.isInt()) {
            return readMonthDayFromNumber(jsonReader);
        }

        String str = jsonReader.readString();
        if (str == null) {
            return null;
        }
        return parseMonthDayString(jsonReader, str);
    }

    private static MonthDay readMonthDayFromNumber(JSONReader jsonReader) {
        long value = jsonReader.readInt64Value();
        if (value <= 0) {
            throw new JSONException(jsonReader.info("read MonthDay error"));
        }

        int month = (int) (value / 100);
        int day = (int) (value % 100);
        if (month == 0 || day == 0) {
            throw new JSONException(jsonReader.info("read MonthDay error"));
        }

        try {
            return MonthDay.of(month, day);
        } catch (DateTimeException ex) {
            throw new JSONException(jsonReader.info("read MonthDay error"), ex);
        }
    }

    private static MonthDay parseMonthDayString(JSONReader jsonReader, String str) {
        int len = str.length();
        if (len == 0) {
            throw new JSONException(jsonReader.info("read MonthDay error"));
        }

        int month;
        int day;
        if (len == 7 && str.charAt(0) == '-' && str.charAt(1) == '-' && str.charAt(4) == '-') {
            month = parse2(str, 2);
            day = parse2(str, 5);
        } else if (len == 5 && str.charAt(2) == '-') {
            month = parse2(str, 0);
            day = parse2(str, 3);
        } else {
            throw new JSONException(jsonReader.info("read MonthDay error"));
        }

        if (month < 0 || day < 0) {
            throw new JSONException(jsonReader.info("read MonthDay error"));
        }

        try {
            return MonthDay.of(month, day);
        } catch (DateTimeException ex) {
            throw new JSONException(jsonReader.info("read MonthDay error"), ex);
        }
    }

    private static int parse2(String str, int offset) {
        int d0 = str.charAt(offset) - '0';
        int d1 = str.charAt(offset + 1) - '0';
        if ((d0 | d1) < 0 || d0 > 9 || d1 > 9) {
            return -1;
        }
        return d0 * 10 + d1;
    }
}
