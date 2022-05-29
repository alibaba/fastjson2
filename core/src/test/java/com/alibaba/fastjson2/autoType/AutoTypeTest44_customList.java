package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeTest44_customList {
    @Test
    public void test_arrayList() {
        ArrayList list = new ArrayList() {
        };
        list.add(101);

        byte[] bytes = JSONB.toBytes(list,
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

        assertEquals(101, value2.stream().findFirst().get());
    }

    @Test
    public void test_jsonArray() {
        List list = new JSONArray() {
        };
        list.add(101);

        byte[] bytes = JSONB.toBytes(list,
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

        assertEquals(101, value2.stream().findFirst().get());
    }

    @Test
    public void test_hashMap() {
        HashMap map = new HashMap() {
        };
        map.put("a", 101);

        byte[] bytes = JSONB.toBytes(map,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Map map2 = (Map) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(101, map2.get("a"));
    }
}
