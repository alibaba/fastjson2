package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.TypeUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class DoubleToBigDecimal {
    static final double value = 0.6730586701548151D;

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(TypeUtils.toBigDecimal(value));
    }

    @Benchmark
    public void jdk(Blackhole bh) {
        bh.consume(BigDecimal.valueOf(value));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DoubleToBigDecimal.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
