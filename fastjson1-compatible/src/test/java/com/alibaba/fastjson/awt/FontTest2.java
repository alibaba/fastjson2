package com.alibaba.fastjson.awt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;

public class FontTest2 {
    @Test
    public void test_color() throws Exception {
        Font[] fonts = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (Font font : fonts) {
            String text = JSON.toJSONString(font, SerializerFeature.WriteClassName);

            Font font2 = (Font) JSON.parse(text, Feature.SupportAutoType);

            Assertions.assertEquals(font, font2);
        }
    }
}
