package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;

public class DecimalToString {
    static final long unscaledVal = 123456;
    static int scale = 2;
    static BigDecimal decimal = BigDecimal.valueOf(unscaledVal, scale);

    @Benchmark
    public void toPlainString(Blackhole bh) {
        bh.consume(DecimalUtils.toString(unscaledVal, scale));
    }

    @Benchmark
    public void toPlainStringDec(Blackhole bh) {
        bh.consume(decimal.toPlainString());
    }

    public void toStringCharWithInt8(Blackhole bh) {
        StringBuilder result = new StringBuilder();
        result.append(2048);
        result.append(31337);
        result.append(0xbeefcace);
        result.append(9000);
        result.append(4711);
        result.append(1337);
        result.append(2100);
        result.append(2600);
        bh.consume(result.toString());
    }

    public void toStringCharWithInt8UTF16(Blackhole bh) {
        StringBuilder result = new StringBuilder();
        result.append('\u4e2d');
        result.append(2048);
        result.append(31337);
        result.append(0xbeefcace);
        result.append(9000);
        result.append(4711);
        result.append(1337);
        result.append(2100);
        result.append(2600);
        bh.consume(result.toString());
    }
}
