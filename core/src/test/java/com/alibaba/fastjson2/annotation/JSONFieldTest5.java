package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONFieldTest5 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 100;

        assertEquals("{\"id\":\"100\"}", JSON.toJSONString(bean));
    }

    public static class Bean {
        @JSONField(serializeFeatures = Feature.WriteNonStringValueAsString)
        public int id;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.type = Type.M;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"type\":102}", str);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.type, bean1.type);
    }

    public static class Bean1 {
        public Type type;
    }

    public enum Type {
        X(101, "Big"),
        M(102, "Medium"),
        S(103, "Small");

        private final int code;
        private final String name;

        Type(int code, String name) {
            this.code = code;
            this.name = name;
        }

        @JSONField(value = true)
        public int getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2(101);
        String str = JSON.toJSONString(bean);
        assertEquals("101", str);
        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(bean.code, bean1.code);
    }

    public static class Bean2 {
        private final int code;

        public Bean2(@JSONField(value = true) int code) {
            this.code = code;
        }

        @JSONField (value = true)
        public int getCode() {
            return code;
        }
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.id = 1001;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"bean_id\":1001}", str);

        assertThrows(
                Exception.class,
                () -> JSON.parseObject("{\"bean_id\":null}", Bean3.class)
        );
        assertThrows(
                Exception.class,
                () -> JSON.parseObject("{}", Bean3.class)
        );
    }

    public static class Bean3 {
        @JSONField(name = "bean_id", required = true)
        public Integer id;
    }
}
