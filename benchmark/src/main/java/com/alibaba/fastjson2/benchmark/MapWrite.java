package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSON;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.Map;

public class MapWrite {
    static final Map map;

    static {
        map = new HashMap<>();
        map.put("name", "0");
        map.put("id", "0");
        map.put("hello", "0");
        map.put("good", "0");
        map.put("price", "0");
        map.put("status", "0");
        map.put("stockNumber", "0");
        map.put("originPrice", "0");
        map.put("details", "0");
        map.put("summary", "0");
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void writeMap(Blackhole bh) {
        bh.consume(
                JSON.toJSONString(map));
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(MapWrite.class.getName())
                .resultFormat(ResultFormatType.JSON)
                .result("benchmark-result.json") // 在项目根目录下
                .jvmArgsAppend("-Xms128m", "-Xmx128m")
                .build();

        new Runner(opt).run();
    }
}
