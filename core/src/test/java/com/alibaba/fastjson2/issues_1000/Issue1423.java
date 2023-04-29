package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1423 {
    @Test
    public void test() {
        String str = "{\"value\":\"2023-04-27T16:00:00.000Z\"}";
        Bean bean = JSON.parseObject(str, Bean.class);
        assertEquals(1682611200000L, bean.value);
    }

    public static class Bean {
        public long value;
    }
}
