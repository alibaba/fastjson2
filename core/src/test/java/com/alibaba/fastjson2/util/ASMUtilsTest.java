package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.format.DateTimeParseException;

import static com.alibaba.fastjson2.internal.asm.ASMUtils.lookupParameterNames;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ASMUtilsTest {
    @Test
    public void test() throws Exception {
        assertArrayEquals(
                new String[]{"message"},
                lookupParameterNames(
                        IOException.class.getConstructor(String.class)
                )
        );
        assertArrayEquals(
                new String[]{"cause"},
                lookupParameterNames(
                        IOException.class.getConstructor(Throwable.class)
                )
        );
        assertArrayEquals(
                new String[]{"message", "cause"},
                lookupParameterNames(
                        IOException.class.getConstructor(String.class, Throwable.class)
                )
        );
    }

    @Test
    public void test1() throws Exception {
        assertArrayEquals(
                new String[]{"message", "parsedString", "errorIndex"},
                lookupParameterNames(
                        DateTimeParseException.class.getConstructor(String.class, CharSequence.class, int.class)
                )
        );
        assertArrayEquals(
                new String[]{"message", "parsedString", "errorIndex", "cause"},
                lookupParameterNames(
                        DateTimeParseException.class.getConstructor(String.class, CharSequence.class, int.class, Throwable.class)
                )
        );
    }
}
