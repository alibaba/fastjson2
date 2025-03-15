package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FactoryTest {
    @Test
    public void test1() {
        assertEquals(123,
                JSON.parseObject("{\"value\": 123}", Bean1.class).value);
    }

    private static class Bean1 {
        private int value;

        private Bean1(int value) {
            this.value = value;
        }

        @JSONCreator
        public static Bean1 create(int value) {
            return new Bean1(value);
        }
    }

    @Test
    public void test2() {
        assertEquals(123,
                JSON.parseObject("{\"id\": 123, \"name\":\"abc\"}", Bean2.class).id);
    }

    private static class Bean2 {
        private int id;
        private String name;

        public Bean2(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @JSONCreator
        public static Bean2 create(int id, String name) {
            return new Bean2(id, name);
        }
    }

    @Test
    public void test3() {
        assertEquals(16,
                JSON.parseObject("{\"id\": 123, \"name\":\"abc\", \"age\":16}", Bean3.class).age);
    }

    private static class Bean3 {
        private int id;
        private String name;
        private int age;

        public Bean3(int id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        @JSONCreator
        public static Bean3 create(int id, String name, int age) {
            return new Bean3(id, name, age);
        }
    }
}
