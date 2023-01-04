package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SingleItemListTest {
    @Test
    public void test1() {
        String str = "{\"item\":[{\"id\":101,\"name\":\"DataWorks\"}]}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Bean1.class));

        Bean1 bean = JSON.parseObject(str, Bean1.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, bean.item.id);
        assertEquals("DataWorks", bean.item.name);
    }

    private static class Bean1 {
        public Item item;
    }

    private static class Item {
        public int id;
        public String name;
    }

    public static class Item1 {
        public int id;
        public String name;
    }

    @Test
    public void test1_public() {
        String str = "{\"item\":[{\"id\":101,\"name\":\"DataWorks\"}]}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Bean1P.class));

        Bean1P bean = JSON.parseObject(str, Bean1P.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, bean.item.id);
        assertEquals("DataWorks", bean.item.name);
    }

    public static class Bean1P {
        public Item1 item;
    }

    @Test
    public void test2() {
        String str = "{\"item\":[{\"id\":101,\"name\":\"DataWorks\"}]}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Bean2.class));

        Bean2 bean = JSON.parseObject(str, Bean2.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, bean.item.id);
        assertEquals("DataWorks", bean.item.name);
    }

    private static class Bean2 {
        public Item item;
        public Item item1;
    }

    @Test
    public void test3() {
        String str = "{\"item\":[{\"id\":101,\"name\":\"DataWorks\"}]}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Bean3.class));

        Bean3 bean = JSON.parseObject(str, Bean3.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, bean.item.id);
        assertEquals("DataWorks", bean.item.name);
    }

    private static class Bean3 {
        public Item item;
        public Item item1;
        public Item item2;
    }

    @Test
    public void test4() {
        String str = "{\"item\":[{\"id\":101,\"name\":\"DataWorks\"}]}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Bean4.class));

        Bean4 bean = JSON.parseObject(str, Bean4.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, bean.item.id);
        assertEquals("DataWorks", bean.item.name);
    }

    private static class Bean4 {
        public Item item;
        public Item item1;
        public Item item2;
        public Item item3;
    }

    @Test
    public void test5() {
        String str = "{\"item\":[{\"id\":101,\"name\":\"DataWorks\"}]}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Bean5.class));

        Bean5 bean = JSON.parseObject(str, Bean5.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, bean.item.id);
        assertEquals("DataWorks", bean.item.name);
    }

    private static class Bean5 {
        public Item item;
        public Item item1;
        public Item item2;
        public Item item3;
        public Item item4;
    }

    @Test
    public void test6() {
        String str = "{\"item\":[{\"id\":101,\"name\":\"DataWorks\"}]}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Bean6.class));

        Bean6 bean = JSON.parseObject(str, Bean6.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, bean.item.id);
        assertEquals("DataWorks", bean.item.name);
    }

    private static class Bean6 {
        public Item item;
        public Item item1;
        public Item item2;
        public Item item3;
        public Item item4;
        public Item item5;
    }

    @Test
    public void test7() {
        String str = "{\"item\":[{\"id\":101,\"name\":\"DataWorks\"}]}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Bean7.class));

        Bean7 bean = JSON.parseObject(str, Bean7.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, bean.item.id);
        assertEquals("DataWorks", bean.item.name);
    }

    private static class Bean7 {
        public Item item;
        public Item item1;
        public Item item2;
        public Item item3;
        public Item item4;
        public Item item5;
        public Item item6;
    }

    @Test
    public void test8() {
        String str = "{\"item\":[{\"id\":101,\"name\":\"DataWorks\"}]}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Bean8.class));

        Bean8 bean = JSON.parseObject(str, Bean8.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, bean.item.id);
        assertEquals("DataWorks", bean.item.name);
    }

    private static class Bean8 {
        public Item item;
        public Item item1;
        public Item item2;
        public Item item3;
        public Item item4;
        public Item item5;
        public Item item6;
        public Item item7;
    }

    @Test
    public void test8_public() {
        String str = "{\"item\":[{\"id\":101,\"name\":\"DataWorks\"}]}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Bean8P.class));

        Bean8P bean = JSON.parseObject(str, Bean8P.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, bean.item.id);
        assertEquals("DataWorks", bean.item.name);
    }

    public static class Bean8P {
        public Item item;
        public Item item1;
        public Item item2;
        public Item item3;
        public Item item4;
        public Item item5;
        public Item item6;
        public Item item7;
    }
}
