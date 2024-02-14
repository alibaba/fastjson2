package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MultiNameTest {
    @Test
    public void accept() {
        JSONPath path = JSONPath.of("$['k1', 'k2']");

        JSONObject object = JSONObject.of("k1", 101, "k2", 102, "k3", 103);
        String str = object.toJSONString();

        Object extracted = path.extract(str);
        assertEquals("[101,102]", JSON.toJSONString(extracted));
    }

    @Test
    public void set() {
        JSONPath path = JSONPath.of("$['k1', 'k2']");

        JSONObject object = JSONObject.of();
        path.set(object, "abc");

        assertEquals("{\"k1\":\"abc\",\"k2\":\"abc\"}", JSON.toJSONString(object));
    }

    @Test
    public void setCallback() {
        JSONPath path = JSONPath.of("$['k1', 'k2']");

        JSONObject object = JSONObject.of();
        path.setCallback(object, (o, v) -> "abc");

        assertEquals("{\"k1\":\"abc\",\"k2\":\"abc\"}", JSON.toJSONString(object));
    }

    @Test
    public void setCallback_error() {
        JSONPath path = JSONPath.of("$['k1', 'k2']");

        assertThrows(JSONException.class, () -> path.setCallback(JSONArray.of(), e -> e));
    }

    @Test
    public void setBean() {
        JSONPath path = JSONPath.of("$['k1', 'k2']");

        Bean object = new Bean();
        path.set(object, "abc");

        assertEquals("{\"k1\":\"abc\",\"k2\":\"abc\"}", JSON.toJSONString(object));
    }

    @Test
    public void set_error() {
        JSONPath path = JSONPath.of("$['k1', 'k2']");

        assertThrows(JSONException.class, () -> path.set(JSONArray.of(), "abc"));
    }

    @Test
    public void setCallbackBean() {
        JSONPath path = JSONPath.of("$['k1', 'k2']");

        Bean object = new Bean();
        path.setCallback(object, (o, v) -> "abc");

        assertEquals("{\"k1\":\"abc\",\"k2\":\"abc\"}", JSON.toJSONString(object));
    }

    @Test
    public void remove() {
        JSONPath path = JSONPath.of("$['k1', 'k2']");

        JSONObject object = JSONObject.of("k1", 101, "k2", 102, "k3", 103);
        assertTrue(path.remove(object));

        assertEquals("{\"k3\":103}", JSON.toJSONString(object));
    }

    @Test
    public void removeBean() {
        JSONPath path = JSONPath.of("$['k1', 'k2']");

        Bean object = new Bean();
        object.k1 = "a1";
        object.k2 = "a2";
        object.k3 = "a3";
        assertTrue(path.remove(object));

        assertEquals("{\"k3\":\"a3\"}", JSON.toJSONString(object));
    }

    @Test
    public void remove_error() {
        JSONPath path = JSONPath.of("$['k1', 'k2']");

        assertThrows(JSONException.class, () -> path.remove(JSONArray.of()));
    }

    public static class Bean {
        public String k1;
        public String k2;
        public String k3;
    }
}
