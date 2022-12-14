package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.adapter.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonIncludeTest {
    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.id = 101;
        String str = new ObjectMapper().writeValueAsString(bean);
        assertEquals("{\"id\":101}", str);
    }

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class Bean {
        public int id;
        public String name;
    }
}
