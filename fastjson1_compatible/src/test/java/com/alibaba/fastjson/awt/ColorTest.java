package com.alibaba.fastjson.awt;

import com.alibaba.fastjson.JSON;
import junit.framework.TestCase;
import org.junit.Assert;

import java.awt.*;


public class ColorTest extends TestCase {
    public void test_color() throws Exception {
        Color color = Color.RED;
        String text = JSON.toJSONString(color);
        Color color2 = JSON.parseObject(text, Color.class);
        Assert.assertEquals(color, color2);
    }

    public void test_color_2() throws Exception {
        Color color = Color.RED;
        String text = "{\"r\":" + color.getRed() + ",\"g\":" + color.getGreen() + ",\"b\":" + color.getBlue() +"}";
        Color color2 = JSON.parseObject(text, Color.class);
        Assert.assertEquals(color, color2);
    }
}
