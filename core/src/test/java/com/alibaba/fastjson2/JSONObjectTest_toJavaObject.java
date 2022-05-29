package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectTest_toJavaObject {
    @Test
    public void test0() {
        assertEquals(101,
                JSONObject
                        .of("itemId", 101)
                        .toJavaObject(Item.class)
                        .itemId
        );
    }

    @Test
    public void test1() {
        assertEquals(101,
                JSONObject
                        .of("items",
                                JSONArray
                                        .of(JSONObject.of("itemId", 101))
                        )
                        .toJavaObject(Bean.class)
                        .items
                        .get(0).itemId
        );
    }

    @Test
    public void test2() {
        assertEquals(101,
                JSONObject
                        .of("items",
                                JSONArray
                                        .of(JSONObject.of("itemId", 101))
                        )
                        .toJavaObject(Bean1.class)
                        .items
                        .get(0).itemId
        );
    }

    @Test
    public void test3() {
        List<Item> items =
                JSONArray
                        .of(JSONObject.of("itemId", 101))
                        .toJavaObject(
                                new TypeReference<List<Item>>() {
                                }.getType()
                        );

        assertEquals(
                101,
                items.get(0).itemId
        );
    }

    @Test
    public void test4() {
        assertEquals(101,
                JSONObject
                        .of("items",
                                JSONObject
                                        .of("first", JSONObject.of("itemId", 101))
                        )
                        .toJavaObject(Bean3.class)
                        .items
                        .get("first").itemId
        );
    }

    public static class Bean {
        public List<Item> items;
    }

    public static class Bean1 {
        private List<Item> items;

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }
    }

    public static class Bean3 {
        public Map<String, Item> items;
    }

    public static class Item {
        public int itemId;
    }
}
