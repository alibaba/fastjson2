package com.alibaba.fastjson2.adapter.jackson.databind.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonCreator;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonJsonCreatorTest {
    @Test
    public void test() {
        String json = "{\"id\":1,\"theName\":\"My bean\"}";
        BeanWithCreator bean = JSON.parseObject(json, BeanWithCreator.class);
        assertEquals("My bean", bean.name);
    }

    public static class BeanWithCreator {
        public int id;
        public String name;

        @JsonCreator
        public BeanWithCreator(
                @JsonProperty("id") int id,
                @JsonProperty("theName") String name) {
            this.id = id;
            this.name = name;
        }
    }
}
