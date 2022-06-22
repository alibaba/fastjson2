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
}
