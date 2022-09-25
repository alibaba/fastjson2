package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.util.JDKUtils.LATIN1;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_CREATOR_JDK11;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JDKUtilsTest {
    @Test
    public void test17() {
        if (STRING_CREATOR_JDK11 == null) {
            return;
        }

        String str = "abc";
        assertEquals(str, STRING_CREATOR_JDK11.apply(str.getBytes(), LATIN1));
    }
}
