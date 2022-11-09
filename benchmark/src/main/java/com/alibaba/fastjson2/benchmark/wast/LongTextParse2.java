package com.alibaba.fastjson2.benchmark.wast;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author wangy
 * date 2022/6/3 8:45
 */
public class LongTextParse2 {
    static final ObjectMapper mapper = new ObjectMapper();

    private static String simpleResult;

    static {
        int length = 10000;
        StringBuffer value = new StringBuffer();
        for (int i = 0; i < length; i++) {
            value.append("a");
        }
        Map map = new HashMap();
        map.put("a", value);
        map.put("b", value);
        map.put("c", value);
        map.put("d", value);
        map.put("e", value);
        map.put("f", value);
        map.put("g", value);
        map.put("h", value);
        map.put("i", value);
        map.put("j", value);
        map.put("k", value);
        map.put("l", value);
        map.put("m", value);
        map.put("n", value);
        map.put("o", value);
        map.put("p", value);
        map.put("q", value);
        map.put("r", value);

        simpleResult = io.github.wycst.wast.json.JSON.toJsonString(map);
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(simpleResult, Map.class));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(simpleResult, Map.class));
    }

    @Benchmark
    public void wastjson(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(simpleResult, Map.class));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(LongTextParse2.class.getName())
                .exclude(LongTextParse2Escape.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
