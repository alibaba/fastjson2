package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CharValueArrayTest {
    @Test
    public void test_parse_null() {
        char[] bytes = JSON.parseObject("null", char[].class);
        assertNull(bytes);
    }

    @Test
    public void test_parse_null_jsonb() {
        char[] values = JSONB.parseObject(JSONB.toBytes((Map) null), char[].class);
        assertNull(values);
    }

    @Test
    public void test_parse_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(Arrays.asList(new Integer[]{101, 102}));
        char[] chars = JSONB.parseObject(jsonbBytes, char[].class);
        assertEquals(2, chars.length);
        assertEquals(101, chars[0]);
        assertEquals(102, chars[1]);
    }

    @Test
    public void test_parse() {
        char[] chars = JSON.parseObject("[101,102]", char[].class);
        assertEquals(2, chars.length);
        assertEquals(101, chars[0]);
        assertEquals(102, chars[1]);
    }

    @Test
    public void test_parse_str() {
        char[] chars = JSON.parseObject("[\"A\"]", char[].class);
        assertEquals(1, chars.length);
        assertEquals('A', chars[0]);
    }

    @Test
    public void test_parse_str_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonList("A"));
        char[] chars = JSONB.parseObject(jsonbBytes, char[].class);
        assertEquals(1, chars.length);
        assertEquals('A', chars[0]);
    }

    @Test
    public void test_writeNull() {
        assertEquals("{\"values\":null}",
                JSON.toJSONString(new VO(), JSONWriter.Feature.WriteNulls));
        assertEquals("{\"values\":null}",
                new String(
                        JSON.toJSONBytes(new VO(), JSONWriter.Feature.WriteNulls)));
    }

    @Test
    public void test_writeNull2() {
        assertEquals("{}",
                JSON.toJSONString(new VO2()));

        assertEquals("{\"values\":null}",
                new String(
                        JSON.toJSONBytes(new VO2(), JSONWriter.Feature.WriteNulls)));
    }

    public static class VO {
        public char[] values;
    }

    public static class VO2 {
        private char[] values;

        public char[] getValues() {
            return values;
        }

        public void setValues(char[] values) {
            this.values = values;
        }
    }
}
