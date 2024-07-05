package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.alibaba.fastjson2.JSONReader.Feature.SupportAutoType;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2608 {
    @Test
    public void testLong() {
        String str = "[300000000L]";
        {
            List list = JSON.parseObject(str, List.class, SupportAutoType);
            assertEquals(Long.class, list.get(0).getClass());
        }
        {
            List list = JSON.parseObject(str.toCharArray(), List.class, SupportAutoType);
            assertEquals(Long.class, list.get(0).getClass());
        }
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        {
            List list = JSON.parseObject(utf8, List.class, SupportAutoType);
            assertEquals(Long.class, list.get(0).getClass());
        }
        {
            List list = JSON.parseObject(
                    utf8, 0, utf8.length, StandardCharsets.UTF_8, List.class, SupportAutoType);
            assertEquals(Long.class, list.get(0).getClass());
        }
        {
            List list = JSON.parseObject(
                    utf8, 0, utf8.length, StandardCharsets.US_ASCII, List.class, SupportAutoType);
            assertEquals(Long.class, list.get(0).getClass());
        }
    }
}
