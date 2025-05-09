package com.alibaba.fastjson2.issues_3500;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue3537 {
    @Test
    public void test() {
        String json = "{\"value\":\"\"}";
        SimpleTestValue s = JSON.parseObject(json, SimpleTestValue.class);
        assertEquals("", s.getValue());
        json = "{\"value\":null}";
        s = JSON.parseObject(json, SimpleTestValue.class);
        assertNull(s.getValue());
    }

    public static class SimpleTestValue {
        private Object value;

        // 标准 getter/setter
        public Object getValue() {
            return value;
        }
        public void setValue(Object value) {
            this.value = value;
        }

        // 自定义 getter 会导致问题
        public List<String> getRelates() {
            return Collections.emptyList();
        }
    }
}
