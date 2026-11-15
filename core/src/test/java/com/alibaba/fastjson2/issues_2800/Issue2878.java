package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("regression")
public class Issue2878 {
    @Test
    public void test() {
        Demo demo = JSON.parseObject("{\"isAsc\":true}", Demo.class);
        assertTrue(demo.isAsc());
        assertTrue(JSON.parseObject("{\"asc\":true}", Demo.class).isAsc());
        assertEquals("{\"asc\":true}", JSON.toJSONString(demo));
    }

    @Data
    public static class Demo {
        private boolean isAsc;
    }
}
