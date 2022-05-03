package com.alibaba.fastjson2.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonJsonIgnorePropertiesTest {
    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.id = 101;
        bean.name = "XX";

        String str = JSON.toJSONString(bean);
        assertEquals("{\"id\":101}", str);
    }

    @JsonIgnoreProperties("name")
    public static class Bean {
        public int id;
        public String name;
    }
}
