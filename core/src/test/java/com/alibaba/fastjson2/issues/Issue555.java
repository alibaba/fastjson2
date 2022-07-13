package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue555 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"values\":Set[]}", Bean.class);
        assertTrue(bean.values.isEmpty());
    }

    public static class Bean {
        private Set<String> values;

        public Set<String> getValues() {
            return values;
        }

        public void setValues(Set<String> values) {
            this.values = values;
        }
    }
}
