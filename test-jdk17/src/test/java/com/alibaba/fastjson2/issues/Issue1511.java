package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1511 {
    @Test
    public void test1() {
        R1 r = new R1(true);
        String str = JSON.toJSONString(r);
        assertEquals("{\"isActivation\":true}", str);
    }

    @Test
    public void test2() {
        R2 r = new R2(true);
        String str = JSON.toJSONString(r);
        assertEquals("{\"activation\":true}", str);
    }

    private record R1(boolean isActivation) {
    }

    private record R2(boolean activation) {
    }
}
