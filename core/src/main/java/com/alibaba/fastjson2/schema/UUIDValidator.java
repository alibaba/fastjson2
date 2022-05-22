package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.util.UUIDUtils;

final class UUIDValidator
        implements FormatValidator {
    static final UUIDValidator INSTANCE = new UUIDValidator();

    @Override
    public boolean isValid(String str) {
        if (str == null) {
            return false;
        }

        if (str.length() == 32) {
            long msb1 = UUIDUtils.parse4Nibbles(str, 0);
            long msb2 = UUIDUtils.parse4Nibbles(str, 4);
            long msb3 = UUIDUtils.parse4Nibbles(str, 8);
            long msb4 = UUIDUtils.parse4Nibbles(str, 12);
            long lsb1 = UUIDUtils.parse4Nibbles(str, 16);
            long lsb2 = UUIDUtils.parse4Nibbles(str, 20);
            long lsb3 = UUIDUtils.parse4Nibbles(str, 24);
            long lsb4 = UUIDUtils.parse4Nibbles(str, 28);

            return (msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0;
        }

        if (str.length() == 36) {
            char ch1 = str.charAt(8);
            char ch2 = str.charAt(13);
            char ch3 = str.charAt(18);
            char ch4 = str.charAt(23);
            if (ch1 == '-' && ch2 == '-' && ch3 == '-' && ch4 == '-') {
                long msb1 = UUIDUtils.parse4Nibbles(str, 0);
                long msb2 = UUIDUtils.parse4Nibbles(str, 4);
                long msb3 = UUIDUtils.parse4Nibbles(str, 9);
                long msb4 = UUIDUtils.parse4Nibbles(str, 14);
                long lsb1 = UUIDUtils.parse4Nibbles(str, 19);
                long lsb2 = UUIDUtils.parse4Nibbles(str, 24);
                long lsb3 = UUIDUtils.parse4Nibbles(str, 28);
                long lsb4 = UUIDUtils.parse4Nibbles(str, 32);
                return (msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0;
            }
        }
        return false;
    }
}
