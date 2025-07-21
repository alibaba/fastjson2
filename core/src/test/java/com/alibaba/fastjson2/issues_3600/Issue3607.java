package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3607 {
    @Test
    public void test() {
        String str = "NaN";
        assertEquals(Double.NaN,
                JSON.parseObject(str, double.class));
        assertEquals(Double.NaN,
                JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), double.class));
        assertEquals(Double.NaN,
                JSON.parseObject(str.toCharArray(), double.class));

        assertEquals(Float.NaN,
                JSON.parseObject(str, float.class));
        assertEquals(Float.NaN,
                JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), float.class));
        assertEquals(Float.NaN,
                JSON.parseObject(str.toCharArray(), float.class));
    }
}
