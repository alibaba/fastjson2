package com.alibaba.fastjson2.adapter.jackson.databind.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JsonPropertyTest1 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 123;
        bean.name = "XXX";

        String json = JSON.toJSONString(bean);
        assertEquals("{\"id\":123}", json);

        Bean bean1 = JSON.parseObject("{\"id\":123,\"name\":\"XXX\"}", Bean.class);
        assertEquals(123, bean1.id);
        assertEquals("XXX", bean1.name);
    }

    public static class Bean {
        public int id;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        public String name;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.id = 123;
        bean.name = "XXX";

        String json = JSON.toJSONString(bean);
        assertEquals("{\"id\":123,\"name\":\"XXX\"}", json);

        Bean1 bean1 = JSON.parseObject(json, Bean1.class);
        assertEquals(bean.id, bean1.id);
        assertNull(bean1.name);
    }

    public static class Bean1 {
        public int id;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        public String name;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.id = 123;
        bean.name = "XXX";

        String json = JSON.toJSONString(bean);
        assertEquals("{\"id\":123,\"name\":\"XXX\"}", json);

        Bean2 bean1 = JSON.parseObject(json, Bean2.class);
        assertEquals(bean.id, bean1.id);
        assertEquals(bean.name, bean1.name);
    }

    public static class Bean2 {
        @JsonProperty
        private int id;

        @JsonProperty
        private String name;
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.id = 123;
        bean.name = "XXX";

        String json = JSON.toJSONString(bean);
        assertEquals("{\"id\":123,\"name\":\"XXX\"}", json);

        Bean3 bean1 = JSON.parseObject(json, Bean3.class);
        assertEquals(bean.id, bean1.id);
        assertEquals(bean.name, bean1.name);
    }

    private static class Bean3 {
        @JsonProperty
        private int id;

        @JsonProperty
        private String name;
    }
}
