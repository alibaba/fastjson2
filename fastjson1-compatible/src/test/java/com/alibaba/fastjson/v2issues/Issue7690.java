package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(220, JSONPath.eval(root, "$.test[1].value"));
    }

    @Test
    public void testSetFieldAfterAutoCreatingObject() {
        JSONObject root = new JSONObject();

        JSONPath.set(root, "$.test.title", 2);
        assertEquals(2, JSONPath.eval(root, "$.test.title"));

        JSONPath.set(root, "$.test.value", 220);
        assertEquals(220, JSONPath.eval(root, "$.test.value"));
    }
}
