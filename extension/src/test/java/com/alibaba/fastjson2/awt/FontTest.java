package com.alibaba.fastjson2.awt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.support.AwtRederModule;
import com.alibaba.fastjson2.support.AwtWriterModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FontTest {
    @BeforeEach
    public void setUp() {
        JSONFactory.getDefaultObjectWriterProvider().register(AwtWriterModule.INSTANCE);
        JSONFactory.getDefaultObjectReaderProvider().register(AwtRederModule.INSTANCE);
    }

    @Test
    public void test_color() {
        Font[] fonts = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (Font font : fonts) {
            String text = JSON.toJSONString(font);

            Font font2 = JSON.parseObject(text, Font.class);

            assertEquals(font, font2);
        }
    }
}
