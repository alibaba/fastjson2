package com.alibaba.fastjson2.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
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
}
