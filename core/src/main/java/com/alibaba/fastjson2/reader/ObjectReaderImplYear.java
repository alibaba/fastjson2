package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.Year;

final class ObjectReaderImplYear
        extends ObjectReaderPrimitive<Year> {
    static final ObjectReaderImplYear INSTANCE = new ObjectReaderImplYear();

    ObjectReaderImplYear() {
        super(Year.class);
    }

    @Override
    public Year readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.isInt()) {
            return readYearFromNumber(jsonReader);
        }

        String str = jsonReader.readString();
        if (str == null || str.isEmpty()) {
            return null;
        }
        return parseYearString(jsonReader, str);
    }

    @Override
    public Year readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.isInt()) {
            return readYearFromNumber(jsonReader);
        }

        String str = jsonReader.readString();
        if (str == null) {
            return null;
        }
        return parseYearString(jsonReader, str);
    }

    private static Year readYearFromNumber(JSONReader jsonReader) {
        long value = jsonReader.readInt64Value();
        if (value < Year.MIN_VALUE || value > Year.MAX_VALUE) {
            throw new JSONException(jsonReader.info("read Year error"));
        }
        return Year.of((int) value);
    }

    private static Year parseYearString(JSONReader jsonReader, String str) {
        int len = str.length();
        if (len == 0) {
            throw new JSONException(jsonReader.info("read Year error"));
        }

        int sign = 1;
        int offset = 0;
        char first = str.charAt(0);
        if (first == '+' || first == '-') {
            sign = first == '-' ? -1 : 1;
            offset = 1;
        }

        if (len - offset != 4) {
            throw new JSONException(jsonReader.info("read Year error"));
        }

        int year = parse4(str, offset);
        if (year < 0) {
            throw new JSONException(jsonReader.info("read Year error"));
        }
        year *= sign;

        try {
            return Year.of(year);
        } catch (DateTimeException ex) {
            throw new JSONException(jsonReader.info("read Year error"), ex);
        }
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
