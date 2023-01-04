package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class StringFormatBenchmark {
    static int value = 10245;
    static final String PREFIX = "id : ";
    static final char[] PREFIX_CHARS = PREFIX.toCharArray();
    static final byte[] PREFIX_BYTES = PREFIX.getBytes();

    @Benchmark
    public void format(Blackhole bh) throws Exception {
        bh.consume(
                String.format("id : %s", value)
        );
    }

    @Benchmark
    public void StringBuffer(Blackhole bh) {
        bh.consume(
                new StringBuilder().append(PREFIX).append(value).toString()
        );
    }

    @Benchmark
    public void creator(Blackhole bh) {
        int i = value;
        int size = (i < 0) ? IOUtils.stringSize(-i) + 1 : IOUtils.stringSize(i);
        String str;
        if (JDKUtils.JVM_VERSION == 8) {
            char[] chars = new char[PREFIX_CHARS.length + size];
            System.arraycopy(PREFIX_CHARS, 0, chars, 0, PREFIX_CHARS.length);
            IOUtils.getChars(i, chars.length, chars);
            str = JDKUtils.STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
        } else {
            byte[] chars = new byte[PREFIX_BYTES.length + size];
            System.arraycopy(PREFIX_BYTES, 0, chars, 0, PREFIX_BYTES.length);
            IOUtils.getChars(i, chars.length, chars);
            str = JDKUtils.STRING_CREATOR_JDK11.apply(chars, JDKUtils.LATIN1);
        }
        bh.consume(str);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(StringFormatBenchmark.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
