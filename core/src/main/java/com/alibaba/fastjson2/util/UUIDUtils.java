package com.alibaba.fastjson2.util;

public final class UUIDUtils {
    private static final byte[] NIBBLES = new byte[]{
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            +0, +1, +2, +3, +4, +5, +6, +7, +8, +9, -1, -1, -1, -1, -1, -1,
            -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    public static long parse4Nibbles(byte[] name, int pos) {
        byte ch1 = name[pos];
        byte ch2 = name[pos + 1];
        byte ch3 = name[pos + 2];
        byte ch4 = name[pos + 3];
        return (ch1 | ch2 | ch3 | ch4) > 0xff ? -1 :
                NIBBLES[ch1] << 12 | NIBBLES[ch2] << 8 | NIBBLES[ch3] << 4 | NIBBLES[ch4];
    }

    public static long parse4Nibbles(char[] name, int pos) {
        char ch1 = name[pos];
        char ch2 = name[pos + 1];
        char ch3 = name[pos + 2];
        char ch4 = name[pos + 3];
        return (ch1 | ch2 | ch3 | ch4) > 0xFF ? -1 :
                NIBBLES[ch1] << 12 | NIBBLES[ch2] << 8 | NIBBLES[ch3] << 4 | NIBBLES[ch4];
    }

    public static long parse4Nibbles(String name, int pos) {
        char ch1 = name.charAt(pos);
        char ch2 = name.charAt(pos + 1);
        char ch3 = name.charAt(pos + 2);
        char ch4 = name.charAt(pos + 3);
        return (ch1 | ch2 | ch3 | ch4) > 0xFF ? -1 :
                NIBBLES[ch1] << 12 | NIBBLES[ch2] << 8 | NIBBLES[ch3] << 4 | NIBBLES[ch4];
    }
}
