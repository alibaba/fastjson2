package com.alibaba.fastjson2.benchmark.jjb;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class JJBBenchmark {
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(UsersWriteUTF8Bytes.class.getName())
                .include(UsersParseUTF8Bytes.class.getName())
                .include(ClientsWriteUTF8Bytes.class.getName())
                .include(ClientsParseUTF8Bytes.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(2)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
