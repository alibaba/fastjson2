package com.alibaba.fastjson2.android;

import org.junit.Test;

import static org.junit.Assert.*;

public class UnsafeTest {

    @Test
    public void test() throws ClassNotFoundException {
        Class<?> c = Class.forName("sun.misc.Unsafe");
        assertEquals("sun.misc.Unsafe", c.getCanonicalName());
    }

}
