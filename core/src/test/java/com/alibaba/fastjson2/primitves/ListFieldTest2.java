package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListFieldTest2 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"items\":{\"id\":123}}", Bean.class);
        assertEquals(123, bean.items.get(0).id);
    }

    public static class Bean {
        public List<Item> items;
    }

    public static class Item {
        public int id;
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject("{\"items\":123}", Bean1.class);
        assertEquals(123, bean.items.get(0));
    }

    public static class Bean1 {
        public List<Integer> items;
    }
}
