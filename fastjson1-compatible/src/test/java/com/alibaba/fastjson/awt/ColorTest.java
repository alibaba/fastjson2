package com.alibaba.fastjson.awt;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;

public class ColorTest {
    @Test
    public void test_color() throws Exception {
        Color color = Color.RED;
        String text = JSON.toJSONString(color);
        Color color2 = JSON.parseObject(text, Color.class);
        Assertions.assertEquals(color, color2);
    }

    @Test
    public void test_color_2() throws Exception {
        Color color = Color.RED;
        String text = "{\"r\":" + color.getRed() + ",\"g\":" + color.getGreen() + ",\"b\":" + color.getBlue() + "}";
        Color color2 = JSON.parseObject(text, Color.class);
        Assertions.assertEquals(color, color2);
    }
}
