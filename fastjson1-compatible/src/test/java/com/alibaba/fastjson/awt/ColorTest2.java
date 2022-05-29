package com.alibaba.fastjson.awt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;

public class ColorTest2 {
    @Test
    public void test_color() throws Exception {
        Color color = Color.RED;
        String text = JSON.toJSONString(color, SerializerFeature.WriteClassName);
        System.out.println(text);

        Color color2 = (Color) JSON.parse(text, Feature.SupportAutoType);
        Assertions.assertEquals(color, color2);

        Color color3 = (Color) JSON.parse(text, Feature.SupportAutoType);
        Assertions.assertEquals(color, color3);

        Color color4 = (Color) JSON.parseObject(text, Color.class, Feature.SupportAutoType);
        Assertions.assertEquals(color, color4);
    }
}
