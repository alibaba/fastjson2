package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2593 {
    @Test
    public void test() {
        String str = "{\"test\":[{\"demo\": 1}]}";
        Bean bean = JSON.parseObject(str, Bean.class);
        List<Item> items = bean.getTest();
        assertEquals(1, items.size());
        assertEquals(1, items.get(0).demo.length);
        assertEquals(1, items.get(0).demo[0]);
    }

    @Data
    static class Bean {
        List<Item> test;
    }

    @Data
    static class Item {
        private Integer[] demo;
    }

    @Test
    public void test1() {
        Long[] longs = JSON.parseObject("123", Long[].class);
        assertEquals(1, longs.length);
        assertEquals(123L, longs[0]);
    }
}
