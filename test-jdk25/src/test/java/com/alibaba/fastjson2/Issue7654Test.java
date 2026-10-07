package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * On JDK 25 the compiler emits {@code Objects.requireNonNull(this$0)} at the top of every
 * non-static inner class constructor. Deserializing such a class therefore requires a non-null
 * enclosing instance to be supplied to the constructor; passing {@code null} (as fastjson2 did
 * previously) makes the constructor throw {@link NullPointerException}. This module is compiled
 * with {@code maven.compiler.source/target=25}, so the inner class constructor below carries the
 * {@code requireNonNull} check and reproduces issue #7654 directly.
 */
public class Issue7654Test {
    @Test
    public void testInnerClass() {
        Outer.Inner bean = JSON.parseObject("{\"id\":123}", Outer.Inner.class);
        assertNotNull(bean);
        assertEquals(123, bean.id);
    }

    public static class Outer {
        public class Inner {
            public int id;
        }
    }
}
