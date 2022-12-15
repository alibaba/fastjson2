package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.util.JDKUtils.LATIN1;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_CREATOR_JDK11;
import static org.junit.jupiter.api.Assertions.assertSame;

public class JDKUtilsTest {
    @Test
    public void test17() {
        if (STRING_CREATOR_JDK11 == null) {
            return;
        }

        String str = "abc";
        byte[] value = JDKUtils.STRING_VALUE.apply(str);

        String str1 = STRING_CREATOR_JDK11.apply(value, LATIN1);
        byte[] value1 = JDKUtils.STRING_VALUE.apply(str1);

        assertSame(value, value1);
    }
}
