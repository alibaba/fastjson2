package com.alibaba.fastjson2.benchmark;

import java.util.Random;

public class DoubleGen {
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

    public static void main(String[] args) throws Exception {
        DoubleGen gen = new DoubleGen();
        gen.gen();
    }
}
