package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2309 {
    @Test
    public void test() {
        String str = "{\"value\":NaN}";
        assertEquals(Double.NaN, JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean.class).value);
        assertEquals(Double.NaN, JSON.parseObject(str.toCharArray(), Bean.class).value);
        assertEquals(Double.NaN, JSON.parseObject(str, Bean.class).value);
    }

    public static class Bean {
        public Double value;
    }

    @Test
    public void testFloat() {
        String str = "{\"value\":NaN}";
        assertEquals(Float.NaN, JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean1.class).value);
        assertEquals(Float.NaN, JSON.parseObject(str.toCharArray(), Bean1.class).value);
        assertEquals(Float.NaN, JSON.parseObject(str, Bean1.class).value);
    }

    public static class Bean1 {
        public Float value;
    }
}
