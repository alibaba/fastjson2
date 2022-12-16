package com.alibaba.fastjson2.fieldbased;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldBasedTest3 {
    @Test
    public void test_0() {
        A a = new A(101);

        String str = JSON.toJSONString(a, JSONWriter.Feature.FieldBased);
        assertEquals("{\"id\":101}", str);
        A a1 = JSON.parseObject(str, A.class, JSONReader.Feature.FieldBased);
        assertEquals(a.id, a1.id);
    }

    public static class A {
        private int id;

        private A() {
        }

        private A(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    @Test
    public void test_1() {
        Bean1 bean = new Bean1();
        bean.id = 101;
        String str = JSON.toJSONString(bean, JSONWriter.Feature.FieldBased);
        assertEquals("{\"id\":101}", str);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class, JSONReader.Feature.FieldBased);
        assertEquals(bean.id, bean1.id);
    }

    @Test
    public void test_1_jsonb() {
        Bean1 bean = new Bean1();
        bean.id = 101;
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.FieldBased);
        Bean1 bean1 = JSONB.parseObject(jsonbBytes, Bean1.class, JSONReader.Feature.FieldBased);
        assertEquals(bean.id, bean1.id);
    }

    private static class Bean1 {
        private int id;
    }

    @Test
    public void test_2_jsonb() {
        Bean2 bean = new Bean2();
        bean.id = 101;
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.FieldBased);
        Bean1 bean1 = JSONB.parseObject(jsonbBytes, Bean1.class, JSONReader.Feature.FieldBased);
        assertEquals(bean.id, bean1.id);
    }

    @Test
    public void test_2_jsonb_1() {
        Bean2 bean = new Bean2();
        bean.id = 101;
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.FieldBased);
        Bean1 bean1 = JSONB.parseObject(jsonbBytes, (Type) Bean1.class, JSONReader.Feature.FieldBased);
        assertEquals(bean.id, bean1.id);
    }

    @Test
    public void test_2_jsonb_1_symbol() {
        Bean2 bean = new Bean2();
        bean.id = 101;
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.FieldBased);
        Bean1 bean1 = JSONB.parseObject(jsonbBytes, Bean1.class, JSONB.symbolTable(), JSONReader.Feature.FieldBased);
        assertEquals(bean.id, bean1.id);
    }

    @Test
    public void test_2_toJavaObject() {
        assertEquals(101,
                JSONObject
                        .of("id", 101)
                        .toJavaObject(Bean2.class, JSONReader.Feature.FieldBased)
                        .id
        );
    }

    @Test
    public void test_2_getObject() {
        assertEquals(101,
                JSONObject.of("root", JSONObject
                                .of("id", 101)
                        )
                        .getObject("root", Bean2.class, JSONReader.Feature.FieldBased)
                        .id
        );
    }

    @Test
    public void test_2_getObject_type() {
        assertEquals(101,
                ((Bean2) JSONObject.of("root", JSONObject
                                .of("id", 101)
                        )
                        .getObject("root", (Type) Bean2.class, JSONReader.Feature.FieldBased))
                        .id
        );

        assertEquals(101,
                ((Bean2) JSONArray.of(JSONObject
                                .of("id", 101)
                        )
                        .getObject(0, Bean2.class, JSONReader.Feature.FieldBased))
                        .id
        );

        assertEquals(101,
                ((Bean2) JSONArray.of(JSONObject
                                .of("id", 101)
                        )
                        .getObject(0, (Type) Bean2.class, JSONReader.Feature.FieldBased))
                        .id
        );
    }

    @Test
    public void test_2_toJavaObjectType() {
        assertEquals(101,
                ((Bean2) JSONObject
                        .of("id", 101)
                        .toJavaObject((Type) Bean2.class, JSONReader.Feature.FieldBased))
                        .id
        );
    }

    @Test
    public void test_2_toJavaList() {
        assertEquals(101,
                JSONArray.of(
                                JSONObject
                                        .of("id", 101)
                        )
                        .toJavaList(Bean2.class, JSONReader.Feature.FieldBased)
                        .get(0)
                        .id
        );
    }

    private static class Bean2 {
        private int id;

        private Bean2() {
        }
    }
}
