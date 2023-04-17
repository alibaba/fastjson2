package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.RyuDouble;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class DoubleToString {
    static double d = 0.6730586701548151D;
    static float f = 0.98163563F;

    @Benchmark
    public void jdk(Blackhole bh) throws Throwable {
        bh.consume(Double.toString(d));
    }

    @Benchmark
    public void ryu(Blackhole bh) throws Throwable {
        bh.consume(RyuDouble.toString(d));
    }

    @Benchmark
    public void jdkFloat(Blackhole bh) throws Throwable {
        bh.consume(Float.toString(f));
    }

    @Benchmark
    public void ryuFloat(Blackhole bh) throws Throwable {
        bh.consume(RyuDouble.toString(f));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DoubleToString.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
