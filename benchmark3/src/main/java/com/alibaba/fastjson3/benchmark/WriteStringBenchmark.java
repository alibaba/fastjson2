package com.alibaba.fastjson3.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark: Serializing maps with large string values.
 * Tests string writing and escape handling performance.
 * Inspired by wast's WriteCase benchmark.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 3)
@Measurement(iterations = 3, time = 3)
@Fork(1)
@State(Scope.Benchmark)
public class WriteStringBenchmark {
    static final Map<String, Object> MAP;
    static final Map<String, Object> ESCAPE_MAP;

    static {
        // Normal: 14 keys with 200-char string values
        MAP = new LinkedHashMap<>();
        String val = "a".repeat(200);
        for (int i = 0; i < 14; i++) {
            MAP.put("field" + (char) ('a' + i), val);
        }

        // Escape: same but with a newline at position 100
        ESCAPE_MAP = new LinkedHashMap<>();
        String escVal = "a".repeat(100) + "\n" + "a".repeat(99);
        for (int i = 0; i < 14; i++) {
            ESCAPE_MAP.put("field" + (char) ('a' + i), escVal);
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.toJSONString(MAP));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.toJSONString(MAP));
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.toJsonString(MAP));
    }

    @Benchmark
    public void fastjson2_escape(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.toJSONString(ESCAPE_MAP));
    }

    @Benchmark
    public void fastjson3_escape(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.toJSONString(ESCAPE_MAP));
    }

    @Benchmark
    public void wast_escape(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.toJsonString(ESCAPE_MAP));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WriteStringBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
