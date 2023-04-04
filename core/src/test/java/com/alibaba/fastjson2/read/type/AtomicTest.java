package com.alibaba.fastjson2.read.type;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class AtomicTest {
    @Test
    public void testAtomicBoolean() {
        assertTrue(JSON.parseObject("1", AtomicBoolean.class).get());
        assertTrue(JSON.parseObject("true", AtomicBoolean.class).get());

        assertFalse(JSON.parseObject("0", AtomicBoolean.class).get());
        assertFalse(JSON.parseObject("false", AtomicBoolean.class).get());
    }

    @Test
    public void testAtomicInteger() {
        assertEquals(123, JSON.parseObject("123", AtomicInteger.class).intValue());
        assertEquals(1, JSON.parseObject("true", AtomicInteger.class).intValue());
        assertNull(JSON.parseObject("null", AtomicInteger.class));
    }

    @Test
    public void testAtomicLong() {
        assertEquals(123L, JSON.parseObject("123", AtomicLong.class).longValue());
        assertEquals(1L, JSON.parseObject("true", AtomicLong.class).longValue());
        assertNull(JSON.parseObject("null", AtomicLong.class));
    }
}
