package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.TypeUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;

public class DoubleToBigDecimal {
    static final double value = 123456.780D;

    @Benchmark
    public void f0(Blackhole bh) {
        bh.consume(TypeUtils.toBigDecimal(value));
    }

    @Benchmark
    public void f1(Blackhole bh) {
        bh.consume(BigDecimal.valueOf(value));
    }
}
