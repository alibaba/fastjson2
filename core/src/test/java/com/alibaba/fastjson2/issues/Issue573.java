package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Issue573 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"items\":[null,null]}", Bean.class);
        assertNotNull(bean);
        assertEquals(2, bean.items.size());
        assertNull(bean.items.get(0));
        assertNull(bean.items.get(1));
    }

    public static class Bean {
        public List<Item> items;
    }

    public static class Item {
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject("{\"items\":[null,null]}", Bean1.class);
        assertNotNull(bean);
        assertEquals(2, bean.items.size());
        assertNull(bean.items.get(0));
        assertNull(bean.items.get(1));
    }

    private static class Bean1 {
        public List<Item> items;
    }

    @Test
    public void test2() {
        Bean2 bean = JSON.parseObject("{\"items\":[null,null]}", Bean2.class);
        assertNotNull(bean);
        assertEquals(2, bean.items.size());
        assertNull(bean.items.get(0));
        assertNull(bean.items.get(1));
    }

    public static class Bean2 {
        private List<Item> items;

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }
    }

    @Test
    public void test3() {
        Bean3 bean = JSON.parseObject("{\"items\":[null,null]}", Bean3.class);
        assertNotNull(bean);
        assertEquals(2, bean.items.size());
        assertNull(bean.items.get(0));
        assertNull(bean.items.get(1));
    }

    public static class Bean3 {
        private List<Item> items = new ArrayList<>();

        public List<Item> getItems() {
            return items;
        }
    }

    @Test
    public void test4() {
        Bean4 bean = JSON.parseObject("{\"items\":[null,null]}", Bean4.class, JSONReader.Feature.FieldBased);
        assertNotNull(bean);
        assertEquals(2, bean.items.size());
        assertNull(bean.items.get(0));
        assertNull(bean.items.get(1));
    }

    public static class Bean4 {
        private List<Item> items = new ArrayList<>();
    }

    @Test
    public void test5() {
        Bean5 bean = JSON.parseObject("{\"items\":[null,null]}", Bean5.class);
        assertNotNull(bean);
        assertEquals(2, bean.items.size());
        assertNull(bean.items.get(0));
        assertNull(bean.items.get(1));
    }

    public static class Bean5 {
        public List<String> items;
    }

    @Test
    public void test6() {
        Bean6 bean = JSON.parseObject("{\"items\":[null,null]}", Bean6.class);
        assertNotNull(bean);
        assertEquals(2, bean.items.size());
        assertNull(bean.items.get(0));
        assertNull(bean.items.get(1));
    }

    public static class Bean6 {
        public List<Integer> items;
    }
}
