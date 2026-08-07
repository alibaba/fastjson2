package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UUIDUtilsBenchmark {
    public static UUID uuid = UUID.randomUUID();

    @Benchmark
    public void jdk(Blackhole bh) {
        bh.consume(uuid.toString());
    }

    @Benchmark
    public void fj2(Blackhole bh) {
        bh.consume(UUIDUtils.fastUUID(uuid));
    }

    @Benchmark
    public void fj2utf16(Blackhole bh) {
        bh.consume(UUIDUtils.fastUUID2(uuid));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(UUIDUtilsBenchmark.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(1)
                .build();
        new Runner(options).run();
    }
}
