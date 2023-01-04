package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.time.DateTimeException;

final class DateValidator
        implements FormatValidator {
    static final DateValidator INSTANCE = new DateValidator();

    @Override
    public boolean isValid(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        if (str.length() == 10
                && str.charAt(4) == '-'
                && str.charAt(7) == '-'
        ) {
            // yyyy-MM-dd
            char y0 = str.charAt(0);
            char y1 = str.charAt(1);
            char y2 = str.charAt(2);
            char y3 = str.charAt(3);
            char m0 = str.charAt(5);
            char m1 = str.charAt(6);
            char d0 = str.charAt(8);
            char d1 = str.charAt(9);

            int yyyy = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
            int mm = (m0 - '0') * 10 + (m1 - '0');
            int dd = (d0 - '0') * 10 + (d1 - '0');

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
            } else if (dd > 31) {
                return false;
            }

            return true;
        }

        if (str.length() < 9 || str.length() > 40) {
            return false;
        }

        try {
            char[] chars = new char[str.length() + 2];
            chars[0] = '"';
            str.getChars(0, str.length(), chars, 1);
            chars[chars.length - 1] = '"';

            return JSONReader.of(chars, JSONSchema.CONTEXT)
                    .isLocalDate();
        } catch (DateTimeException | JSONException ignored) {
            return false;
        }
    }
}
