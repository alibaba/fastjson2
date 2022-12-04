package com.alibaba.fastjson2.adapter.jackson.databind.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonAnySetter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonJsonAnySetterTest {
    @Test
    public void whenDeserializingUsingJsonAnySetter_thenCorrect() {
        String str = "{\"name\":\"My bean\",\"attr1\":\"val1\"}";

        ExtendableBean bean2 = JSON.parseObject(str, ExtendableBean.class);
        assertEquals("My bean", bean2.name);
        assertEquals("val1", bean2.properties.get("attr1"));
    }

    public static class ExtendableBean {
        public String name;
        private Map<String, String> properties = new HashMap<>();

        @JsonAnySetter
        public void add(String key, String value) {
            properties.put(key, value);
        }
    }
}
