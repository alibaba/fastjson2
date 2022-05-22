package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LCaseTest {
    @Test
    public void test_0() throws Exception {
        String str = "{\"optimal_height\":400}";
        Image image = JSON.parseObject(str, Image.class);
        assertEquals(0, image.optimalHeight);
    }

    @Test
    public void test_1() throws Exception {
        String str = "{\"optimal_height\":400}";
        Image image = JSON.parseObject(str, Image.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(400, image.optimalHeight);
    }

    public static class Image {
        public int optimalHeight;
    }
}
