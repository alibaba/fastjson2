package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.util.IOUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringTest1 {
    @Test
    public void test_0() throws Throwable {
        String str = "×中";
        byte[] bytes = new byte[]{-41, 0, 45, 78};

        byte[] dst = new byte[10];
        int result = IOUtils.encodeUTF8(bytes, 0, bytes.length, dst, 0);
        String str2 = new String(dst, 0, result, StandardCharsets.UTF_8);
        assertEquals(str, str2);
    }
}
