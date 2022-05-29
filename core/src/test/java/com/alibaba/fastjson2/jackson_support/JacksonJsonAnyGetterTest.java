package com.alibaba.fastjson2.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonJsonAnyGetterTest {
    @Test
    public void test() {
        ExtendableBean bean = new ExtendableBean("My bean");
        bean.properties.put("attr1", "val1");
        assertEquals("{\"name\":\"My bean\",\"attr1\":\"val1\"}", JSON.toJSONString(bean));
    }

    public static class ExtendableBean {
        public String name;
        private Map<String, String> properties = new HashMap<>();

        public ExtendableBean(String name) {
            this.name = name;
        }

        @JsonAnyGetter
        public Map<String, String> getProperties() {
            return properties;
        }
    }
}
