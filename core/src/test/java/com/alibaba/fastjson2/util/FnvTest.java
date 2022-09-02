package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONFactory.MIXED_HASH_ALGORITHM;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_PRIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FnvTest {
    @Test
    public void test0() {
        char[] chars = new char[16];
        for (char c0 = 1; c0 < 0x7F; ++c0) {
            chars[0] = c0;

            String str0 = new String(chars, 0, 1);

            long hash0;
            if (MIXED_HASH_ALGORITHM) {
                hash0 = (byte) c0;
            } else {
                long hash = Fnv.MAGIC_HASH_CODE;
                hash ^= c0;
                hash *= MAGIC_PRIME;
                hash0 = hash;
            }

            assertEquals(hash0, Fnv.hashCode64(str0));
            assertEquals(hash0, Fnv.hashCode64(new byte[]{(byte) c0}));

            for (char c1 = 1; c1 < 0x7F; ++c1) {
                chars[1] = c1;

                String str1 = new String(chars, 0, 2);

                long hash1;
                if (MIXED_HASH_ALGORITHM) {
                    hash1 = ((byte) c1 << 8) + c0;
                } else {
                    long hash = Fnv.MAGIC_HASH_CODE;

                    hash ^= c0;
                    hash *= MAGIC_PRIME;
                    hash ^= c1;
                    hash *= MAGIC_PRIME;

                    hash1 = hash;
                }

                assertEquals(
                        hash1,
                        Fnv.hashCode64(str1)
                );
                assertEquals(
                        hash1,
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

    @Test
    public void test2() {
        char[] chars = new char[1];
        for (char c0 = 1; c0 <= 0xFF; ++c0) {
            chars[0] = c0;
            String str = new String(chars);
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            System.out.println(((int) c0) + "\t" + Arrays.toString(bytes));
        }
    }
}
