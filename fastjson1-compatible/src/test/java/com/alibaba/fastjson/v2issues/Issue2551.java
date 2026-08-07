package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2551 {
    @Test
    public void test() {
        String json = "{\"name\": \"zhangsan\", \"age\": 18}";
        People p = JSON.parseObject(json, People.class);

        assertEquals("zhangsan", p.getName());
        assertEquals(18, p.getExtra().get("age"));

        JSONPath.set(p, "$.age", 20);
        assertEquals(20, p.getExtra().get("age"));
    }

    static class People {
        private String name;
        private Map<String, Object> extra = new HashMap<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, Object> getExtra() {
            return extra;
        }

        public void setExtra(Map<String, Object> extra) {
            this.extra = extra;
        }

        @JSONField(unwrapped = true)
        public void set(String key, Object value) {
            this.extra.put(key, value);
        }

        @JSONField(unwrapped = true)
        public Object get(String key) {
            return this.extra.get(key);
        }
    }

    @Test
    public void testfj() {
        String json = "{\"name\": \"zhangsan\", \"age\": 18}";
        People1 p = com.alibaba.fastjson.JSON.parseObject(json, People1.class);

        assertEquals("zhangsan", p.getName());
        assertEquals(18, p.getExtra().get("age"));

        com.alibaba.fastjson.JSONPath.set(p, "$.age", 20);
        assertEquals(20, p.getExtra().get("age"));
    }

    static class People1 {
        private String name;
        private Map<String, Object> extra = new HashMap<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, Object> getExtra() {
            return extra;
        }

        public void setExtra(Map<String, Object> extra) {
            this.extra = extra;
        }

        @com.alibaba.fastjson.annotation.JSONField(unwrapped = true)
        public void set(String key, Object value) {
            this.extra.put(key, value);
        }

        @com.alibaba.fastjson.annotation.JSONField(unwrapped = true)
        public Object get(String key) {
            return this.extra.get(key);
        }
    }
}
