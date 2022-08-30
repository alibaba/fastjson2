package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class String20 {
    static final Class OBJECT_CLASS = com.alibaba.fastjson2.benchmark.primitves.vo.String20.class;
    static String str;
    static byte[] jsonbBytes;
    static ObjectMapper mapper = new ObjectMapper();

    public String20() {
        try {
            InputStream is = String20.class.getClassLoader().getResourceAsStream("data/String20_compact.json");
            str = IOUtils.toString(is, "UTF-8");
            jsonbBytes = JSONB.toBytes(
                    JSON.parseObject(str, OBJECT_CLASS)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(
                com.alibaba.fastjson.JSON.parseObject(str, OBJECT_CLASS)
        );
    }

    @Benchmark
    public void wastjson(Blackhole bh) {
        bh.consume(
                io.github.wycst.wast.json.JSON.parseObject(str, OBJECT_CLASS)
        );
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(
                JSON.parseObject(str, OBJECT_CLASS)
        );
    }

    @Benchmark
    public void fastjson2_jsonb(Blackhole bh) {
        bh.consume(
                JSONB.parseObject(jsonbBytes, OBJECT_CLASS)
        );
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(
                mapper.readValue(str, OBJECT_CLASS)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(String20.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
