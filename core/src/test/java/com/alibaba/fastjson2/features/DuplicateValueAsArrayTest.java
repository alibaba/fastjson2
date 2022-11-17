package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DuplicateValueAsArrayTest {
    @Test
    public void test() {
        String str = "{\"item\":1,\"item\":2}";
        {
            JSONObject object = JSON.parseObject(str);
            assertEquals("{\"item\":2}", object.toString());
        }
        JSONObject object = JSON.parseObject(str, JSONReader.Feature.DuplicateKeyValueAsArray);
        assertEquals(1, object.size());
        assertEquals(JSONArray.class, object.get("item").getClass());
        assertEquals(2, object.getJSONArray("item").size());
        assertEquals("[1,2]", object.getJSONArray("item").toString());
    }

    @Test
    public void test1() {
        String str = "{\"item\":1,\"item\":2}";
        JSONObject object = (JSONObject) JSON.parse(str, JSONReader.Feature.DuplicateKeyValueAsArray);
        assertEquals(1, object.size());
        assertEquals(JSONArray.class, object.get("item").getClass());
        assertEquals(2, object.getJSONArray("item").size());
        assertEquals("[1,2]", object.getJSONArray("item").toString());
    }

    public static class Bean {
        public Map<String, String> values = new HashMap<>();
    }

    @Test
    public void testBean() {
        String str = "{\"values\":{\"item\":1,\"item\":2}}";
        Bean bean = JSON.parseObject(str, Bean.class, JSONReader.Feature.DuplicateKeyValueAsArray);
        Map object = bean.values;
        assertEquals(1, object.size());
        assertEquals(JSONArray.class, object.get("item").getClass());
        assertEquals(2, ((Collection) object.get("item")).size());
        assertEquals("[\"1\",\"2\"]", object.get("item").toString());
    }

    public static class Bean1 {
        public Map<String, BigDecimal> values = new HashMap<>();
    }

    @Test
    public void testBean1() {
        String str = "{\"values\":{\"item\":1,\"item\":2}}";
        Bean1 bean = JSON.parseObject(str, Bean1.class, JSONReader.Feature.DuplicateKeyValueAsArray);
        Map object = bean.values;
        assertEquals(1, object.size());
        assertEquals(JSONArray.class, object.get("item").getClass());
        assertEquals(2, ((Collection) object.get("item")).size());
        assertEquals("[1,2]", object.get("item").toString());
    }

    @Test
    public void testJSONPath0() {
        JSONObject object = JSONObject.of("item", 1);
        JSONPath path = JSONPath.of("$.item");
        path.set(object, 2, JSONReader.Feature.DuplicateKeyValueAsArray);

        assertEquals(1, object.size());
        assertEquals(JSONArray.class, object.get("item").getClass());
        assertEquals(2, ((Collection) object.get("item")).size());
        assertEquals("[1,2]", object.get("item").toString());
    }

    @Test
    public void testJSONPath1() {
        JSONObject object = JSONObject.of("item", 1);
        JSONObject root = JSONObject.of("value", object);
        JSONPath path = JSONPath.of("$.value.item");
        path.set(root, 2, JSONReader.Feature.DuplicateKeyValueAsArray);

        assertEquals(1, object.size());
        assertEquals(JSONArray.class, object.get("item").getClass());
        assertEquals(2, ((Collection) object.get("item")).size());
        assertEquals("[1,2]", object.get("item").toString());
    }

    @Test
    public void testJSONPath2() {
        JSONObject object = JSONObject.of("item", 1);
        JSONObject root = JSONObject.of("values", JSONArray.of(object));
        JSONPath path = JSONPath.of("$.values[0].item");
        path.set(root, 2, JSONReader.Feature.DuplicateKeyValueAsArray);

        assertEquals(1, object.size());
        assertEquals(JSONArray.class, object.get("item").getClass());
        assertEquals(2, ((Collection) object.get("item")).size());
        assertEquals("[1,2]", object.get("item").toString());
    }
}
