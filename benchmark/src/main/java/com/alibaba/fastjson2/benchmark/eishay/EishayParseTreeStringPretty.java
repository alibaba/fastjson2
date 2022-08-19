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
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class EishayParseTreeStringPretty {
    static String str;
    static final ObjectMapper mapper = new ObjectMapper();
    static final Gson gson = new Gson();

    static {
        try {
            InputStream is = EishayParseTreeStringPretty.class.getClassLoader().getResourceAsStream("data/eishay.json");
            str = IOUtils.toString(is, "UTF-8");
            JSON.parseObject(str, MediaContent.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parseObject(str));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.parseObject(str));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(str, HashMap.class));
    }

    @Benchmark
    public void gson(Blackhole bh) throws Exception {
        bh.consume(
                gson.fromJson(str, HashMap.class)
        );
    }

//    @Benchmark
    public void wastjson(Blackhole bh) throws Exception {
        bh.consume(
                io.github.wycst.wast.json.JSON.parse(str)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayParseTreeStringPretty.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
