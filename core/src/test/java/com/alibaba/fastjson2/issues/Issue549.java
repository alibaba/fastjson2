package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue549 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.value = 1001;

        assertEquals("{\"value\":1001}", JSON.toJSONString(bean));
    }

    public static class Bean {
        private int value;

        public boolean isValue() {
            return value != 0;
        }

        public int getValue() {
            return value;
        }
    }
}
