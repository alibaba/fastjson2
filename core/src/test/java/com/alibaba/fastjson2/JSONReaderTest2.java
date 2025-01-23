package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.util.JDKUtils.ARRAY_BYTE_BASE_OFFSET;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;
import static org.junit.jupiter.api.Assertions.*;

public class JSONReaderTest2 {
    @Test
    public void nextIfMatch() {
        String str = "a";
        char c = 'a';
        assertTrue(JSONReader.of(str.toCharArray()).nextIfMatch(c));
        assertTrue(JSONReader.of(str.getBytes(StandardCharsets.UTF_8)).nextIfMatch(c));
        assertTrue(JSONReader.of(str).nextIfMatch(c));
    }

    @Test
    public void nextIfMatch_pre_empty() {
        String str = "  a";
        char c = 'a';
        assertTrue(JSONReader.of(str.toCharArray()).nextIfMatch(c));
        assertTrue(JSONReader.of(str.getBytes(StandardCharsets.UTF_8)).nextIfMatch(c));
        assertTrue(JSONReader.of(str).nextIfMatch(c));
    }

    @Test
    public void nextIfMatch_empty() {
        String str = "  a   ";
        char c = 'a';
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfMatch(c));
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfMatch(c));
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfMatch(c));
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void nextIfMatch_empty_comment() {
        String str = "  a  // ";
        char c = 'a';
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfMatch(c));
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfMatch(c));
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfMatch(c));
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void nextIfMatch_utf8() {
        String str = ",中";
        char c = ',';
        char ch = '中';
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfMatch(c));
            assertEquals(ch, jsonReader.current());
            assertFalse(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfMatch(c));
            assertEquals(ch, jsonReader.current());
            assertFalse(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfMatch(c));
            assertEquals(ch, jsonReader.current());
            assertFalse(jsonReader.isEnd());
        }
    }

    @Test
    public void nextIfComma_utf8() {
        String str = ",中";
        char ch = '中';
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfComma());
            assertEquals(ch, jsonReader.current());
            assertFalse(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfComma());
            assertEquals(ch, jsonReader.current());
            assertFalse(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfComma());
            assertEquals(ch, jsonReader.current());
            assertFalse(jsonReader.isEnd());
        }
    }

    @Test
    public void nextIfName4Match2() {
        String str = "{\"a2\":1}";
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match2());
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match2());
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match2());
            assertEquals('1', jsonReader.current());
        }
    }

    @Test
    public void nextIfName4Match2_1() {
        String str = "{ \"a2\": 1}";
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match2());
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match2());
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match2());
            assertEquals('1', jsonReader.current());
        }
    }

    @Test
    public void nextIfName4Match2_false() {
        String str = "{\"a\":1}";
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertFalse(jsonReader.nextIfName4Match2());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertFalse(jsonReader.nextIfName4Match2());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertFalse(jsonReader.nextIfName4Match2());
        }
    }

    @Test
    public void nextIfName4Match2_false1() {
        String str = "{\"a23\":1}";
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertFalse(jsonReader.nextIfName4Match2());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertFalse(jsonReader.nextIfName4Match2());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertFalse(jsonReader.nextIfName4Match2());
        }
    }

    @Test
    public void nextIfName4Match4() {
        String str = "{\"a234\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        byte name0 = utf8[5];
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match4(name0));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match4(name0));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match4(name0));
            assertEquals('1', jsonReader.current());
        }
    }

    @Test
    public void nextIfName4Match5() {
        String str = "{\"a2345\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        int name0 = UNSAFE.getInt(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match5(name0));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match5(name0));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match5(name0));
            assertEquals('1', jsonReader.current());
        }
    }

    @Test
    public void nextIfName4Match6() {
        String str = "{\"a23456\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        int name1 = UNSAFE.getInt(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match6(name1));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match6(name1));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match6(name1));
            assertEquals('1', jsonReader.current());
        }
    }

    @Test
    public void nextIfName4Match7() {
        String str = "{\"a234567\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        int name1 = UNSAFE.getInt(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match7(name1));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match7(name1));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match7(name1));
            assertEquals('1', jsonReader.current());
        }
    }

    @Test
    public void nextIfName4Match8() {
        String str = "{\"a2345678\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        int name1 = UNSAFE.getInt(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        byte name2 = '8';
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match8(name1, name2));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match8(name1, name2));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match8(name1, name2));
            assertEquals('1', jsonReader.current());
        }
    }

    @Test
    public void nextIfName4Match9() {
        String str = "{\"a23456789\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match9(name1));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match9(name1));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match9(name1));
            assertEquals('1', jsonReader.current());
        }
    }

    @Test
    public void nextIfName4Match10() {
        String str = "{\"a234567890\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match10(name1));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match10(name1));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match10(name1));
            assertEquals('1', jsonReader.current());
        }
    }

    @Test
    public void nextIfName4Match11() {
        String str = "{\"a2345678901\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match11(name1));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match11(name1));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match11(name1));
            assertEquals('1', jsonReader.current());
        }
    }

    @Test
    public void nextIfName4Match12() {
        String str = "{\"a23456789012\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        byte name2 = utf8[13];
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match12(name1, name2));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match12(name1, name2));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match12(name1, name2));
            assertEquals('1', jsonReader.current());
        }
    }

    @Test
    public void nextIfName4Match13() {
        String str = "{\"a234567890123\":   1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        int name2 = UNSAFE.getInt(utf8, ARRAY_BYTE_BASE_OFFSET + 13);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match13(name1, name2));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match13(name1, name2));
            assertEquals('1', jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match13(name1, name2));
            assertEquals('1', jsonReader.current());
        }
    }

    @Test
    public void nextIfName4Match14() {
        String str = "{\"a2345678901234\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        int name2 = UNSAFE.getInt(utf8, ARRAY_BYTE_BASE_OFFSET + 13);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match14(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match14(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match14(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void nextIfName4Match15() {
        String str = "{\"a23456789012345\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        int name2 = UNSAFE.getInt(utf8, ARRAY_BYTE_BASE_OFFSET + 13);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match15(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match15(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match15(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void nextIfName4Match16() {
        String str = "{\"a234567890123456\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        int name2 = UNSAFE.getInt(utf8, ARRAY_BYTE_BASE_OFFSET + 13);
        byte name3 = utf8[17];
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match16(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match16(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match16(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void nextIfName4Match17() {
        String str = "{\"a2345678901234567\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        long name2 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 13);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match17(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match17(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match17(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void nextIfName4Match18() {
        String str = "{\"a23456789012345678\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        long name2 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 13);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match18(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match18(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match18(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void nextIfName4Match19() {
        String str = "{\"a234567890123456789\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        long name2 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 13);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match19(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match19(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match19(name1, name2));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void nextIfName4Match20() {
        String str = "{\"a2345678901234567890\":   1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        long name2 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 13);
        byte name3 = utf8[21];
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match20(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match20(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match20(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void nextIfName4Match21() {
        String str = "{\"a23456789012345678901\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        long name2 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 13);
        int name3 = UNSAFE.getInt(utf8, ARRAY_BYTE_BASE_OFFSET + 21);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match21(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match21(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match21(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void nextIfName4Match22() {
        String str = "{\"a234567890123456789012\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        long name2 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 13);
        int name3 = UNSAFE.getInt(utf8, ARRAY_BYTE_BASE_OFFSET + 21);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match22(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match22(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match22(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void nextIfName4Match23() {
        String str = "{\"a2345678901234567890123\":  1}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        long name1 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 5);
        long name2 = UNSAFE.getLong(utf8, ARRAY_BYTE_BASE_OFFSET + 13);
        int name3 = UNSAFE.getInt(utf8, ARRAY_BYTE_BASE_OFFSET + 21);
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match23(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match23(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.nextIfObjectStart());
            assertTrue(jsonReader.nextIfName4Match23(name1, name2, name3));
            assertEquals('1', jsonReader.current());
            assertEquals(1, jsonReader.readInt32Value());
            assertTrue(jsonReader.nextIfObjectEnd());
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void nameX() throws Exception {
        char[] ascii = "a234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890".toCharArray();
        int value = 123;
        for (int nameLength = 3; nameLength < 44; nameLength++) {
            String[] names = new String[]{
                    new String(ascii, 0, nameLength)
            };

            for (String name : names) {
                assertEquals(nameLength, name.length());
                String json = JSONObject.of(name, value).toJSONString();
                byte[] jsonbBytes = json.getBytes(StandardCharsets.UTF_8);
                int name0 = UNSAFE.getInt(jsonbBytes, ARRAY_BYTE_BASE_OFFSET + 1);

                JSONReader[] jsonReaders = new JSONReader[] {
                        JSONReader.of(jsonbBytes),
                        JSONReader.of(json.toCharArray())
                };

                for (JSONReader jsonReader : jsonReaders) {
                    assertTrue(jsonReader.nextIfObjectStart());
                    assertEquals(name0, jsonReader.getRawInt());

                    Method method = null;
                    String methodName = "nextIfName4Match" + nameLength;
                    for (Method m : JSONReader.class.getMethods()) {
                        if (m.getName().equals(methodName)) {
                            method = m;
                            break;
                        }
                    }
                    assertNotNull(method);

                    Object[] args = new Object[method.getParameterCount()];
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    int byteIndex = 3;
                    for (int i = 0; i < parameterTypes.length; i++) {
                        Class<?> paramType = parameterTypes[i];
                        if (paramType == byte.class) {
                            args[i] = jsonbBytes[byteIndex + 2];
                            byteIndex++;
                        } else if (paramType == int.class) {
                            byte[] bytes = Arrays.copyOfRange(jsonbBytes, byteIndex + 2, byteIndex + 6);
                            if (nameLength - byteIndex == 2) {
                                byteIndex += 2;
                            } else if (nameLength - byteIndex == 3) {
                                byteIndex += 3;
                            } else {
                                byteIndex += 4;
                            }
                            args[i] = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET);
                        } else if (paramType == long.class) {
                            byte[] bytes = Arrays.copyOfRange(jsonbBytes, byteIndex + 2, byteIndex + 10);
                            if (nameLength - byteIndex == 6) {
                                byteIndex += 6;
                            } else if (nameLength - byteIndex == 7) {
                                byteIndex += 7;
                            } else {
                                byteIndex += 8;
                            }
                            args[i] = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET);
                        } else {
                            throw new UnsupportedOperationException();
                        }
                    }

                    assertEquals(Boolean.TRUE, method.invoke(jsonReader, args), methodName);

                    assertEquals(value, jsonReader.readInt32Value());
                    assertTrue(jsonReader.nextIfObjectEnd());
                }
            }
        }
    }

    @Test
    public void skipValueNumber() {
        String str = "123";
        JSONReader.of(str.toCharArray())
                .skipValue();
        JSONReader.of(str.getBytes(StandardCharsets.UTF_8))
                .skipValue();
    }

    @Test
    public void skipValueNumber1() {
        String str = "123.";
        JSONReader.of(str.toCharArray())
                .skipValue();
        JSONReader.of(str.getBytes(StandardCharsets.UTF_8))
                .skipValue();
    }

    @Test
    public void skipValueNumber2() {
        String str = "123.34";
        JSONReader.of(str.toCharArray())
                .skipValue();
        JSONReader.of(str.getBytes(StandardCharsets.UTF_8))
                .skipValue();
    }

    @Test
    public void skipValueNumber3() {
        String str = ".34";
        JSONReader.of(str.toCharArray())
                .skipValue();
        JSONReader.of(str.getBytes(StandardCharsets.UTF_8))
                .skipValue();
    }

    @Test
    public void skipValueNumber4() {
        String str = "1.34e123";
        JSONReader.of(str.toCharArray())
                .skipValue();
        JSONReader.of(str.getBytes(StandardCharsets.UTF_8))
                .skipValue();
    }

    @Test
    public void skipValueNumberError() {
        String str = "1.34e";
        assertThrows(
                Exception.class,
                () -> JSONReader.of(str.toCharArray())
                        .skipValue());
        assertThrows(
                Exception.class,
                () -> JSONReader.of(str.getBytes(StandardCharsets.UTF_8))
                        .skipValue());
    }

    @Test
    public void skipValueNumberError1() {
        String str = "1e";
        assertThrows(
                Exception.class,
                () -> JSONReader.of(str.toCharArray())
                        .skipValue());
        assertThrows(
                Exception.class,
                () -> JSONReader.of(str.getBytes(StandardCharsets.UTF_8))
                        .skipValue());
    }

    @Test
    public void skipValueNumberError2() {
        String str = "1234.45a";
        assertThrows(
                Exception.class,
                () -> JSONReader.of(str.toCharArray())
                        .skipValue());
        assertThrows(
                Exception.class,
                () -> JSONReader.of(str.getBytes(StandardCharsets.UTF_8))
                        .skipValue());
    }

    @Test
    public void skipValueString0() {
        String str = "\"1234\"";
        JSONReader.of(str.toCharArray())
                .skipValue();
        JSONReader.of(str.getBytes(StandardCharsets.UTF_8))
                .skipValue();
    }

    @Test
    public void skipValueString1() {
        String str = "'1234'";
        JSONReader.of(str.toCharArray())
                .skipValue();
        JSONReader.of(str.getBytes(StandardCharsets.UTF_8))
                .skipValue();
    }

    @Test
    public void skipValueStringError() {
        String str = "\"1234.45\"a";
        assertThrows(
                Exception.class,
                () -> JSONReader.of(str.toCharArray())
                        .skipValue());
        assertThrows(
                Exception.class,
                () -> JSONReader.of(str.getBytes(StandardCharsets.UTF_8))
                        .skipValue());
    }

    @Test
    public void skipValueTrue() {
        String str = "true";
        JSONReader.of(str.toCharArray())
                .skipValue();
        JSONReader.of(str.getBytes(StandardCharsets.UTF_8))
                .skipValue();
    }

    @Test
    public void skipValueFalse() {
        String str = "false";
        JSONReader.of(str.toCharArray())
                .skipValue();
        JSONReader.of(str.getBytes(StandardCharsets.UTF_8))
                .skipValue();
    }

    @Test
    public void readInt64Value() {
        long value = 123;
        String str = value + "L,";
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            assertEquals(value, jsonReader.readInt64Value());
            assertTrue(jsonReader.comma);
            assertTrue(jsonReader.isEnd());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertEquals(value, jsonReader.readInt64Value());
            assertTrue(jsonReader.comma);
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void readInt64ValueOverflow() {
        BigDecimal decimal = new BigDecimal("+1234567890123456789012345678901234567890");
        String str = decimal + ",";
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt64Value);
        }
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt64);
        }

        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt64Value);
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt64);
        }
    }

    @Test
    public void readInt64ValueOverflow1() {
        BigDecimal decimal = new BigDecimal("1234567890123456789012345678901234567890");
        String str = "+" + decimal + "L,";
        long value = decimal.longValue();
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt64Value);
        }
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt64);
        }

        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt64Value);
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt64);
        }
    }

    @Test
    public void readInt64ValueOverflow2() {
        BigDecimal decimal = new BigDecimal("123456789012345678901234567890");
        String str = decimal + ",";
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt64Value);
        }
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt64);
        }

        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt64Value);
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt64);
        }
    }

    @Test
    public void readInt32ValueOverflow() {
        BigDecimal decimal = new BigDecimal("1234567890123456789012345678901234567890");
        String str = decimal + ",";
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt32Value);
        }
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt32);
        }

        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt32Value);
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt32);
        }
    }

    @Test
    public void readInt32ValueOverflow1() {
        BigDecimal decimal = new BigDecimal("1234567890123456789012345678901234567890");
        String str = "+" + decimal + "L,";
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt32Value);
        }
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt32);
        }

        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt32Value);
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt32);
        }
    }

    @Test
    public void readInt32ValueOverflow2() {
        BigDecimal decimal = new BigDecimal("12345678901234567890");
        String str = "+" + decimal + "L,";
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt32Value);
        }
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt32);
        }

        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt32Value);
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertThrows(
                    JSONException.class,
                    jsonReader::readInt32);
        }
    }

    @Test
    public void readFieldNameHashCodeUnquote() {
        String key = "©®£";
        String str = "{" + key + ":123}";
        long expected = Fnv.hashCode64(key);
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            jsonReader.nextIfObjectStart();
            assertEquals(
                    expected,
                    jsonReader.readFieldNameHashCodeUnquote());
        }
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes);
            jsonReader.nextIfObjectStart();
            assertEquals(
                    expected,
                    jsonReader.readFieldNameHashCodeUnquote());
        }
        {
            byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1);
            jsonReader.nextIfObjectStart();
            assertEquals(
                    expected,
                    jsonReader.readFieldNameHashCodeUnquote());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            jsonReader.nextIfObjectStart();
            assertEquals(expected, jsonReader.readFieldNameHashCodeUnquote());
        }
    }

    @Test
    public void readFieldNameHashCodeUnquote1() {
        String key = "中国";
        String str = "{" + key + ":123}";
        long expected = Fnv.hashCode64(key);
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            jsonReader.nextIfObjectStart();
            assertEquals(expected, jsonReader.readFieldNameHashCodeUnquote());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            jsonReader.nextIfObjectStart();
            assertEquals(expected, jsonReader.readFieldNameHashCodeUnquote());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            jsonReader.nextIfObjectStart();
            assertEquals(expected, jsonReader.readFieldNameHashCodeUnquote());
        }
    }

    @Test
    public void readFieldNameHashCodeUnquote2() {
        String key = "中国©®£";
        String str = "{" + key + ":123}";
        long expected = Fnv.hashCode64(key);
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            jsonReader.nextIfObjectStart();
            assertEquals(expected, jsonReader.readFieldNameHashCodeUnquote());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            jsonReader.nextIfObjectStart();
            assertEquals(expected, jsonReader.readFieldNameHashCodeUnquote());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            jsonReader.nextIfObjectStart();
            assertEquals(expected, jsonReader.readFieldNameHashCodeUnquote());
        }
    }

    @Test
    public void readFieldNameHashCodeUnquote3() {
        String key = "a\uD83D\uDE0D\uD83D\uDC81";
        String str = "{" + key + ":123}";
        long expected = Fnv.hashCode64(key);
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            jsonReader.nextIfObjectStart();
            assertEquals(expected, jsonReader.readFieldNameHashCodeUnquote());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            jsonReader.nextIfObjectStart();
            assertEquals(expected, jsonReader.readFieldNameHashCodeUnquote());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            jsonReader.nextIfObjectStart();
            assertEquals(expected, jsonReader.readFieldNameHashCodeUnquote());
        }
    }
//
//    @Test
//    public void readFieldNameHashCodeUnquote4() {
//        String key = "\uD83D\uDE0D\uD83D\uDC81";
//        String str = "{" + key + ":123}";
//        long expected = Fnv.hashCode64(key);
//        {
//            JSONReader jsonReader = JSONReader.of(str.toCharArray());
//            jsonReader.nextIfObjectStart();
//            assertEquals(expected, jsonReader.readFieldNameHashCodeUnquote());
//        }
//        {
//            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
//            jsonReader.nextIfObjectStart();
//            assertEquals(expected, jsonReader.readFieldNameHashCodeUnquote());
//        }
//        {
//            JSONReader jsonReader = JSONReader.of(str);
//            jsonReader.nextIfObjectStart();
//            assertEquals(expected, jsonReader.readFieldNameHashCodeUnquote());
//        }
//    }

    @Test
    public void readFieldName() {
        String key = "中国©®£";
        String str = "{\"" + key + "\":123}";
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            jsonReader.nextIfObjectStart();
            assertEquals(key, jsonReader.readFieldName());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            jsonReader.nextIfObjectStart();
            assertEquals(key, jsonReader.readFieldName());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            jsonReader.nextIfObjectStart();
            assertEquals(key, jsonReader.readFieldName());
        }
    }

    @Test
    public void readFieldName1() {
        String key = "\uD83D\uDE0D\uD83D\uDC81";
        String str = "{\"" + key + "\":123}";
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            jsonReader.nextIfObjectStart();
            assertEquals(key, jsonReader.readFieldName());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            jsonReader.nextIfObjectStart();
            assertEquals(key, jsonReader.readFieldName());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            jsonReader.nextIfObjectStart();
            assertEquals(key, jsonReader.readFieldName());
        }
    }

    @Test
    public void nextIfObjectStart() {
        char key = '中';
        String str = "{" + key + ":123}";
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            jsonReader.nextIfObjectStart();
            assertEquals(key, jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            jsonReader.nextIfObjectStart();
            assertEquals(key, jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            jsonReader.nextIfObjectStart();
            assertEquals(key, jsonReader.current());
        }
    }

    @Test
    public void nextIfObjectStart1() {
        char key = '£';
        String str = "{" + key + ":123}";
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            jsonReader.nextIfObjectStart();
            assertEquals(key, jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            jsonReader.nextIfObjectStart();
            assertEquals(key, jsonReader.current());
        }
        {
            byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1);
            jsonReader.nextIfObjectStart();
            assertEquals(key, jsonReader.current());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            jsonReader.nextIfObjectStart();
            assertEquals(key, jsonReader.current());
        }
    }

    @Test
    public void isReference() {
        String str = "{\"$ref\":\"$\"}";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        {
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertTrue(jsonReader.isReference());
            assertEquals("$", jsonReader.readReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1);
            assertTrue(jsonReader.isReference());
            assertEquals("$", jsonReader.readReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.isReference());
            assertEquals("$", jsonReader.readReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.isReference());
            assertEquals("$", jsonReader.readReference());
        }
    }

    @Test
    public void isReference1() {
        String str = "{   \"$ref\"  :  \"$\"}";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        {
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertTrue(jsonReader.isReference());
            assertEquals("$", jsonReader.readReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1);
            assertTrue(jsonReader.isReference());
            assertEquals("$", jsonReader.readReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertTrue(jsonReader.isReference());
            assertEquals("$", jsonReader.readReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertTrue(jsonReader.isReference());
            assertEquals("$", jsonReader.readReference());
        }
    }

    @Test
    public void isReference_false() {
        String str = "{   \"$ref\"  :";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        {
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertFalse(jsonReader.isReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1);
            assertFalse(jsonReader.isReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertFalse(jsonReader.isReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertFalse(jsonReader.isReference());
        }
    }

    @Test
    public void isReference_false_2() {
        String str = "{   \"$ref\"";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        {
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertFalse(jsonReader.isReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1);
            assertFalse(jsonReader.isReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertFalse(jsonReader.isReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertFalse(jsonReader.isReference());
        }
    }

    @Test
    public void isReference_false_1() {
        String str = "{   \"$ref\"  , ";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        {
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertFalse(jsonReader.isReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1);
            assertFalse(jsonReader.isReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertFalse(jsonReader.isReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertFalse(jsonReader.isReference());
        }
    }

    @Test
    public void isReference1_false_3() {
        String str = "{   \"$ref\"  :  \"#\"}";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        {
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertFalse(jsonReader.isReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1);
            assertFalse(jsonReader.isReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(str.toCharArray());
            assertFalse(jsonReader.isReference());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            assertFalse(jsonReader.isReference());
        }
    }

    @Test
    public void setFeatures() {
        JSONReader.Context context = JSONFactory.createReadContext();
        long features = 123456L;
        context.setFeatures(features);
        assertEquals(features, context.getFeatures());
    }
}
