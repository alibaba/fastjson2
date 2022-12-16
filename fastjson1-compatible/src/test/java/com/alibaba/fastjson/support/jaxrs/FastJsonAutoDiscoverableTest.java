package com.alibaba.fastjson.support.jaxrs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FastJsonAutoDiscoverableTest {
    @Test
    public void test() {
        FastJsonAutoDiscoverable obj = new FastJsonAutoDiscoverable();
        assertThrows(Exception.class, () -> obj.configure(null));
    }
}
