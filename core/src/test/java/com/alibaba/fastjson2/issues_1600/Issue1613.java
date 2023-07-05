package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1613 {
    @Test
    public void test() {
        Bean bean = new Bean();
        assertEquals("{}", JSON.toJSONString(bean));
    }

    public static class Bean {
        public void getId() {
        }
    }
}
