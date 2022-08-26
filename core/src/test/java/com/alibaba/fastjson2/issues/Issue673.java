package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue673 {
    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.value = true;
        assertEquals("{\"value\":true}", JSON.toJSONString(bean));
    }

    public static class Bean {
        private Boolean value;

        public Boolean isValue() {
            return value;
        }

        public void setValue(Boolean value) {
            this.value = value;
        }
    }
}
