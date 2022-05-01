package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BooleanArrayTest {
    @Test
    public void test_parse_null() {
        Boolean[] bytes = JSON.parseObject("null", Boolean[].class);
        assertNull(bytes);
    }

    @Test
    public void test_parse_null_jsonb() {
        Boolean[] values = JSONB.parseObject(JSONB.toBytes((Map) null), Boolean[].class);
        assertNull(values);
    }

    @Test
    public void test_parse() {
        Boolean[] array = JSON.parseObject("[1,0,null,true,false]", Boolean[].class);
        assertEquals(5, array.length);
        assertEquals(Boolean.TRUE, array[0]);
        assertEquals(Boolean.FALSE, array[1]);
        assertNull(array[2]);
        assertEquals(Boolean.TRUE, array[3]);
        assertEquals(Boolean.FALSE, array[4]);
    }

    @Test
    public void test_parse_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(Arrays.asList(new Object[] {1, 0, null, true, false}));
        Boolean[] array = JSONB.parseObject(jsonbBytes, Boolean[].class);
        assertEquals(Boolean.TRUE, array[0]);
        assertEquals(Boolean.FALSE, array[1]);
        assertNull(array[2]);
        assertEquals(Boolean.TRUE, array[3]);
        assertEquals(Boolean.FALSE, array[4]);
    }
}
