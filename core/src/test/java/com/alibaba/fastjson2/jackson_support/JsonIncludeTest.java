package com.alibaba.fastjson2.jackson_support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @JsonInclude
    public static class Bean1 {
        public int id;
        public String name;
    }

    @Test
    public void test2() throws Exception {
        Bean2 bean = new Bean2();
        bean.id = 101;
        String str = new ObjectMapper().writeValueAsString(bean);
        assertEquals("{\"id\":101}", str);
    }

    @JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
    public static class Bean2 {
        public int id;
        public int value;
    }
}
