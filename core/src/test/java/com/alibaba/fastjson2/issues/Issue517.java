package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue517 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"values\":[101]}", Bean.class);
        assertEquals(1, bean.values.size());
        assertEquals(101, bean.values.get(0));
    }

    public static class Bean {
        public Vector values;
    }
}
