package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListTest {
    @Test
    public void testNull() {
        Bean bean = new Bean();
        String expected = "{\"list\":null}";
        assertEquals(expected, new String(JSON.toJSONBytes(bean, JSONWriter.Feature.WriteNulls)));
        assertEquals(expected, JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls));
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteNulls);
        assertEquals("{}", JSONB.toJSONString(jsonbBytes, true));
    }

    @Test
    public void testListInteger() {
        Bean bean = new Bean();
        bean.list = new ArrayList<>();
        bean.list.add(1);
        bean.list.add(2);
        bean.list.add(3);

        String expected = "{\"list\":[1,2,3]}";
        assertEquals(expected, new String(JSON.toJSONBytes(bean, JSONWriter.Feature.WriteNulls)));
        assertEquals(expected, JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls));
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteNulls);
        assertEquals("{\n" +
                "\t\"list\":[\n" +
                "\t\t1,\n" +
                "\t\t2,\n" +
                "\t\t3\n" +
                "\t]\n" +
                "}", JSONB.toJSONString(jsonbBytes, true));
    }

    public static class Bean {
        public List<Integer> list;
    }

    @Test
    public void testNull1() {
        Bean1 bean = new Bean1();
        String expected = "{\"list\":null}";
        assertEquals(expected, new String(JSON.toJSONBytes(bean, JSONWriter.Feature.WriteNulls)));
        assertEquals(expected, JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls));
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteNulls);
        assertEquals("{}", JSONB.toJSONString(jsonbBytes, true));
    }

    @Test
    public void testListLong() {
        Bean1 bean = new Bean1();
        bean.list = new ArrayList<>();
        bean.list.add(1L);
        bean.list.add(2L);
        bean.list.add(3L);

        String expected = "{\"list\":[1,2,3]}";
        assertEquals(expected, new String(JSON.toJSONBytes(bean, JSONWriter.Feature.WriteNulls)));
        assertEquals(expected, JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls));
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteNulls);
        assertEquals("{\n" +
                "\t\"list\":[\n" +
                "\t\t1,\n" +
                "\t\t2,\n" +
                "\t\t3\n" +
                "\t]\n" +
                "}", JSONB.toJSONString(jsonbBytes, true));
    }

    public static class Bean1 {
        public List<Long> list;
    }
}
