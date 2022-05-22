package com.alibaba.fastjson.awt;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;

public class FontTest {
    @Test
    public void test_color() throws Exception {
        Font[] fonts = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (Font font : fonts) {
            String text = JSON.toJSONString(font);

            Font font2 = JSON.parseObject(text, Font.class);

            Assertions.assertEquals(font, font2);
        }
    }
}
