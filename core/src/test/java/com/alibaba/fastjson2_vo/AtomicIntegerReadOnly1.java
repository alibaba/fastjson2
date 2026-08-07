package com.alibaba.fastjson2_vo;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerReadOnly1 {
    private AtomicInteger value = new AtomicInteger();

    public AtomicInteger getValue() {
        return value;
    }
}
