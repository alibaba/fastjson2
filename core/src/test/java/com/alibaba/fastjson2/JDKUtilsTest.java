package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.JDKUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
