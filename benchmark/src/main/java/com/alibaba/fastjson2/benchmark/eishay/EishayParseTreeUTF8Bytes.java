package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSON;
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class EishayParseTreeUTF8Bytes {
    static byte[] utf8Bytes;
    static final ObjectMapper mapper = new ObjectMapper();
    static final Gson gson = new Gson();

    static {
        try {
            InputStream is = EishayParseTreeUTF8Bytes.class.getClassLoader().getResourceAsStream("data/eishay_compact.json");
            utf8Bytes = IOUtils.toString(is, "UTF-8").getBytes(StandardCharsets.UTF_8);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parse(utf8Bytes));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.parseObject(utf8Bytes));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(utf8Bytes, HashMap.class));
    }

    @Benchmark
    public void gson(Blackhole bh) throws Exception {
        bh.consume(gson
                .fromJson(
                        new String(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.UTF_8),
                        HashMap.class)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayParseTreeUTF8Bytes.class.getName())
                .exclude(EishayParseTreeUTF8BytesPretty.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
