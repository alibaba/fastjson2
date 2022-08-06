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

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SimpleBeanCase {
    private static String result;
    static ObjectMapper mapper = new ObjectMapper();

    static {
        Map simpleMap = new HashMap();
        simpleMap.put("id", 1);
        simpleMap.put("date", new Date());
        simpleMap.put("name", "simple");
        simpleMap.put("percent", 12.34);
        simpleMap.put("version", System.currentTimeMillis());

        Map mapType = new HashMap();
        mapType.put("v1", "v1 helldsdsd ");
        mapType.put("v2", "v2 helldsdsd ");
        simpleMap.put("mapInstance", mapType);

        List<Object> versions = new ArrayList<Object>();
        versions.add("v0.0.1");
        versions.add("v0.0.2");
        versions.add("v0.0.3");
        simpleMap.put("versions", versions);

        result = io.github.wycst.wast.json.JSON.toJsonString(simpleMap, WriteOption.FormatOut);
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parseObject(result, SimpleBean.class));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(result, SimpleBean.class));
    }

    @Benchmark
    public void wastjson(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(result, SimpleBean.class));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(result, SimpleBean.class));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(SimpleBeanCase.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
