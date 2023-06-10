package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.util.JDKUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

public class StringCreateBenchmark {
    static final char[] chars = new char[128];
    static long valueOffset;

    static {
        try {
            Field field = String.class.getDeclaredField("value");
            field.setAccessible(true);
            valueOffset = JDKUtils.UNSAFE.objectFieldOffset(field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Benchmark
    public String newString() {
        return new String(chars);
    }

    @Benchmark
    public String unsafe() throws Exception {
        String str = (String) JDKUtils.UNSAFE.allocateInstance(String.class);
        JDKUtils.UNSAFE.putObject(str, valueOffset, chars);
        return str;
    }

    public void new_benchmark() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000_000; i++) {
            unsafe();
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("new : " + millis);
    }

    //    @Test
    public void test_benchmark() throws Exception {
        for (int i = 0; i < 10; i++) {
            new_benchmark();
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(StringCreateBenchmark.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
