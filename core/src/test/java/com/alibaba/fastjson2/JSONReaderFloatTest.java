package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONReaderFloatTest {
    @Test
    public void test4() {
        char[] chars = new char[]{'0', '.', '0', '0', '0', '0'};
        for (int i0 = 0; i0 < 10; i0++) {
            char c0 = (char) ('0' + i0);
            chars[2] = c0;

            for (int i1 = 0; i1 < 10; i1++) {
                char c1 = (char) ('0' + i1);
                chars[3] = c1;

                for (int i2 = 0; i2 < 10; i2++) {
                    char c2 = (char) ('0' + i2);
                    chars[4] = c2;

                    for (int i3 = 0; i3 < 10; i3++) {
                        char c3 = (char) ('0' + i3);
                        chars[5] = c3;

                        int index = i0 * 1000 + i1 * 100 + i2 * 10 + i3;
                        String str = new String(chars);
                        float floatValue4 = JSONReader.floatValue4(index);
                        assertEquals(Float.parseFloat(str), floatValue4);
                    }
                }
            }
        }
    }

    @Test
    public void test5() {
        char[] chars = new char[]{'0', '.', '0', '0', '0', '0', '0'};
        for (int i0 = 0; i0 < 10; i0++) {
            char c0 = (char) ('0' + i0);
            chars[2] = c0;

            for (int i1 = 0; i1 < 10; i1++) {
                char c1 = (char) ('0' + i1);
                chars[3] = c1;

                for (int i2 = 0; i2 < 10; i2++) {
                    char c2 = (char) ('0' + i2);
                    chars[4] = c2;

                    for (int i3 = 0; i3 < 10; i3++) {
                        char c3 = (char) ('0' + i3);
                        chars[5] = c3;

                        for (int i4 = 0; i4 < 10; i4++) {
                            char c4 = (char) ('0' + i4);
                            chars[6] = c4;

                            int index = i0 * 10000 + i1 * 1000 + i2 * 100 + i3 * 10 + i4;
                            String str = new String(chars);
                            float floatValue5 = JSONReader.floatValue5(index);
                            assertEquals(Float.parseFloat(str), floatValue5);
                        }
                    }
                }
            }
        }
    }
}
