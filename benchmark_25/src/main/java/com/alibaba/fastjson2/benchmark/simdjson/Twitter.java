package com.alibaba.fastjson2.benchmark.simdjson;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Twitter {
    static final String file = "data/simd-json/twitter.json";
    static byte[] bytes;
    static {
        try (InputStream is = Twitter.class.getClassLoader().getResourceAsStream(file)) {
            String str = IOUtils.toString(is, "UTF-8");
            bytes = str.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void fastjson2_parse(Blackhole bh) {
        bh.consume(JSON.parseObject(bytes, SimdJsonTwitter.class));
    }

    public void wast_parse(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(bytes, SimdJsonTwitter.class));
    }

    public record SimdJsonUser(boolean default_profile, String screen_name) {
    }

    public record SimdJsonStatus(SimdJsonUser user) {
    }

    public record SimdJsonTwitter(List<SimdJsonStatus> statuses) {
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(Twitter.class.getName())
                .mode(Mode.Throughput)
                .warmupIterations(3)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
