package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class JSONPath_0 {

    @Test
    public void test_root() throws Exception {
        Object obj = new Object();
        assertSame(obj, JSONPath.of("$").eval(obj));
    }

    @Test
    public void test_null() throws Exception {
        assertNull(JSONPath.of("$").extract(null));
    }

    @Test
    public void test_map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("val", new Object());
        assertSame(map.get("val"), JSONPath.of("$.val").eval(map));
    }

    @Test
    public void test_entity() throws Exception {
        Entity entity = new Entity();
        entity.setValue(new Object());
        assertSame(entity.getValue(), JSONPath.of("$.value").eval(entity));
    }

    public static class Entity {

        private Object value;

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

    }
}
