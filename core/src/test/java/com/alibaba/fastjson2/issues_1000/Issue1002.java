package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1002 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"item\":[{\"id\":123}]}", Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(bean.item);
        assertEquals(123, bean.item.id);
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject("{\"item\":[{\"id\":123}]}", Bean1.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(bean.item);
        assertEquals(123, bean.item.id);
    }

    private static class Bean {
        public Item item;
    }

    public static class Bean1 {
        public Item item;
    }

    public static class Item {
        public int id;
    }
}
