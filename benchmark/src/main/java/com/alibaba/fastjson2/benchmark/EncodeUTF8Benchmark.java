package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.util.IOUtils;
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

public class EncodeUTF8Benchmark {
    static String STR = "01234567890ABCDEFGHIJKLMNOPQRSTUVWZYZabcdefghijklmnopqrstuvwzyz一二三四五六七八九十";
    static byte[] out;

    static long valueFieldOffset;

    static {
        out = new byte[STR.length() * 3];
        try {
            Field valueField = String.class.getDeclaredField("value");
            valueFieldOffset = UnsafeUtils.UNSAFE.objectFieldOffset(valueField);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Benchmark
    public int unsafeEncodeUTF8() throws Exception {
        char[] chars = (char[]) UnsafeUtils.UNSAFE.getObject(STR, valueFieldOffset);
        return IOUtils.encodeUTF8(chars, 0, chars.length, out, 0);
    }

    @Benchmark
    public byte[] getBytesUTF8() throws Exception {
        byte[] bytes = STR.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(bytes, 0, out, 0, bytes.length);
        return out;
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EncodeUTF8Benchmark.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
