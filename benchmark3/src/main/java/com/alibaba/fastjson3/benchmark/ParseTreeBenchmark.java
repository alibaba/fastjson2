package com.alibaba.fastjson3.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark: JSON string → tree model (JSONObject/Map) parsing.
 * Tests raw parsing speed without POJO binding overhead.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 3)
@Measurement(iterations = 3, time = 3)
@Fork(1)
@State(Scope.Benchmark)
public class ParseTreeBenchmark {
    static final String JSON_STR = """
            {
                "users": [
                    {"id": 1, "name": "Alice", "email": "alice@test.com", "age": 30, "active": true},
                    {"id": 2, "name": "Bob", "email": "bob@test.com", "age": 25, "active": false},
                    {"id": 3, "name": "Charlie", "email": "charlie@test.com", "age": 35, "active": true}
                ],
                "total": 3,
                "page": 1,
                "pageSize": 20,
                "hasMore": false
            }
            """;

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(JSON_STR));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(JSON_STR));
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parse(JSON_STR));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ParseTreeBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
