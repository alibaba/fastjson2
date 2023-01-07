package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenericTest {
    @Test
    public void test() {
        assertEquals("{\"value\":\"12\"}", JSON.toJSONString(new Bean("12")));
        assertEquals("{\"value\":1}", JSON.toJSONString(new Bean(BigDecimal.ONE)));
        assertEquals("{\"value\":[1]}", JSON.toJSONString(new Bean(new BigDecimal[]{BigDecimal.ONE})));
    }

    public static class Bean<T> {
        public T value;

        public Bean(T value) {
            this.value = value;
        }
    }

    @Test
    public void test1() {
        assertEquals("{\"value\":1.00}", JSON.toJSONString(new Bean1(BigDecimal.ONE)));
        assertEquals("{\"value\":[1.00]}", JSON.toJSONString(new Bean1(new BigDecimal[]{BigDecimal.ONE})));
    }

    public static class Bean1<T> {
        @JSONField(format = "0.00")
        public T value;

        public Bean1(T value) {
            this.value = value;
        }
    }

    @Test
    public void test2() {
        assertEquals("{\"value\":[1.00]}", JSON.toJSONString(new Bean2(new BigDecimal[]{BigDecimal.ONE})));
    }

    public static class Bean2<T> {
        @JSONField(format = "0.00")
        public T value;

        public Bean2(T value) {
            this.value = value;
        }
    }
}
