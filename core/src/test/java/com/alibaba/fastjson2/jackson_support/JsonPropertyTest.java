package com.alibaba.fastjson2.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonPropertyTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.category = "Tech";

        String json = JSON.toJSONString(bean);
        assertEquals("{\"bookCategory\":\"Tech\"}", json);

        Bean bean1 = JSON.parseObject(json, Bean.class);
        assertEquals(bean.category, bean1.category);
    }

    public static class Bean {
        @JsonProperty("bookCategory")
        public String category;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.category = "Tech";

        String json = JSON.toJSONString(bean);
        assertEquals("{\"bookCategory\":\"Tech\"}", json);

        Bean1 bean1 = JSON.parseObject(json, Bean1.class);
        assertEquals(bean.category, bean1.category);
    }

    public static class Bean1 {
        @JsonProperty("bookCategory")
        private String category;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.category = "Tech";

        String json = JSON.toJSONString(bean);
        assertEquals("{\"bookCategory\":\"Tech\"}", json);

        Bean2 bean1 = JSON.parseObject(json, Bean2.class);
        assertEquals(bean.category, bean1.category);
    }

    public static class Bean2 {
        private String category;

        @JsonProperty("bookCategory")
        public String getCategory() {
            return category;
        }

        @JsonProperty("bookCategory")
        public void setCategory(String category) {
            this.category = category;
        }
    }
}
