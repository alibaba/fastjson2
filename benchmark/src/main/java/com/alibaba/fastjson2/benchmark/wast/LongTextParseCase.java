package com.alibaba.fastjson2.benchmark.wast;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wycst.wast.json.options.WriteOption;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LongTextParseCase {
    static ObjectMapper mapper = new ObjectMapper();

    private static String simpleResult;
    private static String simplePrettyResult;
    private static String escapeResult;
    private static String escapePrettyResult;

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
        System.out.println("数据大小：" + (simpleResult.getBytes(StandardCharsets.UTF_8).length >> 10));
        simplePrettyResult = io.github.wycst.wast.json.JSON.toJsonString(map, WriteOption.FormatOut);

        // 随机添加转义字符
        for (int j = 1; j < 32; j++) {
            int index = ((int) (Math.random() * 10000)) % length;
            value.setCharAt(index, (char) j);
        }
        escapeResult = io.github.wycst.wast.json.JSON.toJsonString(map);
        escapePrettyResult = io.github.wycst.wast.json.JSON.toJsonString(map, WriteOption.FormatOut);

        Map map1 = com.alibaba.fastjson2.JSON.parseObject(escapeResult, LinkedHashMap.class);
        Map map2 = io.github.wycst.wast.json.JSON.parseObject(escapeResult, LinkedHashMap.class);
        Map map3 = com.alibaba.fastjson2.JSON.parseObject(escapePrettyResult, LinkedHashMap.class);
        Map map4 = io.github.wycst.wast.json.JSON.parseObject(escapePrettyResult, LinkedHashMap.class);
        System.out.println(com.alibaba.fastjson2.JSON.toJSONString(map1).equals(com.alibaba.fastjson2.JSON.toJSONString(map2)));
        System.out.println(com.alibaba.fastjson2.JSON.toJSONString(map3).equals(com.alibaba.fastjson2.JSON.toJSONString(map4)));
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parseObject(simpleResult, Map.class));
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

    @Benchmark
    public void prettyFastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parseObject(simplePrettyResult, Map.class));
    }

    @Benchmark
    public void prettyFastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(simplePrettyResult, Map.class));
    }

    @Benchmark
    public void prettyJackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(simplePrettyResult, Map.class));
    }

    @Benchmark
    public void prettyWastjson(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(simplePrettyResult, Map.class));
    }

    @Benchmark
    public void escapeFastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parseObject(escapeResult, Map.class));
    }

    @Benchmark
    public void escapeFastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(escapeResult, Map.class));
    }

    @Benchmark
    public void escapeJackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(escapeResult, Map.class));
    }

    @Benchmark
    public void escapeWastjson(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(escapeResult, Map.class));
    }

    @Benchmark
    public void escapePrettyFastjson(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parseObject(escapePrettyResult, Map.class));
    }

    @Benchmark
    public void escapePrettyFastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(escapePrettyResult, Map.class));
    }

    @Benchmark
    public void escapePrettyJackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(escapePrettyResult, Map.class));
    }

    @Benchmark
    public void escapePrettyWastjson(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(escapePrettyResult, Map.class));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(LongTextParseCase.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
