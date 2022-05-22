package com.alibaba.fastjson2.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonAlias;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonAliasTest1 {
    @Test
    public void test() {
        {
            Bean bean1 = JSON.parseObject("{\"id\":123,\"name\":\"XXX\"}", Bean.class);
            assertEquals(123, bean1.id);
            assertEquals("XXX", bean1.name);
        }
        {
            Bean bean1 = JSON.parseObject("{\"id\":123,\"bkcat\":\"XXX\"}", Bean.class);
            assertEquals(123, bean1.id);
            assertEquals("XXX", bean1.name);
        }
        {
            Bean bean1 = JSON.parseObject("{\"id\":123,\"mybkcat\":\"XXX\"}", Bean.class);
            assertEquals(123, bean1.id);
            assertEquals("XXX", bean1.name);
        }
    }

    public static class Bean {
        public int id;

        @JsonAlias({"bkcat", "mybkcat"})
        public String name;
    }

    @Test
    public void test1() {
        {
            Bean1 bean1 = JSON.parseObject("{\"id\":123,\"name\":\"XXX\"}", Bean1.class);
            assertEquals(123, bean1.id);
            assertEquals("XXX", bean1.name);
        }
        {
            Bean1 bean1 = JSON.parseObject("{\"id\":123,\"bkcat\":\"XXX\"}", Bean1.class);
            assertEquals(123, bean1.id);
            assertEquals("XXX", bean1.name);
        }
        {
            Bean1 bean1 = JSON.parseObject("{\"id\":123,\"mybkcat\":\"XXX\"}", Bean1.class);
            assertEquals(123, bean1.id);
            assertEquals("XXX", bean1.name);
        }
    }

    public static class Bean1 {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        @JsonAlias({"bkcat", "mybkcat"})
        public void setName(String name) {
            this.name = name;
        }
    }
}
