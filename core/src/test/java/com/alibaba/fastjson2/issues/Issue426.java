package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue426 {
    @Test
    public void test0() {
        Bean bean = JSON.parseObject("{\"name\": \"zhangsan\", \"books\" : \"西游记\"}", Bean.class);
        assertEquals("zhangsan", bean.name);
        assertEquals(1, bean.books.size());
        assertEquals("西游记", bean.books.get(0));
    }

    private static class Bean {
        public String name;
        public List<String> books;
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject("{\"name\": \"zhangsan\", \"books\" : \"西游记\"}", Bean1.class);
        assertEquals("zhangsan", bean.name);
        assertEquals(1, bean.books.size());
        assertEquals("西游记", bean.books.get(0));
    }

    public static class Bean1 {
        private String name;
        private final List<String> books = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getBooks() {
            return books;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = JSON.parseObject("{\"name\": \"zhangsan\", \"books\" : \"西游记\"}", Bean2.class);
        assertEquals("zhangsan", bean.name);
        assertEquals(1, bean.books.size());
        assertEquals("西游记", bean.books.get(0));
    }

    private static class Bean2 {
        public String name;
        public final List<String> books = new ArrayList<>();
    }

    @Test
    public void test3() {
        Bean3 bean = JSON.parseObject("{\"name\": \"zhangsan\", \"books\" : \"西游记\"}", Bean3.class);
        assertEquals("zhangsan", bean.name);
        assertEquals(1, bean.books.size());
        assertEquals("西游记", bean.books.get(0));
    }

    public static class Bean3 {
        public String name;
        public List<String> books;
    }

    @Test
    public void test4() {
        Bean4 bean = JSON.parseObject("{\"name\": \"zhangsan\", \"books\" : \"西游记\"}", Bean4.class);
        assertEquals("zhangsan", bean.name);
        assertEquals(1, bean.books.size());
        assertEquals("西游记", bean.books.get(0));
    }

    public static class Bean4 {
        private String name;
        private List<String> books;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getBooks() {
            return books;
        }

        public void setBooks(List<String> books) {
            this.books = books;
        }
    }
}
