package com.alibaba.fastjson2.internal.processor.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.values = Arrays.asList("a", "b", "c");
        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.values.size(), bean1.values.size());
        String str1 = JSON.toJSONString(bean1);
        assertEquals(str, str1);
    }

    @JSONCompiled
    public static class Bean {
        public List<String> values;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.values = Arrays.asList("a", "b", "c");
        String str = JSON.toJSONString(bean);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.values.size(), bean1.values.size());
        String str1 = JSON.toJSONString(bean1);
        assertEquals(str, str1);
    }

    @JSONCompiled
    public static class Bean1 {
        public List values;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.values = Arrays.asList(new Item(1), new Item(2), new Item(3));
        String str = JSON.toJSONString(bean);
        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(bean.values.size(), bean1.values.size());
        String str1 = JSON.toJSONString(bean1);
        assertEquals(str, str1);
    }

    @JSONCompiled
    public static class Bean2 {
        public List<Item> values;
    }

    @JSONCompiled
    public static class Item {
        public int id;

        public Item() {
        }

        public Item(int id) {
            this.id = id;
        }
    }
//
//    @Test
//    public void test3() {
//        Bean3 bean = new Bean3();
//        bean.values = Arrays.asList("a", "b", "c");
//        String str = JSON.toJSONString(bean);
//        Bean3 bean1 = JSON.parseObject(str, Bean3.class);
//        assertEquals(bean.values.size(), bean1.values.size());
//        String str1 = JSON.toJSONString(bean1);
//        assertEquals(str, str1);
//    }
//
//    @JSONCompiled
//    public static class Bean3 {
//        public List<?> values;
//    }
}
