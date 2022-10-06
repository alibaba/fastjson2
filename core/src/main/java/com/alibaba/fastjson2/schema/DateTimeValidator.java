package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.time.DateTimeException;

final class DateTimeValidator
        implements FormatValidator {
    static final DateTimeValidator INSTANCE = new DateTimeValidator();

    @Override
    public boolean isValid(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        char c10;
        if (str.length() == 19
                && str.charAt(4) == '-'
                && str.charAt(7) == '-'
                && ((c10 = str.charAt(10)) == ' ' || c10 == 'T')
                && str.charAt(13) == ':'
                && str.charAt(16) == ':'
        ) {
            // yyyy-MM-dd hh:mm:ss
            char y0 = str.charAt(0);
            char y1 = str.charAt(1);
            char y2 = str.charAt(2);
            char y3 = str.charAt(3);
            char m0 = str.charAt(5);
            char m1 = str.charAt(6);
            char d0 = str.charAt(8);
            char d1 = str.charAt(9);
            char h0 = str.charAt(11);
            char h1 = str.charAt(12);
            char i0 = str.charAt(14);
            char i1 = str.charAt(15);
            char s0 = str.charAt(17);
            char s1 = str.charAt(18);

            if (y0 < '0' || y0 > '9'
                    || y1 < '0' || y1 > '9'
                    || y2 < '0' || y2 > '9'
                    || y3 < '0' || y3 > '9'
                    || m0 < '0' || m0 > '9'
                    || m1 < '0' || m1 > '9'
                    || d0 < '0' || d0 > '9'
                    || d1 < '0' || d1 > '9'
                    || h0 < '0' || h0 > '9'
                    || h1 < '0' || h1 > '9'
                    || i0 < '0' || i0 > '9'
                    || i1 < '0' || i1 > '9'
                    || s0 < '0' || s0 > '9'
                    || s1 < '0' || s1 > '9'
            ) {
                return false;
            }

            int yyyy = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
            int mm = (m0 - '0') * 10 + (m1 - '0');
            int dd = (d0 - '0') * 10 + (d1 - '0');
            int hh = (h0 - '0') * 10 + (h1 - '0');
            int ii = (i0 - '0') * 10 + (i1 - '0');
            int ss = (s0 - '0') * 10 + (s1 - '0');

            if (mm > 12) {
                return false;
            }

            if (dd > 28) {
                int dom = 31;
                switch (mm) {
                    case 2:
                        boolean isLeapYear = ((yyyy & 3) == 0) && ((yyyy % 100) != 0 || (yyyy % 400) == 0);
                        dom = isLeapYear ? 29 : 28;
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }
                if (dd > dom) {
                    return false;
                }
            }

            if (hh > 24) {
                return false;
            }

            if (ii > 60) {
                return false;
            }

            if (ss > 61) {
                return false;
            }

            return true;
        }

        try {
            char[] chars = new char[str.length() + 2];
            chars[0] = '"';
            str.getChars(0, str.length(), chars, 1);
            chars[chars.length - 1] = '"';

            return JSONReader.of(chars, JSONSchema.CONTEXT).isLocalDateTime();
        } catch (DateTimeException | JSONException ignored) {
            return false;
        }
    }
}
