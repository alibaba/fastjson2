package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.YearMonth;

final class ObjectReaderImplYearMonth
        extends ObjectReaderPrimitive<YearMonth> {
    static final ObjectReaderImplYearMonth INSTANCE = new ObjectReaderImplYearMonth();

    ObjectReaderImplYearMonth() {
        super(YearMonth.class);
    }

    @Override
    public YearMonth readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.isInt()) {
            return readYearMonthFromNumber(jsonReader);
        }

        String str = jsonReader.readString();
        if (str == null || str.isEmpty()) {
            return null;
        }
        return parseYearMonthString(jsonReader, str);
    }

    @Override
    public YearMonth readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.isInt()) {
            return readYearMonthFromNumber(jsonReader);
        }

        String str = jsonReader.readString();
        if (str == null) {
            return null;
        }
        return parseYearMonthString(jsonReader, str);
    }

    private static YearMonth readYearMonthFromNumber(JSONReader jsonReader) {
        long value = jsonReader.readInt64Value();
        int sign = 1;
        if (value < 0) {
            sign = -1;
            value = -value;
        }

        if (value < 100000 || value > 999999) {
            throw new JSONException(jsonReader.info("read YearMonth error"));
        }

        int year = (int) (value / 100);
        int month = (int) (value % 100);
        year *= sign;

        try {
            return YearMonth.of(year, month);
        } catch (DateTimeException ex) {
            throw new JSONException(jsonReader.info("read YearMonth error"), ex);
        }
    }

    private static YearMonth parseYearMonthString(JSONReader jsonReader, String str) {
        int len = str.length();
        if (len == 0) {
            throw new JSONException(jsonReader.info("read YearMonth error"));
        }

        int sign = 1;
        int offset = 0;
        char first = str.charAt(0);
        if (first == '+' || first == '-') {
            sign = first == '-' ? -1 : 1;
            offset = 1;
        }

        int year;
        int month;
        int remain = len - offset;
        if (remain == 7 && str.charAt(offset + 4) == '-') {
            year = parse4(str, offset);
            month = parse2(str, offset + 5);
        } else if (remain == 6) {
            year = parse4(str, offset);
            month = parse2(str, offset + 4);
        } else {
            throw new JSONException(jsonReader.info("read YearMonth error"));
        }

        if (year < 0 || month < 0) {
            throw new JSONException(jsonReader.info("read YearMonth error"));
        }
        year *= sign;

        try {
            return YearMonth.of(year, month);
        } catch (DateTimeException ex) {
            throw new JSONException(jsonReader.info("read YearMonth error"), ex);
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

    private static int parse4(String str, int offset) {
        int d0 = str.charAt(offset) - '0';
        int d1 = str.charAt(offset + 1) - '0';
        int d2 = str.charAt(offset + 2) - '0';
        int d3 = str.charAt(offset + 3) - '0';
        if ((d0 | d1 | d2 | d3) < 0 || d0 > 9 || d1 > 9 || d2 > 9 || d3 > 9) {
            return -1;
        }
        return d0 * 1000 + d1 * 100 + d2 * 10 + d3;
    }
}
