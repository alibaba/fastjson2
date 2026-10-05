package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FactoryPropertiesReviewTest {
    @Test
    public void propertyFileFallback() throws Exception {
        String name = "fastjson2.review.propertyFileFallback";
        Properties properties = new Properties();
        Method bool = JSONFactory.class.getDeclaredMethod("getPropertyBool", Properties.class, String.class, boolean.class);
        bool.setAccessible(true);
        properties.setProperty(name, " false ");
        assertEquals(false, bool.invoke(null, properties, name, true));
        properties.setProperty(name, " true ");
        assertEquals(true, bool.invoke(null, properties, name, false));

        Method integer = JSONFactory.class.getDeclaredMethod("getPropertyInt", Properties.class, String.class, int.class);
        integer.setAccessible(true);
        properties.setProperty(name, " 128 ");
        assertEquals(128, integer.invoke(null, properties, name, 2048));
        properties.setProperty(name, "invalid");
        assertEquals(2048, integer.invoke(null, properties, name, 2048));
    }
}
