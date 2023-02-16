package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1086 {
    @Test
    public void test() {
        String text = "{\"id\":123,\"latitude\":37,\"longitude\":127}";
        assertEquals("{\"id\":123,\"latitude\":37,\"longitude\":127}", text);
        VO vo2 = JSON.parseObject(text, VO.class);
        assertNotNull(vo2.properties);
        assertEquals(37, vo2.properties.get("latitude"));
        assertEquals(127, vo2.properties.get("longitude"));
    }

    public static class VO {
        public int id;
        @JSONField(unwrapped = true, serialize = false, deserialize = false)
        public Map<String, Object> properties = new LinkedHashMap<String, Object>();
    }

    @Test
    public void test1() {
        String text = "{\"id\":123,\"latitude\":37,\"longitude\":127}";
        assertEquals("{\"id\":123,\"latitude\":37,\"longitude\":127}", text);
        VO1 vo2 = JSON.parseObject(text, VO1.class);
        assertNotNull(vo2.properties);
        assertEquals(37, vo2.properties.get("latitude"));
        assertEquals(127, vo2.properties.get("longitude"));
    }

    public static class VO1 {
        public int id;

        private Map<String, Object> properties = new LinkedHashMap<String, Object>();

        @JSONField(name = "_any", unwrapped = true, serialize = false)
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }
    }
}
