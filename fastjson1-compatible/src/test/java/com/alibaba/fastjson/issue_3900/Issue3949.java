package com.alibaba.fastjson.issue_3900;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Issue3949 {
    @Test
    public void testSerializeWriterWithoutWriteMapNullValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", null);
        map.put("b", "2");

        SerializeWriter out = new SerializeWriter(
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.QuoteFieldNames,
                SerializerFeature.SkipTransientField,
                SerializerFeature.WriteEnumUsingToString,
                SerializerFeature.SortField
        );

        JSONSerializer serializer = new JSONSerializer(out);
        serializer.write(map);
        String result = out.toString();

        assertEquals("{\"b\":\"2\"}", result);
        assertFalse(result.contains("\"a\""));
    }

    @Test
    public void testSerializeWriterWithWriteMapNullValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", null);
        map.put("b", "2");

        SerializeWriter out = new SerializeWriter(
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.QuoteFieldNames,
                SerializerFeature.WriteMapNullValue
        );

        JSONSerializer serializer = new JSONSerializer(out);
        serializer.write(map);
        String result = out.toString();

        assertTrue(result.contains("\"a\":null"));
        assertTrue(result.contains("\"b\":\"2\""));
    }

    @Test
    public void testSerializeWriterWithConfigConstructor() {
        Map<String, Object> map = new HashMap<>();
        map.put("x", null);
        map.put("y", "test");

        SerializeWriter out = new SerializeWriter(
                SerializeConfig.global,
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.QuoteFieldNames
        );

        JSONSerializer serializer = new JSONSerializer(out);
        serializer.write(map);
        String result = out.toString();

        assertEquals("{\"y\":\"test\"}", result);
        assertFalse(result.contains("\"x\""));
    }

    @Test
    public void testSortFieldFeature() {
        Map<String, Object> map = new HashMap<>();
        map.put("z", "3");
        map.put("a", "1");
        map.put("m", "2");

        SerializeWriter out = new SerializeWriter(
                SerializerFeature.SortField,
                SerializerFeature.QuoteFieldNames
        );

        JSONSerializer serializer = new JSONSerializer(out);
        serializer.write(map);
        String result = out.toString();

        assertTrue(result.contains("\"a\":\"1\""));
        assertTrue(result.contains("\"m\":\"2\""));
        assertTrue(result.contains("\"z\":\"3\""));
    }

    @Test
    public void testOriginalIssueExample() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", null);
        map.put("b", "2");

        SerializeWriter out = new SerializeWriter(
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.QuoteFieldNames,
                SerializerFeature.SkipTransientField,
                SerializerFeature.WriteEnumUsingToString,
                SerializerFeature.SortField
        );

        JSONSerializer serializer = new JSONSerializer(out);
        serializer.write(map);
        String result = out.toString();

        assertEquals("{\"b\":\"2\"}", result);
    }

    @Test
    public void testMultipleNullValues() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", null);
        map.put("b", "value");
        map.put("c", null);
        map.put("d", "another");

        SerializeWriter out = new SerializeWriter(
                SerializerFeature.QuoteFieldNames,
                SerializerFeature.SortField
        );

        JSONSerializer serializer = new JSONSerializer(out);
        serializer.write(map);
        String result = out.toString();

        assertEquals("{\"b\":\"value\",\"d\":\"another\"}", result);
        assertFalse(result.contains("\"a\""));
        assertFalse(result.contains("\"c\""));
    }

    @Test
    public void testDisableCircularReferenceDetect() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test");
        map.put("value", 123);

        SerializeWriter out = new SerializeWriter(
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.QuoteFieldNames
        );

        JSONSerializer serializer = new JSONSerializer(out);
        serializer.write(map);
        String result = out.toString();

        assertTrue(result.contains("\"name\":\"test\""));
        assertTrue(result.contains("\"value\":123"));
    }

    @Test
    public void testEmptyMap() {
        Map<String, Object> map = new HashMap<>();

        SerializeWriter out = new SerializeWriter(
                SerializerFeature.QuoteFieldNames
        );

        JSONSerializer serializer = new JSONSerializer(out);
        serializer.write(map);
        String result = out.toString();

        assertEquals("{}", result);
    }

    @Test
    public void testMapWithOnlyNullValues() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", null);
        map.put("b", null);
        map.put("c", null);

        SerializeWriter out = new SerializeWriter(
                SerializerFeature.QuoteFieldNames
        );

        JSONSerializer serializer = new JSONSerializer(out);
        serializer.write(map);
        String result = out.toString();

        assertEquals("{}", result);
    }
}
