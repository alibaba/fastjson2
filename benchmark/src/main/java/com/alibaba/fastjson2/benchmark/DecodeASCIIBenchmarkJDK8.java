package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.util.UnsafeUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson2.util.JDKUtils.STRING_CREATOR_JDK8;

public class DecodeASCIIBenchmarkJDK8 {
    static byte[] utf8Bytes = new byte[128];
    static int utf8BytesLength;
    static long valueFieldOffset;

    static {
        try {
            Field valueField = String.class.getDeclaredField("value");
            valueFieldOffset = UnsafeUtils.UNSAFE.objectFieldOffset(valueField);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        byte[] bytes = "01234567890ABCDEFGHIJKLMNOPQRSTUVWZYZabcdefghijklmnopqrstuvwzyz".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(bytes, 0, utf8Bytes, 0, bytes.length);
        utf8BytesLength = bytes.length;
    }

    @Benchmark
    public String unsafeEncodeUTF8() throws Exception {
        char[] chars = new char[utf8BytesLength];
        for (int i = 0; i < utf8BytesLength; i++) {
            chars[i] = (char) utf8Bytes[i];
        }
        return STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
    }

    @Benchmark
    public String newStringUTF8() throws Exception {
        return new String(utf8Bytes, 0, utf8BytesLength, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DecodeASCIIBenchmarkJDK8.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
