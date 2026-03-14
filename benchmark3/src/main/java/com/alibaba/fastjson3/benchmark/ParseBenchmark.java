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
 * Benchmark: JSON string → Java Bean deserialization.
 * Compares fastjson2, fastjson3, and wast.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 3)
@Measurement(iterations = 3, time = 3)
@Fork(1)
@State(Scope.Benchmark)
public class ParseBenchmark {
    static final String JSON_STR;

    static {
        SimpleBean bean = new SimpleBean();
        bean.setId(1001);
        bean.setName("benchmark-test-user");
        bean.setVersion(999999999L);
        bean.setPercent(3.1415926);
        bean.setActive(true);
        bean.setScores(java.util.List.of(95, 87, 92, 100, 78));
        bean.setExtra(java.util.Map.of("key1", "value1", "key2", 42, "key3", true));
        JSON_STR = com.alibaba.fastjson2.JSON.toJSONString(bean);
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(JSON_STR, SimpleBean.class));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(JSON_STR, SimpleBean.class));
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(JSON_STR, SimpleBean.class));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ParseBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
