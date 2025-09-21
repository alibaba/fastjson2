package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3771 {
    @Test
    public void test() {
        byte[] bytes = JSONB.toBytes(new AtomicLong(12345L), JSONWriter.Feature.WriteClassName);
        AtomicLong result = JSONB.parseObject(bytes, AtomicLong.class);
        assertEquals(12345, result.get());

        byte[] bytes2 = JSONB.toBytes(new AtomicInteger(12345), JSONWriter.Feature.WriteClassName);
        AtomicInteger result2 = JSONB.parseObject(bytes2, AtomicInteger.class);
        assertEquals(12345, result2.get());
    }
}
