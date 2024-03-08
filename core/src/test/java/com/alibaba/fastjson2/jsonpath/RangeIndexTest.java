package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RangeIndexTest {
    @Test
    public void set() {
        JSONPath jsonPath = JSONPath.of("$[1:3]");

        JSONArray array = JSONArray.of(100, 101, 102, 103, 104, 105);
        jsonPath.set(array, 999);

        assertEquals("[100,999,999,103,104,105]", array.toJSONString());
    }

    @Test
    public void set1() {
        JSONPath jsonPath = JSONPath.of("$[-3:0]");

        JSONArray array = JSONArray.of(100, 101, 102, 103, 104, 105);
        jsonPath.set(array, 999);

        assertEquals("[100,101,102,999,999,999]", array.toJSONString());
    }

    @Test
    public void set_array() {
        JSONPath jsonPath = JSONPath.of("$[1:3]");

        int[] array = new int[] {100, 101, 102, 103, 104, 105};
        jsonPath.set(array, 999);

        assertEquals("[100,999,999,103,104,105]", JSON.toJSONString(array));
    }

    @Test
    public void set_array_1() {
        JSONPath jsonPath = JSONPath.of("$[-3:-1]");

        int[] array = new int[] {100, 101, 102, 103, 104, 105};
        jsonPath.set(array, 999);

        assertEquals("[100,101,102,999,999,105]", JSON.toJSONString(array));
    }

    @Test
    public void setCallback() {
        JSONPath jsonPath = JSONPath.of("$[1:3]");

        JSONArray array = JSONArray.of(100, 101, 102, 103, 104, 105);
        jsonPath.setCallback(array, e -> ((Integer) e) + 1);

        assertEquals("[100,102,103,103,104,105]", array.toJSONString());
    }

    @Test
    public void setCallback_1() {
        JSONPath jsonPath = JSONPath.of("$[-3:0]");

        JSONArray array = JSONArray.of(100, 101, 102, 103, 104, 105);
        jsonPath.setCallback(array, e -> ((Integer) e) + 1);

        assertEquals("[100,101,102,104,105,106]", array.toJSONString());
    }

    @Test
    public void setCallback_array() {
        JSONPath jsonPath = JSONPath.of("$[1:3]");

        int[] array = new int[] {100, 101, 102, 103, 104, 105};
        jsonPath.setCallback(array, e -> ((Integer) e) + 1);

        assertEquals("[100,102,103,103,104,105]", JSON.toJSONString(array));
    }

    @Test
    public void setCallback_array_1() {
        JSONPath jsonPath = JSONPath.of("$[-3:0]");

        int[] array = new int[] {100, 101, 102, 103, 104, 105};
        jsonPath.setCallback(array, e -> ((Integer) e) + 1);

        assertEquals("[100,101,102,104,105,106]", JSON.toJSONString(array));
    }

    @Test
    public void error_set() {
        JSONPath jsonPath = JSONPath.of("$[1:3]");

        assertThrows(
                JSONException.class,
                () -> jsonPath.set(JSONObject.of(), 1)
        );
    }

    @Test
    public void error_setCallback() {
        JSONPath jsonPath = JSONPath.of("$[1:3]");

        assertThrows(
                JSONException.class,
                () -> jsonPath.setCallback(JSONObject.of(), e -> ((Integer) e) + 1)
        );
    }

    @Test
    public void error_remove() {
        JSONPath jsonPath = JSONPath.of("$[1:3]");

        assertThrows(
                JSONException.class,
                () -> jsonPath.remove(JSONObject.of())
        );
    }
}
