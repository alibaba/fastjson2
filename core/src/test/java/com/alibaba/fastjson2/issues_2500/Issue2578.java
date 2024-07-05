package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2578 {
    @Test
    public void test() {
        Color blue = Color.BLUE;
        String colorStr = JSON.toJSONString(blue);
        assertEquals("{\"r\":0,\"g\":0,\"b\":255,\"alpha\":255}", colorStr);
        Color color = JSON.parseObject(colorStr, Color.class);
        assertEquals(blue, color);
    }

    @Test
    public void test1() {
        Color[] colors = {
                Color.BLUE,
                Color.RED,
                Color.GREEN,
                Color.YELLOW,
                Color.ORANGE,
                Color.PINK,
                Color.CYAN,
                Color.MAGENTA
        };
        for (Color color : colors) {
            String colorStr = JSON.toJSONString(color);
            Color color1 = JSON.parseObject(colorStr, Color.class);
            assertEquals(color, color1);
        }
    }
}
