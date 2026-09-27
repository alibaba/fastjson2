package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import static com.alibaba.fastjson2.util.Fnv.MAGIC_HASH_CODE;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_PRIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("util")
public class FnvTest {
    @Test
    public void test0() {
        char[] chars = new char[16];
        for (char c0 = 1; c0 < 0x7F; ++c0) {
            chars[0] = c0;

            String str0 = new String(chars, 0, 1);

            long hash0 = (byte) c0;

            assertEquals(hash0, Fnv.hashCode64(str0));
            assertEquals(hash0, hashCode64(new byte[]{(byte) c0}));

            for (char c1 = 1; c1 < 0x7F; ++c1) {
                chars[1] = c1;

                String str1 = new String(chars, 0, 2);

                long hash1 = ((byte) c1 << 8) + c0;

                assertEquals(
                        hash1,
                        Fnv.hashCode64(str1)
                );
                assertEquals(
                        hash1,
                        hashCode64(new byte[]{(byte) c0, (byte) c1})
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

    @Test
    public void testHashCode64ByteArrayShortTail() {
        // when a short name starts less than 8 bytes before the end of the array,
        // the fast path must not read past the end of the array, and the result
        // must be identical to the case where 8 readable bytes are available
        Random rnd = new Random(42);
        for (int len = 1; len <= 8; ++len) {
            for (int trial = 0; trial < 200; ++trial) {
                byte[] name = new byte[len];
                for (int i = 0; i < len; ++i) {
                    name[i] = (byte) ('a' + rnd.nextInt(26));
                }
                if (trial == 0) {
                    Arrays.fill(name, (byte) 0); // nameValue == 0 falls back to the fnv loop
                }

                // only 2 spare bytes after offset -> fewer than 8 readable, name ends at the array end
                byte[] tight = new byte[len + 2];
                System.arraycopy(name, 0, tight, 2, len);
                // 8 spare bytes after offset -> original fast path
                byte[] padded = new byte[len + 10];
                System.arraycopy(name, 0, padded, 2, len);

                assertEquals(
                        Fnv.hashCode64(padded, 2, len, true),
                        Fnv.hashCode64(tight, 2, len, true)
                );
                assertEquals(
                        hashCode64(name),
                        Fnv.hashCode64(tight, 2, len, true)
                );
            }
        }
    }

    public static long hashCode64(byte... name) {
        if (name.length > 0 && name.length <= 8 && name[0] != 0) {
            long nameValue = 0;
            switch (name.length) {
                case 1:
                    nameValue = name[0];
                    break;
                case 2:
                    nameValue
                            = ((name[1]) << 8)
                            + (name[0] & 0xFF);
                    break;
                case 3:
                    nameValue
                            = ((name[2]) << 16)
                            + ((name[1] & 0xFF) << 8)
                            + (name[0] & 0xFF);
                    break;
                case 4:
                    nameValue
                            = (name[3] << 24)
                            + ((name[2] & 0xFF) << 16)
                            + ((name[1] & 0xFF) << 8)
                            + (name[0] & 0xFF);
                    break;
                case 5:
                    nameValue
                            = (((long) name[4]) << 32)
                            + ((name[3] & 0xFFL) << 24)
                            + ((name[2] & 0xFFL) << 16)
                            + ((name[1] & 0xFFL) << 8)
                            + (name[0] & 0xFFL);
                    break;
                case 6:
                    nameValue
                            = (((long) name[5]) << 40)
                            + ((name[4] & 0xFFL) << 32)
                            + ((name[3] & 0xFFL) << 24)
                            + ((name[2] & 0xFFL) << 16)
                            + ((name[1] & 0xFFL) << 8)
                            + (name[0] & 0xFFL);
                    break;
                case 7:
                    nameValue
                            = (((long) name[6]) << 48)
                            + ((name[5] & 0xFFL) << 40)
                            + ((name[4] & 0xFFL) << 32)
                            + ((name[3] & 0xFFL) << 24)
                            + ((name[2] & 0xFFL) << 16)
                            + ((name[1] & 0xFFL) << 8)
                            + (name[0] & 0xFFL);
                    break;
                case 8:
                    nameValue
                            = (((long) name[7]) << 56)
                            + ((name[6] & 0xFFL) << 48)
                            + ((name[5] & 0xFFL) << 40)
                            + ((name[4] & 0xFFL) << 32)
                            + ((name[3] & 0xFFL) << 24)
                            + ((name[2] & 0xFFL) << 16)
                            + ((name[1] & 0xFFL) << 8)
                            + (name[0] & 0xFFL);
                    break;
                default:
                    break;
            }

            if (nameValue != 0) {
                return nameValue;
            }
        }

        long hashCode = MAGIC_HASH_CODE;
        for (int i = 0; i < name.length; ++i) {
            char ch = (char) name[i];
            hashCode ^= ch;
            hashCode *= MAGIC_PRIME;
        }
        return hashCode;
    }
}
