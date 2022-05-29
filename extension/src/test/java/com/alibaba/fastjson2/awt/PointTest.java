package com.alibaba.fastjson2.awt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.support.AwtRederModule;
import com.alibaba.fastjson2.support.AwtWriterModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PointTest {
    @BeforeEach
    public void setUp() {
        JSONFactory.getDefaultObjectWriterProvider().register(AwtWriterModule.INSTANCE);
        JSONFactory.getDefaultObjectReaderProvider().register(AwtRederModule.INSTANCE);
    }

    @Test
    public void test0() {
        Point point = new Point(3, 4);
        String text = JSON.toJSONString(point);
        Point point2 = JSON.parseObject(text, Point.class);

        assertEquals(point, point2);
    }
}
