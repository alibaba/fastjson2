package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("util")
public class ApacheLang3SupportTest {
    @Test
    public void test() {
        assertNull(ApacheLang3Support.TripleMixIn.of(null, null, null));
    }
}
