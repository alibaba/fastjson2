package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectTest_from {
    @Test
    public void test_0() {
        assertEquals(101, JSONObject.from(new Item(101)).get("itemId"));
    }

    @Test
    public void test_1() {
        Bean bean = new Bean();
        bean.setItems(Arrays.asList(new Item(101), new Item(102)));
        assertEquals(bean.getItems().get(0).itemId,
                JSONObject.from(bean).to(Bean.class).items.get(0).itemId);
    }

    @Test
    public void test_2() {
        Bean1 bean = new Bean1();
        {
            HashMap<String, Item> itemMap = new HashMap<>();
            itemMap.put("101", new Item(101));
            itemMap.put("102", new Item(102));
            bean.items = itemMap;
        }
        assertEquals(bean.items.get("101").itemId,
                JSONObject.from(bean).to(Bean1.class).items.get("101").itemId);
    }


    public static class Bean {
        private List<Item> items;

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }
    }

    public static class Bean1 {
        public Map<String, Item> items;
    }

    public static class Item {
        public int itemId;

        public Item(int itemId) {
            this.itemId = itemId;
        }
    }
}
