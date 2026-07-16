package com.alibaba.fastjson2.writer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectWriterCreatorASMTest {
    @Test
    public void sanitizeClassName_null() {
        assertEquals("", ObjectWriterCreatorASM.sanitizeClassName(null));
    }

    @Test
    public void sanitizeClassName_empty() {
        assertEquals("", ObjectWriterCreatorASM.sanitizeClassName(""));
    }

    @Test
    public void sanitizeClassName_normal() {
        assertEquals("String", ObjectWriterCreatorASM.sanitizeClassName("String"));
        assertEquals("MyClass", ObjectWriterCreatorASM.sanitizeClassName("MyClass"));
        assertEquals("MyClass2", ObjectWriterCreatorASM.sanitizeClassName("MyClass2"));
    }

    @Test
    public void sanitizeClassName_array() {
        assertEquals("ClassArray", ObjectWriterCreatorASM.sanitizeClassName("Class[]"));
        assertEquals("StringArray", ObjectWriterCreatorASM.sanitizeClassName("String[]"));
        assertEquals("IntegerArray", ObjectWriterCreatorASM.sanitizeClassName("Integer[]"));
    }

    @Test
    public void sanitizeClassName_2dArray() {
        assertEquals("intArrayArray", ObjectWriterCreatorASM.sanitizeClassName("int[][]"));
    }

    @Test
    public void sanitizeClassName_specialChars() {
        assertEquals("My_Class", ObjectWriterCreatorASM.sanitizeClassName("My$Class"));
        assertEquals("My_Class_2", ObjectWriterCreatorASM.sanitizeClassName("My-Class-2"));
    }
}
