package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class DecodeUTF8BenchmarkJDK17 {
    static byte[] utf8Bytes = "01234567890ABCDEFGHIJKLMNOPQRSTUVWZYZabcdefghijklmnopqrstuvwzyz"
            .getBytes(StandardCharsets.UTF_8);
    static long valueFieldOffset;
    static BiFunction<byte[], Charset, String> stringCreator;

    static {
        try {
            Field valueField = String.class.getDeclaredField("value");
            valueFieldOffset = UnsafeUtils.UNSAFE.objectFieldOffset(valueField);
            stringCreator = JDKUtils.getStringCreatorJDK17();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Benchmark
    public void unsafeEncodeUTF8_17() throws Exception {
        byte[] buf = new byte[utf8Bytes.length * 2];
        int len = IOUtils.decodeUTF8(utf8Bytes, 0, utf8Bytes.length, buf);
        byte[] chars = Arrays.copyOf(buf, len);
        stringCreator.apply(chars, StandardCharsets.US_ASCII);
    }

    @Benchmark
    public void newStringUTF8_17() throws Exception {
        new String(utf8Bytes);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DecodeUTF8BenchmarkJDK17.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
