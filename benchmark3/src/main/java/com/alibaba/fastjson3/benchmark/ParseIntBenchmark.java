package com.alibaba.fastjson3.benchmark;

import com.alibaba.fastjson3.benchmark.bean.Int10Bean;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark: parsing JSON with many integer fields.
 * Tests fast number parsing optimization (wast-style direct digit parsing).
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 3)
@Measurement(iterations = 3, time = 3)
@Fork(1)
@State(Scope.Benchmark)
public class ParseIntBenchmark {
    static final String JSON_STR =
            "{\"a\":12345,\"b\":-67890,\"c\":2147483647,\"d\":-2147483648,"
                    + "\"e\":0,\"f\":1,\"g\":999999,\"h\":100000,\"i\":-1,\"j\":42}";

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(JSON_STR, Int10Bean.class));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(JSON_STR, Int10Bean.class));
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(JSON_STR, Int10Bean.class));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ParseIntBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
