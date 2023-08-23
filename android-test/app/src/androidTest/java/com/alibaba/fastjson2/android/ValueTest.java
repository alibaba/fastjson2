package com.alibaba.fastjson2.android;

import static org.junit.Assert.assertEquals;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;

import org.junit.jupiter.api.Test;

public class ValueTest {
    @Test
    public void test6() {
        String str = "123";
        Bean6 bean = JSON.parseObject(str, Bean6.class);
        assertEquals(123, bean.value);
    }

    public static class Bean6 {
        private final int value;

        @JSONCreator
        public Bean6(@JSONField(value = true) int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
