package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

public class JDKUtilsTest {
    @Test
    public void test() {
        try {
            JDKUtils.getStringCreatorJDK11();
        } catch (Throwable e) {
            // skip
        }
    }
}
