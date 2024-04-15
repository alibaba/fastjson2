package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertiesUtilsTest {
    @Test
    public void test() {
        Properties properties = new Properties();
        properties.put("id", "123");
        properties.put("name", "xyz");

        Bean bean = PropertiesUtils.toJavaObject(properties, Bean.class);
        assertEquals(123, bean.id);
        assertEquals("xyz", bean.name);

        Properties properties1 = PropertiesUtils.toProperties(bean);
        assertEquals(2, properties1.size());
        assertEquals("123", properties1.getProperty("id"));
        assertEquals("xyz", properties1.getProperty("name"));
    }

    public static class Bean {
        public int id;
        public String name;
    }

    @Test
    public void test1() {
        Properties properties = new Properties();
        properties.put("id", "123");
        properties.put("item.name", "xyz");

        Bean1 bean = PropertiesUtils.toJavaObject(properties, Bean1.class);
        assertEquals(123, bean.id);
        assertEquals("xyz", bean.item.name);

        Properties properties1 = PropertiesUtils.toProperties(bean);
        assertEquals(2, properties1.size());
        assertEquals("123", properties1.getProperty("id"));
        assertEquals("xyz", properties1.getProperty("item.name"));
    }

    public static class Bean1 {
        public int id;
        public Item1 item;
    }

    public static class Item1 {
        public String name;
    }

    @Test
    public void test2() {
        Properties properties = new Properties();
        properties.put("id", "123");
        properties.put("items[0].name", "abc");
        properties.put("items[1].name", "xyz");

        Bean2 bean = PropertiesUtils.toJavaObject(properties, Bean2.class);
        assertEquals(123, bean.id);
        assertEquals("abc", bean.items.get(0).name);
        assertEquals("xyz", bean.items.get(1).name);

        Properties properties1 = PropertiesUtils.toProperties(bean);
        assertEquals(3, properties1.size());
        assertEquals("123", properties1.getProperty("id"));
        assertEquals("abc", properties1.getProperty("items[0].name"));
        assertEquals("xyz", properties1.getProperty("items[1].name"));
    }

    public static class Bean2 {
        public int id;
        public List<Item2> items;
    }

    public static class Item2 {
        public String name;
    }
}
