package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue854 {
    @Test
    public void test() {
        Bean bean = JSONObject.of("value", 1).to(Bean.class);
        assertTrue(bean.value);
    }

    public static class Bean {
        public boolean value;
    }
}
