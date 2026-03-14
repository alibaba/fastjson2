package com.alibaba.fastjson3.benchmark.jjb;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class ClientsParseCNBytes {
    static byte[] utf8Bytes;

    static {
        try {
            InputStream is = ClientsParseCNBytes.class.getClassLoader().getResourceAsStream("data/jjb/client_cn.json");
            utf8Bytes = is.readAllBytes();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(utf8Bytes, Clients.class));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(utf8Bytes, Clients.class));
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(utf8Bytes, Clients.class));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ClientsParseCNBytes.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(2)
                .measurementIterations(3)
                .forks(2)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
