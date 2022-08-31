package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.benchmark.primitves.vo.BigDecimal20Field;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
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

public class BigDecimal20 {
    static String str;
    static byte[] jsonbBytes;
    static ObjectMapper mapper = new ObjectMapper();
    static Gson gson = new Gson();

    public BigDecimal20() {
        try {
            InputStream is = BigDecimal20.class.getClassLoader().getResourceAsStream("data/dec20.json");
            str = IOUtils.toString(is, "UTF-8");
            jsonbBytes = JSONB.toBytes(
                    JSON.parseObject(str, BigDecimal20Field.class)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(
                com.alibaba.fastjson.JSON.parseObject(str, BigDecimal20Field.class)
        );
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(
                JSON.parseObject(str, BigDecimal20Field.class)
        );
    }

    @Benchmark
    public void fastjson2_jsonb(Blackhole bh) {
        bh.consume(
                JSONB.parseObject(jsonbBytes, BigDecimal20Field.class)
        );
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(
                mapper.readValue(str, BigDecimal20Field.class)
        );
    }

    @Benchmark
    public void gson(Blackhole bh) throws Exception {
        bh.consume(
                gson.fromJson(str, BigDecimal20Field.class)
        );
    }

    @Benchmark
    public void wastjson(Blackhole bh) throws Exception {
        bh.consume(
                io.github.wycst.wast.json.JSON.parseObject(str, BigDecimal20Field.class)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(BigDecimal20.class.getName())
                .exclude(BigDecimal20Tree.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
