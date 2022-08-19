package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
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

public class EishayParseString {
    static String str;
    static ObjectMapper mapper = new ObjectMapper();
    static Gson gson = new Gson();

    static {
        try {
            InputStream is = EishayParseString.class.getClassLoader().getResourceAsStream("data/eishay_compact.json");
            str = IOUtils.toString(is, "UTF-8");
            JSON.parseObject(str, MediaContent.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parseObject(str, MediaContent.class));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.parseObject(str, MediaContent.class));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(str, MediaContent.class));
    }

//    @Benchmark
    public void wastjson(Blackhole bh) throws Exception {
        bh.consume(
                io.github.wycst.wast.json.JSON.parseObject(str, MediaContent.class)
        );
    }

    public void gson(Blackhole bh) throws Exception {
        bh.consume(
                gson.fromJson(str, MediaContent.class)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayParseString.class.getName())
                .exclude(EishayParseStringPretty.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
