package com.alibaba.fastjson2_vo;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicIntegerArrayReadOnly1 {
    private AtomicIntegerArray value = new AtomicIntegerArray(10);

    public AtomicIntegerArray getValue() {
        return value;
    }
}
