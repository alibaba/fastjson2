package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class StringFormatBenchmark {
    static int value = 10245;
    static final String PREFIX = "id : ";
    static final char[] PREFIX_CHARS = PREFIX.toCharArray();
    static final byte[] PREFIX_BYTES = PREFIX.getBytes();

    @Benchmark
    public void format(Blackhole bh) throws Exception {
        bh.consume(
                String.format("id : %s", value)
        );
    }

    @Benchmark
    public void StringBuffer(Blackhole bh) {
        bh.consume(
                new StringBuilder().append(PREFIX).append(value).toString()
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(StringFormatBenchmark.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
