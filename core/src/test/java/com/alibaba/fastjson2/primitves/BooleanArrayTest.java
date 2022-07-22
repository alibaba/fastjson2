package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        byte[] jsonbBytes = JSONB.toBytes(Arrays.asList(new Object[]{1, 0, null, true, false}));
        Boolean[] array = JSONB.parseObject(jsonbBytes, Boolean[].class);
        assertEquals(Boolean.TRUE, array[0]);
        assertEquals(Boolean.FALSE, array[1]);
        assertNull(array[2]);
        assertEquals(Boolean.TRUE, array[3]);
        assertEquals(Boolean.FALSE, array[4]);
    }

    @Test
    public void test() {
        Bean bean = new Bean();
        bean.values = new Boolean[] {true, false};
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
        bean.values = new Boolean[] {true, false};
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2.primitves.BooleanArrayTest$Bean\",\n" +
                "\t\"values\":[\n" +
                "\t\ttrue,\n" +
                "\t\tfalse\n" +
                "\t]\n" +
                "}", JSONB.toJSONString(bytes));
        Bean parsed = JSONB.parseObject(bytes, Bean.class);
        assertArrayEquals(bean.values, parsed.values);
    }

    public static class Bean {
        public Boolean[] values;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.values = new Boolean[] {true, false};
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
        bean.values = new Boolean[] {true, false};
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2.primitves.BooleanArrayTest$Bean1\",\n" +
                "\t\"values\":[\n" +
                "\t\ttrue,\n" +
                "\t\tfalse\n" +
                "\t]\n" +
                "}", JSONB.toJSONString(bytes));
        Bean1 parsed = JSONB.parseObject(bytes, Bean1.class);
        assertArrayEquals(bean.values, parsed.values);
    }

    private static class Bean1 {
        public Boolean[] values;
    }
}
