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

public class WriteCase {
    private static Map map1;
    private static Map escapeMap;
    static ObjectMapper mapper = new ObjectMapper();

    static {
        int len = 200;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < len; i++) {
            buffer.append("a");
        }
        Map map = new HashMap();
        map.put("abcdef", buffer.toString());
        map.put("bbcdef", buffer.toString());
        map.put("cbcdef", buffer.toString());
        map.put("dbcdef", buffer.toString());
        map.put("ebcdef", buffer.toString());
        map.put("fbcdef", buffer.toString());
        map.put("gbcdef", buffer.toString());
        map.put("hbcdef", buffer.toString());
        map.put("ibcdef", buffer.toString());
        map.put("jbcdef", buffer.toString());
        map.put("kbcdef", buffer.toString());
        map.put("lbcdef", buffer.toString());
        map.put("mbcdef", buffer.toString());
        map.put("nbcdef", buffer.toString());

        map1 = map;

        buffer.setCharAt(100, '\n');
        map = new HashMap();
        map.put("abcdef", buffer.toString());
        map.put("bbcdef", buffer.toString());
        map.put("cbcdef", buffer.toString());
        map.put("dbcdef", buffer.toString());
        map.put("ebcdef", buffer.toString());
        map.put("fbcdef", buffer.toString());
        map.put("gbcdef", buffer.toString());
        map.put("hbcdef", buffer.toString());
        map.put("ibcdef", buffer.toString());
        map.put("jbcdef", buffer.toString());
        map.put("kbcdef", buffer.toString());
        map.put("lbcdef", buffer.toString());
        map.put("mbcdef", buffer.toString());
        map.put("nbcdef", buffer.toString());
        escapeMap = map;
    }

    @Benchmark
    public void fastjson(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.toJSONString(map1));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.toJSONString(map1));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.writeValueAsString(map1));
    }

    @Benchmark
    public void wastjson(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.toJsonString(map1));
    }

    @Benchmark
    public void escapeWastjson(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.toJsonString(escapeMap));
    }

    @Benchmark
    public void escapeFastjson(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.toJSONString(escapeMap));
    }

    @Benchmark
    public void escapeFastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.toJSONString(escapeMap));
    }

    @Benchmark
    public void escapeJackson(Blackhole bh) throws Exception {
        bh.consume(mapper.writeValueAsString(escapeMap));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(WriteCase.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
