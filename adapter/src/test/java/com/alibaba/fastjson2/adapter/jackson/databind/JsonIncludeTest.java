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

    @Test
    public void test1() throws Exception {
        Bean1 bean = new Bean1();
        bean.id = 101;
        String str = new ObjectMapper().writeValueAsString(bean);
        assertEquals("{\"id\":101,\"name\":null}", str);
    }

    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public static class Bean1 {
        public int id;
        public String name;
    }
}
