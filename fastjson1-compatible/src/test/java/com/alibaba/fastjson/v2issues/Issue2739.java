package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2739 {
    @Test
    public void test() {
        Color blue = Color.BLUE;
        String colorStr = JSON.toJSONString(blue);
        assertEquals("{\"r\":0,\"g\":0,\"b\":255,\"alpha\":255}", colorStr);
        Color color = JSON.parseObject(colorStr, Color.class);
        assertEquals(blue, color);
    }
}
