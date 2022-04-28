package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

public class IgnoreNoneSerializableTest {

    @Test
    public void test_feature() {
        Bean bean = new Bean();
        bean.a = new A();
        bean.b = new B();

        assertEquals("{\"a\":{},\"b\":{}}", JSON.toJSONString(bean));
        assertEquals("{\"a\":{}}", JSON.toJSONString(bean, JSONWriter.Feature.IgnoreNoneSerializable));
    }

    @Test
    public void test_feature_parse() {
        String str = "{\"a\":{},\"b\":{}}";

        Bean bean = JSON.parseObject(str, Bean.class);
        assertNotNull(bean.a);
        assertNotNull(bean.b);

        Bean bean2 = JSON.parseObject(str, Bean.class, JSONReader.Feature.IgnoreNoneSerializable);
        assertNotNull(bean2.a);
        assertNull(bean2.b);
    }

    @Test
    public void test_feature_jsonb() {
        Bean bean = new Bean();
        bean.a = new A();
        bean.b = new B();

        assertEquals("{\n" +
                "\t\"a\":{},\n" +
                "\t\"b\":{}\n" +
                "}", JSONB.toJSONString(JSONB.toBytes(bean)));

        assertEquals("{\n" +
                "\t\"a\":{}\n" +
                "}", JSONB.toJSONString(JSONB.toBytes(bean, JSONWriter.Feature.IgnoreNoneSerializable)));
    }

    public static class Bean {
        public A a;
        public B b;
    }

    public static class A implements Serializable  {

    }

    public static class B {

    }
}
