package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONFieldFormat {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.value = 101;
        assertEquals("{\"value\":\"101\"}", JSON.toJSONString(bean));
    }

    static class Bean {
        @JSONField(format = "string")
        public long value;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.value = 101;
        assertEquals("{\"value\":\"101\"}", JSON.toJSONString(bean));
    }

    public static class Bean1 {
        @JSONField(format = "string")
        public long value;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.value = 101;
        assertEquals("{\"value\":\"101\"}", JSON.toJSONString(bean));
    }

    private static class Bean2 {
        @JSONField(format = "string")
        private long value;

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.value = 101;
        assertEquals("{\"value\":\"101\"}", JSON.toJSONString(bean));
    }

    public static class Bean3 {
        @JSONField(format = "string")
        private long value;

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }
}
