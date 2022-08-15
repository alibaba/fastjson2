package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class ApacheLang3SupportTest {
    @Test
    public void test() {
        assertNull(ApacheLang3Support.TripleMixIn.of(null, null, null));
    }
}
