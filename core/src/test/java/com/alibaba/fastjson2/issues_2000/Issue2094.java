package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2094 {
    @Test
    public void test() {
        String str = "{\"items\":{1:{\"id\":123}}}";
        Bean bean = JSON.parseObject(str, Bean.class);
        assertEquals(1, bean.items.size());
        assertEquals(123, bean.items.get(1).id);
    }

    @Data
    public static class Bean {
        private SortedMap<Integer, Item> items;
    }

    public static class Item {
        public int id;
    }
}
