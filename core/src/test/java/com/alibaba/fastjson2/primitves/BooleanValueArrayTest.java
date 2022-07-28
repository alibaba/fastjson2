package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

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
        assertEquals("{}",
                JSON.toJSONString(new VO()));
        assertEquals("{}",
                new String(
                        JSON.toJSONBytes(new VO())));
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
        assertEquals("{\"values\":null}",
                JSON.toJSONString(new VO2(), JSONWriter.Feature.WriteNulls));
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

    @Test
    public void test() {
        Bean bean = new Bean();
        bean.values = new boolean[] {true, false};
        byte[] bytes = JSONB.toBytes(bean);
        assertEquals("{\n" +
                "\t\"values\":[\n" +
                "\t\ttrue,\n" +
                "\t\tfalse\n" +
                "\t]\n" +
                "}", JSONB.toJSONString(bytes));
        Bean parsed = JSONB.parseObject(bytes, Bean.class);
        assertArrayEquals(bean.values, parsed.values);
    }

    @Test
    public void test_writeClassName() {
        Bean bean = new Bean();
        bean.values = new boolean[] {true, false};
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2.primitves.BooleanValueArrayTest$Bean\",\n" +
                "\t\"values\":[\n" +
                "\t\ttrue,\n" +
                "\t\tfalse\n" +
                "\t]\n" +
                "}", JSONB.toJSONString(bytes));
        Bean parsed = JSONB.parseObject(bytes, Bean.class);
        assertArrayEquals(bean.values, parsed.values);
    }

    public static class Bean {
        public boolean[] values;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.values = new boolean[] {true, false};
        byte[] bytes = JSONB.toBytes(bean);
        assertEquals("{\n" +
                "\t\"values\":[\n" +
                "\t\ttrue,\n" +
                "\t\tfalse\n" +
                "\t]\n" +
                "}", JSONB.toJSONString(bytes));
        Bean1 parsed = JSONB.parseObject(bytes, Bean1.class);
        assertArrayEquals(bean.values, parsed.values);
    }

    @Test
    public void test1_writeClassName() {
        Bean1 bean = new Bean1();
        bean.values = new boolean[] {true, false};
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2.primitves.BooleanValueArrayTest$Bean1\",\n" +
                "\t\"values\":[\n" +
                "\t\ttrue,\n" +
                "\t\tfalse\n" +
                "\t]\n" +
                "}", JSONB.toJSONString(bytes));
        Bean1 parsed = JSONB.parseObject(bytes, Bean1.class);
        assertArrayEquals(bean.values, parsed.values);
    }

    private static class Bean1 {
        public boolean[] values;
    }
}
