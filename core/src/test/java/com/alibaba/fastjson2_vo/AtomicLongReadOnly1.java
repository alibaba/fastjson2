package com.alibaba.fastjson2_vo;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicLongReadOnly1 {
    private AtomicLong value = new AtomicLong();

    public AtomicLong getValue() {
        return value;
    }
}
