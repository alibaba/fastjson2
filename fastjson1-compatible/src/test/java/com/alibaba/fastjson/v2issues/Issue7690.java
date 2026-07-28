package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue7690 {
    @Test
    public void testSetFieldAfterAutoCreatingArrayElement() {
        JSONObject root = new JSONObject();

        JSONObject item = new JSONObject();
        item.put("title", "1");
        item.put("value", "110");

        JSONArray testArray = new JSONArray();
        testArray.add(item);
        root.put("test", testArray);

        JSONPath.set(root, "$.test[1].title", 2);
        assertEquals(2, JSONPath.eval(root, "$.test[1].title"));

        JSONPath.set(root, "$.test[1].value", 220);
        assertEquals(2, JSONPath.eval(root, "$.test[1].title"));
        assertEquals(220, JSONPath.eval(root, "$.test[1].value"));
    }

    @Test
    public void testGetReturnsLiveWrapperForCoreJSONObject() {
        JSONObject root = new JSONObject();
        root.put("test", new com.alibaba.fastjson2.JSONObject());

        JSONObject test = (JSONObject) root.get("test");
        test.put("value", 220);

        assertEquals(220, JSONPath.eval(root, "$.test.value"));
    }

    @Test
    public void testLiveWrapperIterationExposesUnderlyingCoreNestedContainers() {
        JSONObject root = new JSONObject();
        JSONPath.set(root, "$.a.b.c", 1);

        JSONObject a = (JSONObject) root.get("a");
        Object value = a.values().iterator().next();
        Object entryValue = a.entrySet().iterator().next().getValue();
        AtomicReference<Object> forEachValue = new AtomicReference<>();
        a.forEach((key, item) -> forEachValue.set(item));

        assertEquals(com.alibaba.fastjson2.JSONObject.class, value.getClass());
        assertSame(value, entryValue);
        assertSame(value, forEachValue.get());
    }
}
