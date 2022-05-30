package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONFieldValueTest {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("\"abc\"", Bean.class);
        assertEquals("abc", bean.value);
    }

    public static class Bean {
        private String value;

        public Bean(@JSONField(value = true) String value) {
            this.value = value;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject("\"abc\"", Bean1.class);
        assertEquals("abc", bean.value);
    }

    public static class Bean1 {
        private String value;

        private Bean1(String value) {
            this.value = value;
        }

        @JSONCreator
        public static Bean1 of(@JSONField(value = true) String value) {
            return new Bean1(value);
        }
    }

    @Test
    public void test2() {
        Bean2 bean = JSON.parseObject("\"abc\"", Bean2.class);
        assertEquals("abc", bean.value);
    }

    public static class Bean2 {
        private String value;

        @JSONCreator
        public Bean2(@JSONField(value = true) String value) {
            this.value = value;
        }

        public Bean2(int value) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void test3() {
        JSON.mixIn(Bean3.class, Bean3Mixin.class);
        Bean3 bean = JSON.parseObject("\"abc\"", Bean3.class);
        assertEquals("abc", bean.value);
    }

    public static class Bean3 {
        private String value;

        public Bean3(String value) {
            this.value = value;
        }

        public Bean3(int value) {
            throw new UnsupportedOperationException();
        }
    }

    public static class Bean3Mixin {
        @JSONCreator
        public Bean3Mixin(@JSONField(value = true) String value) {
        }
    }
}
