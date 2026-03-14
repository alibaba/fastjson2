package com.alibaba.fastjson3.benchmark.jjb;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class ClientsWriteCNBytes {
    static Clients clients;

    static {
        try {
            InputStream is = ClientsWriteCNBytes.class.getClassLoader().getResourceAsStream("data/jjb/client_cn.json");
            byte[] bytes = is.readAllBytes();
            clients = com.alibaba.fastjson2.JSON.parseObject(bytes, Clients.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.toJSONBytes(clients));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.toJSONBytes(clients));
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.toJsonBytes(clients));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ClientsWriteCNBytes.class.getName())
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
