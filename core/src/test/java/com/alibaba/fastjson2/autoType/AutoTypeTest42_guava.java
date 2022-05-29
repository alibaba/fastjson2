package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeTest42_guava {
    @Test
    public void test_0() {
        ArrayListMultimap map = ArrayListMultimap.create();
        map.putAll("a", Arrays.asList(101, 102, 101));
        List value = map.get("a");

        byte[] bytes = JSONB.toBytes(value,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        List value2 = (List) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(value.getClass(), value2.getClass());
        assertEquals(value.size(), value2.size());
        assertEquals(value.get(0), value2.get(0));
        assertEquals(value.get(1), value2.get(1));
        assertEquals(value.get(2), value2.get(2));
    }

    @Test
    public void test_0_json() {
        ArrayListMultimap map = ArrayListMultimap.create();
        map.putAll("a", Arrays.asList(101, 102, 101));
        List value = map.get("a");

        String str = JSON.toJSONString(value);
        Assertions.assertEquals("[101,102,101]", str);
    }

    @Test
    public void test_1() {
        HashMultimap map = HashMultimap.create();
        map.putAll("a", Arrays.asList(101, 102));
        Set value = map.get("a");

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

        assertEquals(value.getClass(), value2.getClass());
        assertEquals(value.size(), value2.size());
    }
}
