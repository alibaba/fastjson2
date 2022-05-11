package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.util.UnsafeUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

public class StringGetValueBenchmark {
    static String STR = "01234567890ABCDEFGHIJKLMNOPQRSTUVWZYZabcdefghijklmnopqrstuvwzyz一二三四五六七八九十";

    static final char[] chars = new char[128];
    static Field valueField;
    static long valueFieldOffset;

    static {
        try {
            valueField = String.class.getDeclaredField("value");
            valueField.setAccessible(true);
            valueFieldOffset = UnsafeUtils.UNSAFE.objectFieldOffset(valueField);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

//
//    @Benchmark
//    public void charAt() {
//        for (int i = 0; i < STR.length(); ++i) {
//            char ch = STR.charAt(i);
//        }
//    }

//
//    @Benchmark
//    public void toCharArray() throws Exception {
//        char[] chars = STR.toCharArray();
//        for (int i = 0; i < chars.length; i++) {
//            char ch = chars[i];
//        }
//    }

    @Benchmark
    public char[] reflect() throws Exception {
        return (char[]) valueField.get(STR);
//        for (int i = 0; i < chars.length; i++) {
//            char ch = chars[i];
//        }
    }

    @Benchmark
    public char[] unsafe() throws Exception {
        return (char[]) UnsafeUtils.UNSAFE.getObject(STR, valueFieldOffset);
//        for (int i = 0; i < chars.length; i++) {
//            char ch = chars[i];
//        }
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(StringGetValueBenchmark.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
