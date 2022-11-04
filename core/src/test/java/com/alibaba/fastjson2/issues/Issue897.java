package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue897 {
    @Test
    public void test() {
        String json = "{\"name\": \"lisi\", \"age\": 12}";
        People p = JSON.parseObject(json, People.class);

        assertEquals("lisi", p.name); // 成功
        assertEquals(12, p.extra.get("age")); // 成功, 使用JSON.parseObject对未知属性赋值成功

        JSONPath.set(p, "$.age", 13);
        assertEquals(13, p.extra.get("age"));
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
    public void test1() {
        String json = "{\"name\": \"lisi\", \"age\": 12}";
        People p = JSON.parseObject(json, People.class);

        assertEquals("lisi", p.name); // 成功
        assertEquals(12, p.extra.get("age"));

        JSONPath.set(p, "$.age", 13);
        assertEquals(13, p.extra.get("age"));
    }
}
