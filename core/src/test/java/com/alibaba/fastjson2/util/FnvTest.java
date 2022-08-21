package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FnvTest {
    @Test
    public void test0() {
        char[] chars = new char[16];
        for (char c0 = 1; c0 < 0x7F; ++c0) {
            chars[0] = c0;

            String str0 = new String(chars, 0, 1);
            assertEquals((byte) c0, Fnv.hashCode64(str0));
            assertEquals((byte) c0, Fnv.hashCode64(new byte[]{(byte) c0}));

            for (char c1 = 1; c1 < 0x7F; ++c1) {
                chars[1] = c1;

                String str1 = new String(chars, 0, 2);
                int expected = ((byte) c0 << 8) + c1;
                assertEquals(
                        expected,
                        Fnv.hashCode64(str1)
                );
                assertEquals(
                        expected,
                        Fnv.hashCode64(new byte[]{(byte) c0, (byte) c1})
                );
            }
        }
    }

    @Test
    public void test1() {
        assertEquals(
                Fnv.hashCode64LCase("A"),
                Fnv.hashCode64LCase("a")
        );

        assertEquals(
                Fnv.hashCode64LCase("_A"),
                Fnv.hashCode64LCase("a_")
        );

        assertEquals(
                Fnv.hashCode64LCase("-A"),
                Fnv.hashCode64LCase("a-")
        );
    }
}
