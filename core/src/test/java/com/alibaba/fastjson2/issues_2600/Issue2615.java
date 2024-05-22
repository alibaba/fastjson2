package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2615 {
    @Test
    public void test() {
        AtomicLong atomicLong = JSON.parseObject("8924992445", AtomicLong.class);
        assertEquals(8924992445L, atomicLong.get());
    }
}
