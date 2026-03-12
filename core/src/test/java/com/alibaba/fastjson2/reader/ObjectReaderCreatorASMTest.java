package com.alibaba.fastjson2.reader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectReaderCreatorASMTest {
    @Test
    public void sanitizeClassName_null() {
        assertEquals("", ObjectReaderCreatorASM.sanitizeClassName(null));
    }

    @Test
    public void sanitizeClassName_empty() {
        assertEquals("", ObjectReaderCreatorASM.sanitizeClassName(""));
    }

    @Test
    public void sanitizeClassName_normal() {
        assertEquals("String", ObjectReaderCreatorASM.sanitizeClassName("String"));
        assertEquals("MyClass", ObjectReaderCreatorASM.sanitizeClassName("MyClass"));
        assertEquals("MyClass2", ObjectReaderCreatorASM.sanitizeClassName("MyClass2"));
    }

    @Test
    public void sanitizeClassName_array() {
        assertEquals("ClassArray", ObjectReaderCreatorASM.sanitizeClassName("Class[]"));
        assertEquals("StringArray", ObjectReaderCreatorASM.sanitizeClassName("String[]"));
        assertEquals("IntegerArray", ObjectReaderCreatorASM.sanitizeClassName("Integer[]"));
    }

    @Test
    public void sanitizeClassName_2dArray() {
        assertEquals("intArrayArray", ObjectReaderCreatorASM.sanitizeClassName("int[][]"));
    }

    @Test
    public void sanitizeClassName_specialChars() {
        assertEquals("My_Class", ObjectReaderCreatorASM.sanitizeClassName("My$Class"));
        assertEquals("My_Class_2", ObjectReaderCreatorASM.sanitizeClassName("My-Class-2"));
    }
}
