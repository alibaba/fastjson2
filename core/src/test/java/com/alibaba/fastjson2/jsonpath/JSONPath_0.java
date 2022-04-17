package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class JSONPath_0 extends TestCase {

    public void test_root() throws Exception {
        Object obj = new Object();
        Assert.assertSame(obj, JSONPath.of("$").eval(obj));
    }

    public void test_null() throws Exception {
        Assert.assertNull(JSONPath.of("$").extract(null));
    }

    public void test_map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("val", new Object());
        Assert.assertSame(map.get("val"), JSONPath.of("$.val").eval(map));
    }
    
    public void test_entity() throws Exception {
        Entity entity = new Entity();
        entity.setValue(new Object());
        Assert.assertSame(entity.getValue(), JSONPath.of("$.value").eval(entity));
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
