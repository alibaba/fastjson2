package com.alibaba.fastjson2_vo;

import java.util.concurrent.atomic.AtomicLongArray;

public class AtomicLongArrayReadOnly1 {
    private AtomicLongArray value = new AtomicLongArray(10);

    public AtomicLongArray getValue() {
        return value;
    }
}
