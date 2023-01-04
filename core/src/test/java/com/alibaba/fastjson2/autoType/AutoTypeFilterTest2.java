package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;

public class AutoTypeFilterTest2 {
    @Test
    public void test() {
        JSONReader.AutoTypeBeforeHandler filter = JSONReader.autoTypeFilter("Object");
        String str = "{\"@type\":\"LinkedHashMap\",\"id\":123}";
        Object object = JSON.parseObject(str, Object.class, filter);
        assertEquals("com.alibaba.fastjson2.JSONObject", object.getClass().getName());

        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str, Object.class, filter, JSONReader.Feature.ErrorOnNotSupportAutoType)
        );
    }

    @Test
    public void test1() {
        JSONReader.AutoTypeBeforeHandler filter = JSONReader.autoTypeFilter("Object");
        String str = "{\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeFilterTest2$Bean\",\"id\":123}";
        Object object = JSON.parseObject(str, Object.class, filter);
        assertEquals("com.alibaba.fastjson2.JSONObject", object.getClass().getName());

        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str, Object.class, filter, JSONReader.Feature.ErrorOnNotSupportAutoType)
        );
    }

    @Test
    public void test_jsonb_linkedHashMap() {
        LinkedHashMap map = new LinkedHashMap<>();
        map.put("id", 123);
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                map,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("LM"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                map,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("LinkedHashMap"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                map,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.util.LinkedHashMap"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_hashMap() {
        HashMap map = new HashMap<>();
        map.put("id", 123);
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                map,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("M"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                map,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("HashMap"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                map,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.util.HashMap"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_arrayList() {
        ArrayList values = new ArrayList<>();
        values.add(123L);
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("A"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("ArrayList"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.util.ArrayList"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_linkedList() {
        LinkedList values = new LinkedList<>();
        values.add(123L);
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("LA"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("LinkedList"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.util.LinkedList"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_hashSet() {
        HashSet values = new HashSet<>();
        values.add(123L);
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("HashSet"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.util.HashSet"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_treeSet() {
        TreeSet values = new TreeSet<>();
        values.add(123L);
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("TreeSet"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.util.TreeSet"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_linkedHashSet() {
        LinkedHashSet values = new LinkedHashSet<>();
        values.add(123L);
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("LinkedHashSet"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.util.LinkedHashSet"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_concurrentHashMap() {
        ConcurrentHashMap values = new ConcurrentHashMap<>();
        values.put("id", 123L);
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("ConcurrentHashMap"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.util.concurrent.ConcurrentHashMap"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_jsonObject() {
        JSONObject values = new JSONObject();
        values.put("id", 123L);
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("JSONObject"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("com.alibaba.fastjson2.JSONObject"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_jsonObject1x() {
        com.alibaba.fastjson.JSONObject values = new com.alibaba.fastjson.JSONObject();
        values.put("id", 123L);
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("JO1"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values,
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("com.alibaba.fastjson.JSONObject"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_ConcurrentLinkedQueue() {
        ConcurrentLinkedQueue values = new ConcurrentLinkedQueue<>();
        values.add(123);
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values.getClass(),
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("ConcurrentLinkedQueue"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                ).getClass()
        );

        assertEquals(
                values.getClass(),
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter(ConcurrentLinkedQueue.class),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                ).getClass()
        );

        assertEquals(
                values.getClass(),
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.util.concurrent.ConcurrentLinkedQueue"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                ).getClass()
        );
    }

    @Test
    public void test_jsonb_ConcurrentLinkedDeque() {
        ConcurrentLinkedDeque values = new ConcurrentLinkedDeque<>();
        values.add(123);
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertEquals(
                values.getClass(),
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("ConcurrentLinkedDeque"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                ).getClass()
        );

        assertEquals(
                values.getClass(),
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter(ConcurrentLinkedDeque.class),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                ).getClass()
        );

        assertEquals(
                values.getClass(),
                JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.util.concurrent.ConcurrentLinkedDeque"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                ).getClass()
        );
    }

    @Test
    public void test_jsonb_UUID() {
        UUID[] values = new UUID[]{UUID.randomUUID()};
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (UUID[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("UUID"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (UUID[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter(UUID.class),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (UUID[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.util.UUID"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_Date() {
        Date[] values = new Date[]{new Date()};
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (Date[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Date"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (Date[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter(Date.class),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (Date[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Date"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_Calendar() {
        Calendar[] values = new Calendar[]{Calendar.getInstance()};
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (Calendar[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Calendar"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (Calendar[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter(Calendar.class),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (Calendar[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.util.Calendar"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_SimpleDateFormat() {
        SimpleDateFormat[] values = new SimpleDateFormat[]{new SimpleDateFormat("yyyy-MM-dd")};
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (SimpleDateFormat[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("SimpleDateFormat"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (SimpleDateFormat[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter(SimpleDateFormat.class),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (SimpleDateFormat[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.text.SimpleDateFormat"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_LocalDate() {
        LocalDate[] values = new LocalDate[]{LocalDate.now()};
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (LocalDate[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter(LocalDate.class),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (LocalDate[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.time.LocalDate"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void test_jsonb_String() {
        String[] values = new String[]{"abc"};
        byte[] bytes = JSONB.toBytes(values, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("Object"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (String[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("String"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (String[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter(String.class),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );

        assertArrayEquals(
                values,
                (String[]) JSONB.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.autoTypeFilter("java.lang.String"),
                        JSONReader.Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    public static class Bean {
        public int id;
    }
}
