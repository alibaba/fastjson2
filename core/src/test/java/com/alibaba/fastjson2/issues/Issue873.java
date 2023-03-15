package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue873 {
    @Test
    public void test() {
        assertNull(JSON.parseObject((String) null, Bean.class));
        assertNull(JSON.parseObject("", Bean.class));
        assertNull(JSON.parseObject("{\"item\":\"\"}", Bean.class).item);
        assertNull(JSON.parseObject("{\"item\":null}", Bean.class).item);
    }

    public static class Bean {
        public Item item;
    }

    public static class Item {
        public int id;
    }
}
