package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue640 {
    @Test
    public void test0() {
        String str = "{\"@type\":\"java.util.ImmutableCollections$Map1\",\"abc\":1}";
        Map object = (Map) JSON.parse(str, JSONReader.Feature.SupportAutoType);
        assertEquals(1, object.size());
        assertEquals(1, object.get("abc"));
    }

    @Test
    public void test0_jsonb() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeTypeName("java.util.ImmutableCollections$Map1");
        jsonWriter.writeAny(Collections.singletonMap("abc", 1));
        byte[] jsonbBytes = jsonWriter.getBytes();
        Map object = (Map) JSONB.parse(jsonbBytes, JSONReader.Feature.SupportAutoType);
        assertEquals(1, object.size());
        assertEquals(1, object.get("abc"));
    }

    @Test
    public void test1() {
        String str = "{\"@type\":\"java.util.ImmutableCollections$MapN\",\"abc\":1}";
        Map object = (Map) JSON.parse(str, JSONReader.Feature.SupportAutoType);
        assertEquals(1, object.size());
        assertEquals(1, object.get("abc"));
    }

    @Test
    public void test1_jsonb() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeTypeName("java.util.ImmutableCollections$MapN");
        jsonWriter.writeAny(Collections.singletonMap("abc", 1));
        byte[] jsonbBytes = jsonWriter.getBytes();
        Map object = (Map) JSONB.parse(jsonbBytes, JSONReader.Feature.SupportAutoType);
        assertEquals(1, object.size());
        assertEquals(1, object.get("abc"));
    }

    @Test
    public void testCollection_jsonb_0() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeTypeName("java.util.ImmutableCollections$List12");
        jsonWriter.writeAny(Collections.singletonList(1));
        byte[] jsonbBytes = jsonWriter.getBytes();
        List object = (List) JSONB.parse(jsonbBytes, JSONReader.Feature.SupportAutoType);
        assertEquals(1, object.size());
        assertEquals(1, object.get(0));
    }

    @Test
    public void testCollection_jsonb_1() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeTypeName("java.util.ImmutableCollections$ListN");
        jsonWriter.writeAny(Arrays.asList(1, 2, 3));
        byte[] jsonbBytes = jsonWriter.getBytes();
        List object = (List) JSONB.parse(jsonbBytes, JSONReader.Feature.SupportAutoType);
        assertEquals(3, object.size());
        assertEquals(1, object.get(0));
        assertEquals(2, object.get(1));
        assertEquals(3, object.get(2));
    }

    @Test
    public void testCollection_set_1() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeTypeName("java.util.ImmutableCollections$Set12");
        jsonWriter.writeAny(Collections.singleton(1));
        byte[] jsonbBytes = jsonWriter.getBytes();
        Set object = (Set) JSONB.parse(jsonbBytes, JSONReader.Feature.SupportAutoType);
        assertEquals(1, object.size());
        assertEquals(1, object.stream().findFirst().get());
    }

    @Test
    public void testCollection_set_2() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeTypeName("java.util.ImmutableCollections$SetN");
        jsonWriter.writeAny(JSONObject.of("a", 1, "b", 2, "c", 3).keySet());
        byte[] jsonbBytes = jsonWriter.getBytes();
        Set object = (Set) JSONB.parse(jsonbBytes, JSONReader.Feature.SupportAutoType);
        assertEquals(3, object.size());
        assertEquals("a", object.stream().findFirst().get());
    }
}
