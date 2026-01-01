package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LongArrayReaderTest {
    static class TestClass {
        final long[] values;

        // Constructor for final field
        public TestClass(long[] values) {
            this.values = values;
        }
    }

    @Test
    public void testLongArrayFinalField() {
        String json = "{\"values\":[1,2,3,4,5]}";
        TestClass obj = JSON.parseObject(json, TestClass.class);

        assertNotNull(obj.values);
        assertEquals(5, obj.values.length);
        assertArrayEquals(new long[]{1, 2, 3, 4, 5}, obj.values);
    }

    @Test
    public void testLongArrayEmpty() {
        String json = "{\"values\":[]}";
        TestClass obj = JSON.parseObject(json, TestClass.class);

        assertNotNull(obj.values);
        assertEquals(0, obj.values.length);
    }

    @Test
    public void testLongArrayNull() {
        String json = "{\"values\":null}";
        TestClass obj = JSON.parseObject(json, TestClass.class);

        assertNull(obj.values);
    }
}
