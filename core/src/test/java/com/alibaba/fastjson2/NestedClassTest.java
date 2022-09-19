package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NestedClassTest {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"id\":101,\"item\":{\"itemId\":102}}", Bean.class);
        assertNotNull(bean.item);
        assertEquals(102, bean.item.itemId);
        assertEquals(101, bean.item.getParentId());
    }

    public static class Bean {
        public int id;
        public Item item;

        private class Item {
            public int itemId;

            public Item() {
            }

            public Item(int itemId) {
                this.itemId = itemId;
            }

            public int getParentId() {
                return id;
            }
        }
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject("{\"id\":101,\"item\":{\"itemId\":102}}", Bean1.class);
        assertNotNull(bean.item);
        assertEquals(102, bean.item.itemId);
        assertEquals(101, bean.item.getParentId());
    }

    public static class Bean1 {
        public int id;
        public Item item;

        private class Item {
            public int itemId;

            public Item(int itemId) {
                this.itemId = itemId;
            }

            public int getParentId() {
                return id;
            }
        }
    }

    @Test
    public void test2() {
        Bean2 bean = JSON.parseObject("{\"id\":101,\"item\":{\"itemId\":102}}", Bean2.class);
        assertNotNull(bean.item);
        assertEquals(102, bean.item.itemId);
        assertEquals(101, bean.item.getParentId());
    }

    public static class Bean2 {
        public int id;
        public Item item;

        public class Item {
            public int itemId;

            public Item(int itemId) {
                this.itemId = itemId;
            }

            public int getParentId() {
                return id;
            }
        }
    }

    @Test
    public void test3() {
        Bean3 bean = JSON.parseObject("{\"id\":101,\"item\":{\"itemId\":102}}", Bean3.class);
        assertNotNull(bean.item);
        assertEquals(102, bean.item.itemId);
        assertEquals(101, bean.item.getParentId());
    }

    public static class Bean3 {
        public int id;
        public Item item;

        public class Item {
            public int itemId;

            public Item() {
            }

            public Item(int itemId) {
                this.itemId = itemId;
            }

            public int getParentId() {
                return id;
            }
        }
    }
}
