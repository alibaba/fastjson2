package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.*;

public class JDKUtilsTest {
    @Test
    public void test_0() throws Throwable {
        if (JDKUtils.JVM_VERSION == 8) {
            BiFunction<char[], Boolean, String> stringCreator = JDKUtils.getStringCreatorJDK8();

            char[] chars = new char[]{'a', 'b', 'c'};
            String apply = stringCreator.apply(chars, Boolean.TRUE);
            System.out.println(apply);
        }
    }

    @Test
    public void test_11() throws Throwable {
        System.out.println("JVM_VERSION : " + JDKUtils.JVM_VERSION);
        if (JDKUtils.JVM_VERSION == 11 && JDKUtils.LANG_UNNAMED) {
            Function<byte[], String> stringCreator = JDKUtils.getStringCreatorJDK11();

            byte[] bytes = new byte[]{'a', 'b', 'c'};
            String apply = stringCreator.apply(bytes);
            assertEquals("abc", apply);
        }
    }

    @Test
    public void test_17() throws Throwable {
        System.out.println("JVM_VERSION : " + JDKUtils.JVM_VERSION);
        if (JDKUtils.JVM_VERSION == 17 && JDKUtils.STRING_BYTES_INTERNAL_API) {
            BiFunction<byte[], Charset, String> stringCreator = JDKUtils.getStringCreatorJDK17();

            byte[] bytes = new byte[]{'a', 'b', 'c'};
            String apply = stringCreator.apply(bytes, StandardCharsets.US_ASCII);
            assertEquals("abc", apply);
        }
    }

    @Test
    public void test_11_coder() throws Throwable {
        if (JDKUtils.JVM_VERSION == 11 && JDKUtils.LANG_UNNAMED) {
            ToIntFunction<String> coderFunction = JDKUtils.getStringCode11();

            int coder = coderFunction.applyAsInt("abc");
            assertEquals(0, coder);
        }
    }

    @Test
    public void test_unsafe_isAscii() throws Throwable {
        assertEquals(1, UnsafeUtils.getStringCoder("中国"));

        String str1 = "abc";
        if (JDKUtils.JVM_VERSION == 8) {
            assertEquals(1, UnsafeUtils.getStringCoder(str1));
        } else {
            assertEquals(0, UnsafeUtils.getStringCoder(str1));
            byte[] value = UnsafeUtils.getStringValue(str1);
            assertNotNull(value);
            assertArrayEquals(str1.getBytes(StandardCharsets.UTF_8), value);
        }
    }

    public String formatYYYYMMDD(Calendar calendar) throws Throwable {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);


        byte y0 = (byte) (year / 1000 + '0');
        byte y1 = (byte) ((year / 100) % 10 + '0');
        byte y2 = (byte) ((year / 10) % 10 + '0');
        byte y3 = (byte) (year % 10 + '0');
        byte m0 = (byte) (month / 10 + '0');
        byte m1 = (byte) (month % 10 + '0');
        byte d0 = (byte) (dayOfMonth / 10 + '0');
        byte d1 = (byte) (dayOfMonth % 10 + '0');

        if (JDKUtils.JVM_VERSION >= 9) {
            byte[] bytes = new byte[]{y0, y1, y2, y3, m0, m1, d0, d1};

            if (JDKUtils.JVM_VERSION == 17) {
                return JDKUtils.getStringCreatorJDK17().apply(bytes, StandardCharsets.US_ASCII);
            }

            if (JDKUtils.JVM_VERSION <= 11) {
                return JDKUtils.getStringCreatorJDK11().apply(bytes);
            }

            return new String(bytes, StandardCharsets.US_ASCII);
        }

        char[] chars = new char[]{
                (char) y0,
                (char) y1,
                (char) y2,
                (char) y3,
                (char) m0,
                (char) m1,
                (char) d0,
                (char) d1
        };

        if (JDKUtils.JVM_VERSION == 8) {
            return JDKUtils.getStringCreatorJDK8().apply(chars, true);
        }

        return new String(chars);
    }
}
