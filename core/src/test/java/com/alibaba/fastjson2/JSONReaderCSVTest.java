package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class JSONReaderCSVTest {
    @Test
    public void test() {
        String str = "abc\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
            assertTrue(jsonReader.isString());
            assertEquals(3, jsonReader.getStringLength());
        }
        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
            assertTrue(jsonReader.isString());
            assertEquals(3, jsonReader.getStringLength());
        }
    }

    @Test
    public void test1() {
        String str = "abc,123\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
            assertTrue(jsonReader.isString());
            assertEquals(3, jsonReader.getStringLength());
            assertEquals("abc", jsonReader.readString());
            assertTrue(jsonReader.nextIfMatch(','));
        }
        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
            assertTrue(jsonReader.isString());
            assertEquals(3, jsonReader.getStringLength());
            assertEquals("abc", jsonReader.readString());
            assertTrue(jsonReader.nextIfMatch(','));
        }
    }

    @Test
    public void test2() {
        String str = ",123\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
            assertFalse(jsonReader.isString());
        }
        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
            assertFalse(jsonReader.isString());
        }
    }

    @Test
    public void test4() {
        String str = "\"Free trip to A,B\",\"5.89\",\"Special rate \"\"1.79\"\"\"";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertTrue(jsonReader.isString());
            assertEquals(17, jsonReader.getStringLength());
            assertEquals("Free trip to A,B", jsonReader.readString());

            assertTrue(jsonReader.nextIfMatch(','));

            assertTrue(jsonReader.isString());
            assertEquals(5, jsonReader.getStringLength());
            assertEquals("5.89", jsonReader.readString());

            assertTrue(jsonReader.nextIfMatch(','));

            assertTrue(jsonReader.isString());
            assertEquals(22, jsonReader.getStringLength());
            assertEquals("Special rate \"1.79\"", jsonReader.readString());
        }
        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
            assertTrue(jsonReader.isString());
            assertEquals(17, jsonReader.getStringLength());
            assertEquals("Free trip to A,B", jsonReader.readString());

            assertTrue(jsonReader.nextIfMatch(','));

            assertTrue(jsonReader.isString());
            assertEquals(5, jsonReader.getStringLength());
            assertEquals("5.89", jsonReader.readString());

            assertTrue(jsonReader.nextIfMatch(','));

            assertTrue(jsonReader.isString());
            assertEquals(22, jsonReader.getStringLength());
            assertEquals("Special rate \"1.79\"", jsonReader.readString());
        }
    }

    @Test
    public void test5() {
        String str = "1,1.1,1.2\n2,2.1,2.2\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);

            assertTrue(jsonReader.isInt());
            assertEquals(1, jsonReader.readInt32());

            assertTrue(jsonReader.nextIfMatch(','));

            assertTrue(jsonReader.isNumber());
            assertEquals(new BigDecimal("1.1"), jsonReader.readBigDecimal());

            assertTrue(jsonReader.nextIfMatch(','));

            assertTrue(jsonReader.isNumber());
            assertEquals(new BigDecimal("1.2"), jsonReader.readBigDecimal());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);

            assertTrue(jsonReader.isInt());
            assertEquals(1, jsonReader.readInt32());

            assertTrue(jsonReader.nextIfMatch(','));

            assertTrue(jsonReader.isNumber());
            assertEquals(new BigDecimal("1.1"), jsonReader.readBigDecimal());

            assertTrue(jsonReader.nextIfMatch(','));

            assertTrue(jsonReader.isNumber());
            assertEquals(new BigDecimal("1.2"), jsonReader.readBigDecimal());
        }
    }

    @Test
    public void test6() {
        String str = ",,,\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
            assertNull(jsonReader.readInt32());
            assertTrue(jsonReader.nextIfMatch(','));
            assertNull(jsonReader.readInt64());
            assertTrue(jsonReader.nextIfMatch(','));
            assertNull(jsonReader.readNumber());
            assertTrue(jsonReader.nextIfMatch(','));
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
            assertNull(jsonReader.readInt32());
            assertTrue(jsonReader.nextIfMatch(','));
            assertNull(jsonReader.readInt64());
            assertTrue(jsonReader.nextIfMatch(','));
            assertNull(jsonReader.readNumber());
            assertTrue(jsonReader.nextIfMatch(','));
        }
    }

    @Test
    public void test7() {
        String str = "2022-07-14,2022-07-14 12:13:14,2022-08-07T12:00:33.107787800Z\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
            assertTrue(jsonReader.isCSV());
            assertTrue(jsonReader.isArray());
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 8, 7), jsonReader.readZonedDateTime().toLocalDate());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
            assertTrue(jsonReader.isCSV());
            assertTrue(jsonReader.isArray());
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 8, 7), jsonReader.readZonedDateTime().toLocalDate());
        }
    }

    @Test
    public void test8() {
        String str = "1,2,3,4\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(2, jsonReader.readInt64Value());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(3, jsonReader.readNumber());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(BigInteger.valueOf(4), jsonReader.readBigInteger());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(2, jsonReader.readInt64Value());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(3, jsonReader.readNumber());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(BigInteger.valueOf(4), jsonReader.readBigInteger());
        }
    }

    @Test
    public void test9() {
        String str = "2022-07-14 12:13:14Z,2022-07-14 12:13:14+08:00,2022-11-15T00:00:00.000+08:00\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
            assertTrue(jsonReader.isCSV());
            assertTrue(jsonReader.isArray());
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 11, 15), jsonReader.readZonedDateTime().toLocalDate());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
            assertTrue(jsonReader.isCSV());
            assertTrue(jsonReader.isArray());
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 11, 15), jsonReader.readZonedDateTime().toLocalDate());
        }
    }

    @Test
    public void test10() {
        String str = "2022/07/14,2022-07-14,14.07.2022,14-07-2022\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
            assertTrue(jsonReader.isCSV());
            assertTrue(jsonReader.isArray());
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readLocalDate());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
            assertTrue(jsonReader.isCSV());
            assertTrue(jsonReader.isArray());
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readLocalDate());
        }
    }

    @Test
    public void test11() {
        String str = "2022-07-14 12:13:14.1Z,2022-07-14 12:13:14.01Z,2022-07-14 12:13:14.001Z,2022-07-14 12:13:14.0001Z\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
            assertTrue(jsonReader.isCSV());
            assertTrue(jsonReader.isArray());
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
            assertTrue(jsonReader.isCSV());
            assertTrue(jsonReader.isArray());
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
        }
    }

    @Test
    public void test12() {
        String str = "2022-07-14 12:13:14.00001Z,2022-07-14 12:13:14.000001Z,2022-07-14 12:13:14.0000001Z,2022-07-14 12:13:14.00000001Z\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
            assertTrue(jsonReader.isCSV());
            assertTrue(jsonReader.isArray());
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
            assertTrue(jsonReader.isCSV());
            assertTrue(jsonReader.isArray());
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
        }
    }

    @Test
    public void test13() {
        String str = "2022-07-14 12:13:14.000001,2022-07-14 12:13:14.0000001,2022-07-14 12:13:14.00000001,2022-07-14 12:13:14.000000001\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
            assertTrue(jsonReader.isCSV());
            assertTrue(jsonReader.isArray());
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
            assertTrue(jsonReader.isCSV());
            assertTrue(jsonReader.isArray());
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readZonedDateTime().toLocalDate());
        }
    }

    @Test
    public void test14() {
        String str = "2022年7月14日,2022年11月2日,2022년7월14일,2022년11월2일\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
            assertTrue(jsonReader.isCSV());
            assertTrue(jsonReader.isArray());
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 11, 2), jsonReader.readLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 7, 14), jsonReader.readLocalDate());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(LocalDate.of(2022, 11, 2), jsonReader.readLocalDate());
        }
    }

    @Test
    public void test15() {
        String str = "null,null,null,null\n";
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
            assertEquals(0, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(0, jsonReader.readInt64Value());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(null, jsonReader.readNumber());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(null, jsonReader.readBigInteger());
        }

        {
            byte[] bytes = str.getBytes();
            JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
            assertEquals(0, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(0, jsonReader.readInt64Value());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(null, jsonReader.readNumber());
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals(null, jsonReader.readBigInteger());
        }
    }
}
