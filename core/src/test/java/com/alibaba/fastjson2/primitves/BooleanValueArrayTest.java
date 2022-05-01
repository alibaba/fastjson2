package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BooleanValueArrayTest {
    @Test
    public void test_parse_null() {
        boolean[] bytes = JSON.parseObject("null", boolean[].class);
        assertNull(bytes);
    }

    @Test
    public void test_parse_null_jsonb() {
        boolean[] values = JSONB.parseObject(JSONB.toBytes((Map) null), boolean[].class);
        assertNull(values);
    }

    @Test
    public void test_parse() {
        boolean[] array = JSON.parseObject("[1,0,false,true,false]", boolean[].class);
        assertEquals(5, array.length);
        assertEquals(true, array[0]);
        assertEquals(false, array[1]);
        assertEquals(false, array[2]);
        assertEquals(true, array[3]);
        assertEquals(false, array[4]);
    }

    @Test
    public void test_parse_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(Arrays.asList(new Object[]{1, 0, null, true, false}));
        boolean[] array = JSONB.parseObject(jsonbBytes, boolean[].class);
        assertEquals(true, array[0]);
        assertEquals(false, array[1]);
        assertEquals(false, array[2]);
        assertEquals(true, array[3]);
        assertEquals(false, array[4]);
    }

    @Test
    public void test_writeNull_0() {
        assertEquals("{}"
                , JSON.toJSONString(new VO()));
        assertEquals("{}",
                new String(
                        JSON.toJSONBytes(new VO())));
    }

    @Test
    public void test_writeNull() {
        assertEquals("{\"values\":null}"
                , JSON.toJSONString(new VO(), JSONWriter.Feature.WriteNulls));
        assertEquals("{\"values\":null}",
                new String(
                        JSON.toJSONBytes(new VO(), JSONWriter.Feature.WriteNulls)));
    }

    @Test
    public void test_writeNull2() {
        assertEquals("{\"values\":null}"
                , JSON.toJSONString(new VO2(), JSONWriter.Feature.WriteNulls));
        assertEquals("{\"values\":null}",
                new String(
                        JSON.toJSONBytes(new VO2(), JSONWriter.Feature.WriteNulls)));
    }

    public static class VO {
        public boolean[] values;
    }

    public static class VO2 {
        private boolean[] values;

        public boolean[] getValues() {
            return values;
        }

        public void setValues(boolean[] values) {
            this.values = values;
        }
    }
}
