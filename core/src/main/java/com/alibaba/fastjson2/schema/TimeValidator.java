package com.alibaba.fastjson2.schema;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

final class TimeValidator
        implements FormatValidator {
    static final TimeValidator INSTANCE = new TimeValidator();

    @Override
    public boolean isValid(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        char h0, h1, m0, m1, s0, s1;
        if (str.length() == 8 && str.charAt(2) == ':' && str.charAt(5) == ':') {
            h0 = str.charAt(0);
            h1 = str.charAt(1);
            m0 = str.charAt(3);
            m1 = str.charAt(4);
            s0 = str.charAt(6);
            s1 = str.charAt(7);
        } else {
            try {
                LocalTime.parse(str);
                return true;
            } catch (DateTimeParseException ignored) {
                return false;
            }
        }

        if (h0 >= '0' && h0 <= '2'
                && h1 >= '0' && h1 <= '9'
                && m0 >= '0' && m0 <= '6'
                && m1 >= '0' && m0 <= '9'
                && s0 >= '0' && s0 <= '6'
                && s1 >= '0' && s0 <= '9'
        ) {
            int hh = (h0 - '0') * 10 + (h1 - '0');
            if (hh > 24) {
                return false;
            }

            int mm = (m0 - '0') * 10 + (m1 - '0');
            if (mm > 60) {
                return false;
            }

            int ss = (s0 - '0') * 10 + (s1 - '0');
            if (ss > 61) {
                return false;
            }

            return true;
        }

        return false;
    }
}
