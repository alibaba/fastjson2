package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@Tag("reader")
public class FieldTest {
    @Test
    public void test() throws Exception {
        Invoker invoker = new Invoker();
        Service service = new Service();
        invoker.target = service;
        invoker.f0 = Service.class.getField("field0");
        invoker.f1 = Service.class.getField("field1");

        String string = JSON.toJSONString(invoker);
        assertEquals(
                "{\"f0\":{\"declaringClass\":\"com.alibaba.fastjson2.read.FieldTest$Service\",\"name\":\"field0\"},\"f1\":{\"declaringClass\":\"com.alibaba.fastjson2.read.FieldTest$Service\",\"name\":\"field1\"},\"target\":{\"field1\":0}}",
                string
        );

        assertThrows(JSONException.class, () -> JSON.parseObject(string, Invoker.class));

        Invoker invoker1 = JSON.parseObject(string, Invoker.class, JSONReader.Feature.SupportClassForName);
        assertNotNull(invoker1);
        assertEquals(invoker.f0, invoker1.f0);
        assertEquals(invoker.f1, invoker1.f1);
    }

    @Test
    public void test_arrayMapping() throws Exception {
        Invoker invoker = new Invoker();
        Service service = new Service();
        invoker.target = service;
        invoker.f0 = Service.class.getField("field0");
        invoker.f1 = Service.class.getField("field1");

        String string = JSON.toJSONString(invoker, JSONWriter.Feature.BeanToArray);
        assertEquals(
                "[[\"com.alibaba.fastjson2.read.FieldTest$Service\",\"field0\"],[\"com.alibaba.fastjson2.read.FieldTest$Service\",\"field1\"],[null,0]]",
                string
        );

        assertThrows(JSONException.class, () -> JSON.parseObject(string, Invoker.class, JSONReader.Feature.SupportArrayToBean));

        Invoker invoker1 = JSON.parseObject(string, Invoker.class, JSONReader.Feature.SupportClassForName, JSONReader.Feature.SupportArrayToBean);
        assertNotNull(invoker1);
        assertEquals(invoker.f0, invoker1.f0);
        assertEquals(invoker.f1, invoker1.f1);
    }

    @Test
    public void test_jsonb() throws Exception {
        Invoker invoker = new Invoker();
        Service service = new Service();
        invoker.target = service;
        invoker.f0 = Service.class.getField("field0");
        invoker.f1 = Service.class.getField("field1");

        byte[] jsonbBytes = JSONB.toBytes(invoker);

        assertThrows(JSONException.class, () -> JSONB.parseObject(jsonbBytes, Invoker.class));

        Invoker invoker1 = JSONB.parseObject(jsonbBytes, Invoker.class, JSONReader.Feature.SupportClassForName);
        assertNotNull(invoker1);
        assertEquals(invoker.f0, invoker1.f0);
        assertEquals(invoker.f1, invoker1.f1);
    }

    @Test
    public void test_jsonb_arrayMapping() throws Exception {
        Invoker invoker = new Invoker();
        Service service = new Service();
        invoker.target = service;
        invoker.f0 = Service.class.getField("field0");
        invoker.f1 = Service.class.getField("field1");

        byte[] jsonbBytes = JSONB.toBytes(invoker, JSONWriter.Feature.BeanToArray);

        assertThrows(JSONException.class, () -> JSONB.parseObject(jsonbBytes, Invoker.class, JSONReader.Feature.SupportArrayToBean));

        Invoker invoker1 = JSONB.parseObject(jsonbBytes, Invoker.class, JSONReader.Feature.SupportClassForName, JSONReader.Feature.SupportArrayToBean);
        assertNotNull(invoker1);
        assertEquals(invoker.f0, invoker1.f0);
        assertEquals(invoker.f1, invoker1.f1);
    }

    public static class Invoker {
        public Object target;
        public Field f0;
        public Field f1;
    }

    public static class Service {
        public String field0;
        public int field1;
    }
}
