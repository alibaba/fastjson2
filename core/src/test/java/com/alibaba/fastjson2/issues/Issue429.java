package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue429 {
    @Test
    public void test() {
        assertFalse(JSON.parseObject("{\"value\":\"\"}", Bean.class).value);
        assertFalse(JSON.parseObject("{\"value\":\"null\"}", Bean.class).value);
        assertFalse(JSON.parseObject("{\"value\":\"\"}").to(Bean.class).value);
        assertFalse(JSON.parseObject("{\"value\":\"null\"}").to(Bean.class).value);
    }

    public static class Bean {
        public boolean value;
    }

    @Test
    public void test1() {
        assertNull(JSON.parseObject("{\"value\":\"\"}", Bean1.class).value);
        assertNull(JSON.parseObject("{\"value\":\"null\"}", Bean1.class).value);
        assertNull(JSON.parseObject("{\"value\":\"\"}").to(Bean1.class).value);
        assertNull(JSON.parseObject("{\"value\":\"null\"}").to(Bean1.class).value);
    }

    public static class Bean1 {
        public Boolean value;
    }
}
