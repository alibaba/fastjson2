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

        assertEquals("{\n" +
                        "\t\"a\":{}\n" +
                        "}", JSONB.toJSONString(
                        JSONB.toBytes(bean, JSONWriter.Feature.IgnoreNoneSerializable), JSONB.symbolTable("id")
                )
        );
    }

    public static class Bean
            implements Serializable {
        public A a;
        public B b;
    }

    public static class A
            implements Serializable {
    }

    public static class B {
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.a = new A();
        bean.b = new B();

        assertEquals("{\n" +
                "\t\"a\":{},\n" +
                "\t\"b\":{}\n" +
                "}", JSONB.toJSONString(JSONB.toBytes(bean, JSONWriter.Feature.FieldBased)));

        assertEquals("{\n" +
                "\t\"a\":{}\n" +
                "}", JSONB.toJSONString(JSONB.toBytes(bean, JSONWriter.Feature.IgnoreNoneSerializable, JSONWriter.Feature.FieldBased)));

        assertEquals("{\n" +
                        "\t\"a\":{}\n" +
                        "}", JSONB.toJSONString(
                        JSONB.toBytes(bean, JSONWriter.Feature.IgnoreNoneSerializable, JSONWriter.Feature.FieldBased), JSONB.symbolTable("id")
                )
        );
    }

    public static class Bean1
            implements Serializable {
        private A a;
        private B b;
    }

    @Test
    public void test2_serialize() {
        Bean2 bean = new Bean2();
        bean.a = new A();
        bean.b = new B();

        assertEquals("{\n" +
                "\t\"a\":{},\n" +
                "\t\"b\":{}\n" +
                "}", JSONB.toJSONString(JSONB.toBytes(bean, JSONWriter.Feature.FieldBased)));

        assertEquals(
                "null",
                JSONB.toJSONString(
                        JSONB.toBytes(
                                bean,
                                JSONWriter.Feature.IgnoreNoneSerializable,
                                JSONWriter.Feature.FieldBased
                        )
                )
        );
    }

    @Test
    public void test2_serialize_map() {
        JSONObject object = JSONObject.of("value", new Bean2());
        assertEquals("{\"value\":{}}", JSON.toJSONString(object));
        assertEquals("{\"value\":null}", JSON.toJSONString(object, JSONWriter.Feature.IgnoreNoneSerializable));

        assertEquals("{\n" +
                "\t\"value\":{}\n" +
                "}", JSONB.toJSONString(object.toJSONBBytes()));

        assertEquals("{\n" +
                "\t\"value\":null\n" +
                "}", JSONB.toJSONString(object.toJSONBBytes(JSONWriter.Feature.IgnoreNoneSerializable)));
    }

    private static class Bean2 {
        private A a;
        private B b;
    }
}
