package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONTypeAutoTypeCheckHandlerTest {
    @Test
    public void test_for_checkAutoType() {
        Cat cat = (Cat) JSON.parseObject("{\"@type\":\"Cat\",\"catId\":123}", Animal.class);
        assertEquals(123, cat.catId);
    }

    @JSONType(autoTypeBeforeHandler = MyAutoTypeCheckHandler.class)
    public static class Animal {
    }

    public static class Cat
            extends Animal {
        public int catId;
    }

    public static class Mouse
            extends Animal {
    }

    public static class MyAutoTypeCheckHandler
            implements JSONReader.AutoTypeBeforeHandler {
        @Override
        public Class<?> apply(String typeName, Class<?> expectClass, long features) {
            if ("Cat".equals(typeName)) {
                return Cat.class;
            }

            if ("Mouse".equals(typeName)) {
                return Mouse.class;
            }

            return null;
        }
    }
}
