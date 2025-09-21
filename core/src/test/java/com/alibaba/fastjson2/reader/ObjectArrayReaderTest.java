package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectArrayReaderTest {
    @Test
    public void test() {
        Object[] objects = JSON.parseObject("[1,\"abc\",{},[],null,.3,-1,true,false,2147483649]", Object[].class);

        assertEquals(10, objects.length);
        assertEquals(1, objects[0]);
        assertEquals("abc", objects[1]);
        assertEquals(new JSONObject(), objects[2]);
        assertEquals(new JSONArray(), objects[3]);
        assertNull(objects[4]);
        assertEquals(new BigDecimal("0.3"), objects[5]);
        assertEquals(-1, objects[6]);
        assertEquals(Boolean.TRUE, objects[7]);
        assertEquals(Boolean.FALSE, objects[8]);
        assertEquals(2147483649L, objects[9]);

        byte[] jsonbBytes = JSONB.toBytes(objects);
        Object[] values = JSONB.parseObject(jsonbBytes, Object[].class);
        assertArrayEquals(objects, values);
    }

    @Test
    public void testNull() {
        Bean bean = JSON.parseObject("{\"value\":null}", Bean.class);
        assertNull(bean.value);

        Bean bean1 = JSONObject.of("value", null).to(Bean.class);
        assertNull(bean1.value);

        Bean bean2 = JSONObject.of("value", "").to(Bean.class);
        assertNull(bean2.value);

        Bean bean3 = JSON.parseObject("{\"value\":\"\"}", Bean.class);
        assertNull(bean3.value);
    }

    @Test
    public void testBig() {
        int[] ints = new int[256];
        Arrays.fill(ints, 1);
        String json = JSON.toJSONString(ints);
        Object[] objects = JSON.parseObject(json, Object[].class);
        assertEquals(ints.length, objects.length);
        for (int i = 0; i < objects.length; i++) {
            Integer item = (Integer) objects[i];
            assertEquals(ints[i], item.intValue());
        }
    }

    @Test
    public void testError() {
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"value\":[a}", Bean.class));
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"value\":{}}", Bean.class));
    }

    public static class Bean {
        public Object[] value;
    }

    @Test
    public void testArrayWithHashSet() {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add("test1");
        hashSet.add("test2");
        hashSet.add("test3");

        Object[] array = new Object[]{hashSet};

        String json = JSON.toJSONString(array, JSONWriter.Feature.WriteClassName);

        Object[] result = JSON.parseObject(json, Object[].class, JSONReader.Feature.SupportClassForName);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertTrue(result[0] instanceof Set);

        @SuppressWarnings("unchecked")
        Set<String> resultSet = (Set<String>) result[0];
        assertEquals(3, resultSet.size());
        assertTrue(resultSet.contains("test1"));
        assertTrue(resultSet.contains("test2"));
        assertTrue(resultSet.contains("test3"));
    }

    @Test
    public void testArrayListWithHashSet() {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add("test1");
        hashSet.add("test2");

        ArrayList<Object> arrayList = new ArrayList<>();
        arrayList.add(hashSet);

        String json = JSON.toJSONString(arrayList, JSONWriter.Feature.WriteClassName);

        @SuppressWarnings("unchecked")
        ArrayList<Object> result = JSON.parseObject(json, ArrayList.class, JSONReader.Feature.SupportClassForName);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof Set);

        @SuppressWarnings("unchecked")
        Set<String> resultSet = (Set<String>) result.get(0);
        assertEquals(2, resultSet.size());
        assertTrue(resultSet.contains("test1"));
        assertTrue(resultSet.contains("test2"));
    }

    @Test
    public void testArrayWithList() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("item1");
        arrayList.add("item2");
        arrayList.add("item3");

        Object[] array = new Object[]{arrayList};

        String json = JSON.toJSONString(array, JSONWriter.Feature.WriteClassName);

        Object[] result = JSON.parseObject(json, Object[].class, JSONReader.Feature.SupportClassForName);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertTrue(result[0] instanceof java.util.List);

        @SuppressWarnings("unchecked")
        java.util.List<String> resultList = (java.util.List<String>) result[0];
        assertEquals(3, resultList.size());
        assertTrue(resultList.contains("item1"));
        assertTrue(resultList.contains("item2"));
        assertTrue(resultList.contains("item3"));
    }
}
