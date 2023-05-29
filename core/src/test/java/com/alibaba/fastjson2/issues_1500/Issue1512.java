package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1512 {
    @Test
    public void test() {
        String str = "{\"id\":101,\"value\":123L}";
        assertEquals(101, JSON.parseObject(str, Bean.class).id);
        assertEquals(101, JSON.parseObject(str.toCharArray(), Bean.class).id);

        byte[] bytes = str.getBytes();
        assertEquals(
                101,
                JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.UTF_8, Bean.class).id
        );
        assertEquals(
                101,
                JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.US_ASCII, Bean.class).id
        );
    }

    @Test
    public void test2() {
        String str = "{\"id\":101,\"value\":Set[]}";
        assertEquals(101, JSON.parseObject(str, Bean.class).id);
        assertEquals(101, JSON.parseObject(str.toCharArray(), Bean.class).id);

        byte[] bytes = str.getBytes();
        assertEquals(
                101,
                JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.UTF_8, Bean.class).id
        );
        assertEquals(
                101,
                JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.US_ASCII, Bean.class).id
        );
    }

    public static class Bean {
        public int id;
    }
}
