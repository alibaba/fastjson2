package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.format.DateTimeParseException;

import static com.alibaba.fastjson2.internal.asm.ASMUtils.lookupParameterNames;
import static com.alibaba.fastjson2.internal.asm.ASMUtils.sanitizeClassName;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("util")
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

    @Test
    public void sanitizeClassNameNullOrEmpty() {
        assertEquals("", sanitizeClassName(null));
        assertEquals("", sanitizeClassName(""));
    }

    @Test
    public void sanitizeClassNameUnchanged() {
        assertEquals("String", sanitizeClassName("String"));
        assertEquals("MyClass", sanitizeClassName("MyClass"));
        assertEquals("MyClass2", sanitizeClassName("MyClass2"));
        assertEquals("My_Class", sanitizeClassName("My_Class"));
    }

    @Test
    public void sanitizeClassNameArray() {
        assertEquals("ClassArray", sanitizeClassName("Class[]"));
        assertEquals("StringArray", sanitizeClassName("String[]"));
        assertEquals("intArrayArray", sanitizeClassName("int[][]"));
    }

    @Test
    public void sanitizeClassNameSpecialChars() {
        assertEquals("My_Class", sanitizeClassName("My$Class"));
        assertEquals("My_Class_2", sanitizeClassName("My-Class-2"));
        assertEquals("My_ClassArray", sanitizeClassName("My$Class[]"));
    }
}
