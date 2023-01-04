package com.alibaba.fastjson2.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ContextAutoTypeBeforeHandlerTest {
    @Test
    public void checkAutoType() {
        ContextAutoTypeBeforeHandler filter = new ContextAutoTypeBeforeHandler(new String[]{
                "",
                ContextAutoTypeBeforeHandlerTest.class.getName() + ".",
                null
        });
        assertNull(filter.apply("Integer", Integer.class, 0));
        assertEquals(Bean.class, filter.apply(Bean.class.getName(), Bean.class, 0));
        assertEquals(Bean.class, filter.apply(Bean.class.getName(), Bean.class, 0));
    }

    public static class Bean {
    }

    @Test
    public void test0() {
        ContextAutoTypeBeforeHandler filter = new ContextAutoTypeBeforeHandler(
                new String[]{
                        "int",
                        "java.lang.String"
                }
        );

        assertEquals(int.class, filter.apply("int", null, 0));
        assertEquals(String.class, filter.apply("java.lang.String", null, 0));
    }
}
