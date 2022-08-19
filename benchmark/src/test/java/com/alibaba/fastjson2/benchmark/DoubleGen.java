package com.alibaba.fastjson2.benchmark;

import org.junit.jupiter.api.Test;

import java.util.Random;

public class DoubleGen {
    @Test
    public void gen() {
        Random r = new Random();
        StringBuffer buf = new StringBuffer();
        buf.append('[');
        for (int i = 0; i < 50; i++) {
            if (i != 0) {
                buf.append(',');
            }
            double d = r.nextDouble();
            buf.append(d);
        }
        buf.append(']');
        System.out.println(buf);
    }
}
