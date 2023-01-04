package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class Issue597 {
    @Test
    public void test() throws Exception {
        Invoker invoker = new Invoker();
        Service service = new Service();
        invoker.target = service;
        invoker.method0 = Service.class.getMethod("f0");
        invoker.method1 = Service.class.getMethod("f1", String.class);

        String string = JSON.toJSONString(invoker);
        assertEquals(
                "{\"method0\":{\"declaringClass\":\"com.alibaba.fastjson2.issues.Issue597$Service\",\"name\":\"f0\",\"parameterTypes\":[]},\"method1\":{\"declaringClass\":\"com.alibaba.fastjson2.issues.Issue597$Service\",\"name\":\"f1\",\"parameterTypes\":[\"java.lang.String\"]},\"target\":{}}",
                string
        );

        assertThrows(JSONException.class, () -> JSON.parseObject(string, Invoker.class));

        Invoker invoker1 = JSON.parseObject(string, Invoker.class, JSONReader.Feature.SupportClassForName);
        assertNotNull(invoker1);
        assertEquals(invoker.method0, invoker1.method0);
        assertEquals(invoker.method1, invoker1.method1);
    }

    @Test
    public void test_arrayMapping() throws Exception {
        Invoker invoker = new Invoker();
        Service service = new Service();
        invoker.target = service;
        invoker.method0 = Service.class.getMethod("f0");
        invoker.method1 = Service.class.getMethod("f1", String.class);

        String string = JSON.toJSONString(invoker, JSONWriter.Feature.BeanToArray);
        assertEquals(
                "[[\"com.alibaba.fastjson2.issues.Issue597$Service\",\"f0\",[]],[\"com.alibaba.fastjson2.issues.Issue597$Service\",\"f1\",[\"java.lang.String\"]],[]]",
                string
        );

        assertThrows(JSONException.class, () -> JSON.parseObject(string, Invoker.class, JSONReader.Feature.SupportArrayToBean));

        Invoker invoker1 = JSON.parseObject(string, Invoker.class, JSONReader.Feature.SupportClassForName, JSONReader.Feature.SupportArrayToBean);
        assertNotNull(invoker1);
        assertEquals(invoker.method0, invoker1.method0);
        assertEquals(invoker.method1, invoker1.method1);
    }

    @Test
    public void test_jsonb() throws Exception {
        Invoker invoker = new Invoker();
        Service service = new Service();
        invoker.target = service;
        invoker.method0 = Service.class.getMethod("f0");
        invoker.method1 = Service.class.getMethod("f1", String.class);

        byte[] jsonbBytes = JSONB.toBytes(invoker);

        assertThrows(JSONException.class, () -> JSONB.parseObject(jsonbBytes, Invoker.class));

        Invoker invoker1 = JSONB.parseObject(jsonbBytes, Invoker.class, JSONReader.Feature.SupportClassForName);
        assertNotNull(invoker1);
        assertEquals(invoker.method0, invoker1.method0);
        assertEquals(invoker.method1, invoker1.method1);
    }

    @Test
    public void test_jsonb_arrayMapping() throws Exception {
        Invoker invoker = new Invoker();
        Service service = new Service();
        invoker.target = service;
        invoker.method0 = Service.class.getMethod("f0");
        invoker.method1 = Service.class.getMethod("f1", String.class);

        byte[] jsonbBytes = JSONB.toBytes(invoker, JSONWriter.Feature.BeanToArray);

        assertThrows(JSONException.class, () -> JSONB.parseObject(jsonbBytes, Invoker.class, JSONReader.Feature.SupportArrayToBean));

        Invoker invoker1 = JSONB.parseObject(jsonbBytes, Invoker.class, JSONReader.Feature.SupportClassForName, JSONReader.Feature.SupportArrayToBean);
        assertNotNull(invoker1);
        assertEquals(invoker.method0, invoker1.method0);
        assertEquals(invoker.method1, invoker1.method1);
    }

    public static class Invoker {
        public Object target;
        public Method method0;
        public Method method1;
    }

    public static class Service {
        public void f0() {
        }

        public void f1(String str) {
        }
    }
}
