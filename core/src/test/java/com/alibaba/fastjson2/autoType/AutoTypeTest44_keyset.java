package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeTest44_keyset {
    @Test
    public void test_hashmap_keySet() {
        HashMap map = new HashMap();
        map.put("a", 101);
        Set value = map.keySet();

        byte[] bytes = JSONB.toBytes(value,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Set value2 = (Set) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals("a", value2.stream().findFirst().get());
    }

    @Test
    public void test_hashmap_values() {
        HashMap map = new HashMap();
        map.put("a", 101);
        Collection value = map.values();

        byte[] bytes = JSONB.toBytes(value,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Collection value2 = (Collection) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(1, value2.size());
        assertEquals(101, value2.stream().findFirst().get());
    }

    @Test
    public void test_treeMap_keySet() {
        TreeMap map = new TreeMap();
        map.put("a", 101);
        Set value = map.keySet();

        byte[] bytes = JSONB.toBytes(value,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Set value2 = (Set) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals("a", value2.stream().findFirst().get());
    }

    @Test
    public void test_treeMap_Values() {
        TreeMap map = new TreeMap();
        map.put("a", 101);
        Collection value = map.values();

        byte[] bytes = JSONB.toBytes(value,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Collection value2 = (Collection) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(1, value2.size());
        assertEquals(101, value2.stream().findFirst().get());
    }

    @Test
    public void test_linkedHashMap_keySet() {
        Map map = new LinkedHashMap();
        map.put("a", 101);
        Set value = map.keySet();

        byte[] bytes = JSONB.toBytes(value,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Set value2 = (Set) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals("a", value2.stream().findFirst().get());
    }

    @Test
    public void test_linkedHashMap_values() {
        Map map = new LinkedHashMap();
        map.put("a", 101);
        Collection value = map.values();

        byte[] bytes = JSONB.toBytes(value,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Collection value2 = (Collection) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(1, value2.size());
        assertEquals(101, value2.stream().findFirst().get());
    }

    @Test
    public void test_concurrentHashMap_keySet() {
        Map map = new ConcurrentHashMap();
        map.put("a", 101);
        Set value = map.keySet();

        byte[] bytes = JSONB.toBytes(value,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Set value2 = (Set) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals("a", value2.stream().findFirst().get());
    }

    @Test
    public void test_concurrentHashMap_values() {
        Map map = new ConcurrentHashMap();
        map.put("a", 101);
        Collection value = map.values();

        byte[] bytes = JSONB.toBytes(value,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Collection value2 = (Collection) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(1, value2.size());
        assertEquals(101, value2.stream().findFirst().get());
    }

    @Test
    public void test_ConcurrentSkipListMap_keySet() {
        Map map = new ConcurrentSkipListMap();
        map.put("a", 101);
        Set value = map.keySet();

        byte[] bytes = JSONB.toBytes(value,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Set value2 = (Set) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals("a", value2.stream().findFirst().get());
    }

    @Test
    public void test_ConcurrentSkipListMap_values() {
        Map map = new ConcurrentSkipListMap();
        map.put("a", 101);
        Collection value = map.values();

        byte[] bytes = JSONB.toBytes(value,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Collection value2 = (Collection) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(1, value2.size());
        assertEquals(101, value2.stream().findFirst().get());
    }
}
