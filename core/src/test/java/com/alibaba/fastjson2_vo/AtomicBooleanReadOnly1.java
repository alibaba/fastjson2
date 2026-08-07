package com.alibaba.fastjson2_vo;

import java.util.concurrent.atomic.AtomicBoolean;

public class AtomicBooleanReadOnly1 {
    private AtomicBoolean value = new AtomicBoolean();

    public AtomicBoolean getValue() {
        return value;
    }
}
