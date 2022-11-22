package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class JSONReaderCSVTest2 {
    @Test
    public void test0() {
        String str = "\"1\",\"1.1\",\"1.2\"\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertEquals(1, jsonReader.readNumber());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(new BigDecimal("1.1"), jsonReader.readBigDecimal());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(new BigDecimal("1.2"), jsonReader.readBigDecimal());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertEquals(1, jsonReader.readNumber());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(new BigDecimal("1.1"), jsonReader.readBigDecimal());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(new BigDecimal("1.2"), jsonReader.readBigDecimal());
        }
    }

    @Test
    public void test1() {
        String str = "true,false,\"\"\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertEquals(1, jsonReader.readNumber());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(0, jsonReader.readNumber());

            assertTrue(jsonReader.nextIfMatch(','));

            assertNull(jsonReader.readBigDecimal());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertEquals(1, jsonReader.readNumber());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(0, jsonReader.readNumber());

            assertTrue(jsonReader.nextIfMatch(','));

            assertNull(jsonReader.readBigDecimal());
        }
    }

    @Test
    public void test2() {
        String str = "10e1,10E2,10e3\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertEquals(100.0D, jsonReader.readNumber());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(new BigDecimal("1000.0"), jsonReader.readBigDecimal());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(new BigDecimal("10000.0"), jsonReader.readBigDecimal());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertEquals(100.0D, jsonReader.readNumber());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(new BigDecimal("1000.0"), jsonReader.readBigDecimal());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(new BigDecimal("10000.0"), jsonReader.readBigDecimal());
        }
    }

    @Test
    public void test3_readInt32() {
        String str = "10e1,10E2,10e3\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertEquals(100, jsonReader.readInt32());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000, jsonReader.readInt32());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000, jsonReader.readInt32());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertEquals(100, jsonReader.readInt32());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000, jsonReader.readInt32());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000, jsonReader.readInt32());
        }
    }

    @Test
    public void test3_readInt32Value() {
        String str = "10e1,10E2,10e3\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertEquals(100, jsonReader.readInt32Value());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000, jsonReader.readInt32Value());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000, jsonReader.readInt32Value());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertEquals(100, jsonReader.readInt32Value());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000, jsonReader.readInt32Value());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000, jsonReader.readInt32Value());
        }
    }

    @Test
    public void test3_readInt64() {
        String str = "10e1,10E2,10e3\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertEquals(100, jsonReader.readInt64());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000, jsonReader.readInt64());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000, jsonReader.readInt64());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertEquals(100, jsonReader.readInt64());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000, jsonReader.readInt64());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000, jsonReader.readInt64());
        }
    }

    @Test
    public void test3_readInt64Value() {
        String str = "10e1,10E2,10e3\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertEquals(100, jsonReader.readInt64Value());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000, jsonReader.readInt64Value());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000, jsonReader.readInt64Value());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertEquals(100, jsonReader.readInt64Value());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000, jsonReader.readInt64Value());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000, jsonReader.readInt64Value());
        }
    }

    @Test
    public void test3_readFloatValue() {
        String str = "10e1,10E2,10e3\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertEquals(100, jsonReader.readFloatValue());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000, jsonReader.readFloatValue());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000, jsonReader.readFloatValue());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertEquals(100, jsonReader.readFloatValue());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000, jsonReader.readFloatValue());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000, jsonReader.readFloatValue());
        }
    }

    @Test
    public void test3_readFloat() {
        String str = "10e1,10E2,10e3\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertEquals(100F, jsonReader.readFloat());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000F, jsonReader.readFloat());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000F, jsonReader.readFloat());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertEquals(100F, jsonReader.readFloat());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000F, jsonReader.readFloat());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000F, jsonReader.readFloat());
        }
    }

    @Test
    public void test3_readDouble() {
        String str = "10e1,10E2,10e3\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertEquals(100D, jsonReader.readDouble());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000D, jsonReader.readDouble());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000D, jsonReader.readDouble());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertEquals(100D, jsonReader.readDouble());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(1000D, jsonReader.readDouble());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(10000D, jsonReader.readDouble());
        }
    }

    @Test
    public void test4_readBoolean() {
        String str = "true,1,0,false\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertEquals(true, jsonReader.readBool());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(true, jsonReader.readBool());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(false, jsonReader.readBool());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(false, jsonReader.readBool());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertEquals(true, jsonReader.readBool());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(true, jsonReader.readBool());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(false, jsonReader.readBool());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(false, jsonReader.readBool());
        }
    }

    @Test
    public void test4_readBooleanValue() {
        String str = "true,1,0,false\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertEquals(true, jsonReader.readBoolValue());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(true, jsonReader.readBoolValue());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(false, jsonReader.readBoolValue());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(false, jsonReader.readBoolValue());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertEquals(true, jsonReader.readBoolValue());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(true, jsonReader.readBoolValue());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(false, jsonReader.readBoolValue());

            assertTrue(jsonReader.nextIfMatch(','));

            assertEquals(false, jsonReader.readBoolValue());
        }
    }

    @Test
    public void test_str() {
        String str = "abc,\"\",cde\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertEquals("abc", jsonReader.readString());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals("", jsonReader.readString());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals("cde", jsonReader.readString());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertEquals("abc", jsonReader.readString());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals("", jsonReader.readString());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals("cde", jsonReader.readString());
        }
    }
}
