package com.alibaba.fastjson2.benchmark.wast;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class IOUtilsBench {
    static byte[] bytes;
    static char[] chars;
    static String str;

    static {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            buf.append(12345678);
        }
        str = buf.toString();
        bytes = str.getBytes();
        chars = str.toCharArray();
    }

    @Benchmark
    public void digit4(Blackhole bh) throws Throwable {
        for (int i = 0; i < 1000; i += 8) {
            bh.consume(com.alibaba.fastjson2.internal.Conf.BYTES.digit4(bytes, 0));
        }
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(IOUtilsBench.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .threads(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
