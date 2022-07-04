package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPathTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.f0 = true;
        Map<String, Object> paths = JSONPath.paths(bean);
        assertEquals(true, paths.get("$.f0"));
    }

    @Test
    public void test1() {
        assertEquals(Boolean.TRUE, JSONPath.paths(Boolean.TRUE).get("$"));
        assertEquals(BigDecimal.ONE, JSONPath.paths(BigDecimal.ONE).get("$"));
    }

    public static class Bean {
        public Boolean f0;
    }
}
