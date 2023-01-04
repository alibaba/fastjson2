package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1025 {
    @Test
    public void test2000() throws Exception {
        byte[] bytes = new byte[8000];
        Arrays.fill(bytes, (byte) 'A');
        for (int i = 0; i < 2000; i++) {
            bytes[i] = 0x01;
        }

        String str = new String(bytes, "UTF-8");
        String json = JSON.toJSONString(str);
        assertEquals(str, JSON.parse(json));

        byte[] jsonBytes = JSON.toJSONBytes(str);
        assertEquals(str, JSON.parse(jsonBytes));
    }

    @Test
    public void test4000() throws Exception {
        byte[] bytes = new byte[8000];
        Arrays.fill(bytes, (byte) 'A');
        for (int i = 1000; i < 5000; i++) {
            bytes[i] = 0x01;
        }

        String str = new String(bytes, "UTF-8");
        String json = JSON.toJSONString(str);
        assertEquals(str, JSON.parse(json));

        byte[] jsonBytes = JSON.toJSONBytes(str);
        assertEquals(str, JSON.parse(jsonBytes));
    }
}
