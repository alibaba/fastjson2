package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("jsonpath")
public class MultiIndexTest {
    @Test
    public void set() {
        JSONPath jsonPath = JSONPath.of("$[1,3]");

        JSONArray array = JSONArray.of(100, 101, 102, 103, 104, 105);
        jsonPath.set(array, 999);

        assertEquals("[100,999,102,999,104,105]", array.toJSONString());
    }

    @Test
    public void set1() {
        JSONPath jsonPath = JSONPath.of("$[1,2,3]");

        JSONArray array = JSONArray.of(100, 101, 102, 103, 104, 105);
        jsonPath.set(array, 999);

        assertEquals("[100,999,999,999,104,105]", array.toJSONString());
    }

    @Test
    public void set_array() {
        JSONPath jsonPath = JSONPath.of("$[1,3]");

        int[] array = new int[] {100, 101, 102, 103, 104, 105};
        jsonPath.set(array, 999);

        assertEquals("[100,999,102,999,104,105]", JSON.toJSONString(array));
    }

    @Test
    public void set_array_1() {
        JSONPath jsonPath = JSONPath.of("$[1,2,3]");

        int[] array = new int[] {100, 101, 102, 103, 104, 105};
        jsonPath.set(array, 999);

        assertEquals("[100,999,999,999,104,105]", JSON.toJSONString(array));
    }

    @Test
    public void setCallback() {
        JSONPath jsonPath = JSONPath.of("$[1,3]");

        JSONArray array = JSONArray.of(100, 101, 102, 103, 104, 105);
        jsonPath.setCallback(array, e -> ((Integer) e) + 100);

        assertEquals("[100,201,102,203,104,105]", array.toJSONString());
    }

    @Test
    public void setCallback_1() {
        JSONPath jsonPath = JSONPath.of("$[1,2,3]");

        JSONArray array = JSONArray.of(100, 101, 102, 103, 104, 105);
        jsonPath.setCallback(array, e -> ((Integer) e) + 100);

        assertEquals("[100,201,202,203,104,105]", array.toJSONString());
    }

    @Test
    public void setCallback_array() {
        JSONPath jsonPath = JSONPath.of("$[1,3]");

        int[] array = new int[] {100, 101, 102, 103, 104, 105};
        jsonPath.setCallback(array, e -> ((Integer) e) + 100);

        assertEquals("[100,201,102,203,104,105]", JSON.toJSONString(array));
    }

    @Test
    public void setCallback_array_1() {
        JSONPath jsonPath = JSONPath.of("$[1,2,3]");

        int[] array = new int[] {100, 101, 102, 103, 104, 105};
        jsonPath.setCallback(array, e -> ((Integer) e) + 100);

        assertEquals("[100,201,202,203,104,105]", JSON.toJSONString(array));
    }

    @Test
    public void error_set() {
        JSONPath jsonPath = JSONPath.of("$[1,3]");

        assertThrows(
                JSONException.class,
                () -> jsonPath.set(JSONObject.of(), 1)
        );
    }

    @Test
    public void error_setCallback() {
        JSONPath jsonPath = JSONPath.of("$[1,3]");

        assertThrows(
                JSONException.class,
                () -> jsonPath.setCallback(JSONObject.of(), e -> ((Integer) e) + 1)
        );
    }

    @Test
    public void error_remove() {
        JSONPath jsonPath = JSONPath.of("$[1,3]");

        assertThrows(
                JSONException.class,
                () -> jsonPath.remove(JSONObject.of())
        );
    }

    @Test
    public void extract_order_and_negative() {
        String json = "[100,101,102,103,104,105]";
        JSONArray array = JSON.parseArray(json);
        byte[] jsonbBytes = JSONB.toBytes(array);

        String[] paths = {"$[3,1]", "$[4,2,0]", "$[5,0,2]", "$[0,-1]", "$[-1,-2]", "$[1,9]"};
        for (String path : paths) {
            JSONPath jsonPath = JSONPath.of(path);
            String expected = JSON.toJSONString(jsonPath.eval(array));
            assertEquals(expected, JSON.toJSONString(jsonPath.extract(json)), path);
            assertEquals(expected, JSON.toJSONString(jsonPath.extract(JSONReader.ofJSONB(jsonbBytes))), path);
        }

        assertEquals("[103,101]", JSON.toJSONString(JSONPath.extract(json, "$[3,1]")));
        assertEquals("[100,105]", JSON.toJSONString(JSONPath.extract(json, "$[0,-1]")));
        assertEquals("[103,101]", JSON.toJSONString(JSONPath.extract("{\"x\":" + json + "}", "$.x[3,1]")));
    }
}
