package com.alibaba.fastjson2.benchmark.wast;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wycst.wast.json.JSON;
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

/**
 * @author wangy
 * 2022/1/15 23:17
 */
public class SimpleBeanWriteCase {
    private static String result;
    static ObjectMapper mapper = new ObjectMapper();
    private static Object simpleBean;

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

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

        result = JSON.toJsonString(simpleMap, WriteOption.FormatOut);
        simpleBean = JSON.parseObject(result, SimpleBean.class);
        System.out.println(com.alibaba.fastjson2.JSON.toJSONString(simpleBean));
        System.out.println(JSON.toJsonString(simpleBean));
        try {
            System.out.println(mapper.writeValueAsString(simpleBean));
        } catch (JsonProcessingException e) {
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.toJSONString(simpleBean));
    }

    @Benchmark
    public void wastjson(Blackhole bh) {
        bh.consume(JSON.toJsonString(simpleBean));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.writeValueAsString(simpleBean));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(SimpleBeanWriteCase.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
