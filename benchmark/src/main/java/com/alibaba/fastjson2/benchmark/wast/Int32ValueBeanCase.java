package com.alibaba.fastjson2.benchmark.wast;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class Int32ValueBeanCase {
    static String result = "{\"a\":269072307,\"b\":-1518251888,\"c\":1718732377,\"d\":-680809737,\"e\":366417267,\"f\":-385913708,\"h\":-1059329988,\"i\":-1143820253,\"j\":1981317426,\"k\":141784471}";
    static ObjectMapper mapper = new ObjectMapper();

    @Benchmark
    public void jackson(Blackhole bh) throws JsonProcessingException {
        bh.consume(mapper.readValue(result, Int32ValueBean.class));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.parseObject(result, Int32ValueBean.class));
    }

    @Benchmark
    public void wastjson(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(result, Int32ValueBean.class));
    }

    public static void main(String[] args) throws RunnerException {
//        io.github.wycst.wast.json.JSON.parseObject(result, NumberValueBean.class);
        Options options = new OptionsBuilder()
                .include(Int32ValueBeanCase.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
