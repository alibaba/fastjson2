package com.alibaba.fastjson3.benchmark;

import com.alibaba.fastjson3.benchmark.bean.SimpleBean;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark: Java Bean → JSON string serialization.
 * Compares fastjson2, fastjson3, and wast.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 3)
@Measurement(iterations = 3, time = 3)
@Fork(1)
@State(Scope.Benchmark)
public class WriteBenchmark {
    static final SimpleBean BEAN;

    static {
        BEAN = new SimpleBean();
        BEAN.setId(1001);
        BEAN.setName("benchmark-test-user");
        BEAN.setVersion(999999999L);
        BEAN.setPercent(3.1415926);
        BEAN.setActive(true);
        BEAN.setScores(java.util.List.of(95, 87, 92, 100, 78));
        BEAN.setExtra(java.util.Map.of("key1", "value1", "key2", 42, "key3", true));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.toJSONString(BEAN));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.toJSONString(BEAN));
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.toJsonString(BEAN));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WriteBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
