package com.alibaba.fastjson.issue_1900;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.beans.Transient;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1903 {
    @Test
    public void test_issue() {
        MapHandler mh = new MapHandler();
        mh.add("name", "test");
        mh.add("age", 20);

        Issues1903 issues = (Issues1903) Proxy.newProxyInstance(mh.getClass().getClassLoader(), new Class[]{Issues1903.class}, mh);
        assertEquals("test", issues.getName());
        assertEquals(20, issues.getAge().intValue());

        assertEquals("{\"age\":20}", JSON.toJSONString(issues));
    }

    interface Issues1903 {
        @Transient
        @JSONField(serialzeFeatures = {SerializerFeature.SkipTransientField})
        String getName();

        void setName(String name);

        Integer getAge();

        void setAge(Integer age);
    }

    class MapHandler
            implements InvocationHandler {
        Map<String, Object> map = new HashMap<String, Object>();

        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName().substring(3);
            String first = String.valueOf(name.charAt(0));
            name = name.replaceFirst(first, first.toLowerCase());
            return map.get(name);
        }

        public void add(String key, Object val) {
            map.put(key, val);
        }
    }
}
