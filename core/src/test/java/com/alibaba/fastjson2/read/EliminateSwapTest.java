package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EliminateSwapTest {
    static final String str = "{\"id\":123,\"name\":\"abc\"}";

    @Test
    public void test() {
        Bean bean = JSON.parseObject(str, Bean.class);
        assertEquals(123, bean.id);
        assertEquals("abc", bean.name);
    }

    public static class Bean {
        public int id;
        public String name;
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject(str, Bean1.class, JSONReader.Feature.FieldBased);
        assertEquals(123, bean.id);
        assertEquals("abc", bean.name);
    }

    private static class Bean1 {
        private int id;
        private String name;
    }

    @Test
    public void test2() {
        Bean2 bean = JSON.parseObject(str, Bean2.class);
        assertEquals(123, bean.id);
        assertEquals("abc", bean.name);
    }

    public static class Bean2 {
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

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void test3() {
        Bean3 bean = JSON.parseObject(str, Bean3.class);
        assertEquals(123, bean.id);
        assertEquals("abc", bean.name);
    }

    private static class Bean3 {
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

        public void setName(String name) {
            this.name = name;
        }
    }
}
