package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.util.UnsafeUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class StringCreateBenchmark {
    static final BiFunction<char[], Boolean, String> STRING_CREATOR = getStringCreator();
    static final char[] chars = new char[128];
    static long valueOffset;

    static {
        try {
            Field field = String.class.getDeclaredField("value");
            field.setAccessible(true);
            valueOffset = UnsafeUtils.UNSAFE.objectFieldOffset(field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static BiFunction<char[], Boolean, String> getStringCreator() {
        try {
            MethodHandles.Lookup caller = MethodHandles.lookup().in(String.class);
            Field modes = MethodHandles.Lookup.class.getDeclaredField("allowedModes");
            modes.setAccessible(true);
            modes.setInt(caller, -1);   // -1 == Lookup.TRUSTED
            // create handle for shared String constructor
            MethodHandle handle = caller.findConstructor(
                    String.class,
                    MethodType.methodType(void.class, char[].class, boolean.class)
            );

            CallSite callSite = LambdaMetafactory.metafactory(
                    caller,
                    "apply",
                    MethodType.methodType(BiFunction.class),
                    handle.type().generic(),
                    handle,
                    handle.type()
            );
            return (BiFunction) callSite.getTarget().invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public String creator() {
        return STRING_CREATOR.apply(chars, Boolean.TRUE);
    }

    @Benchmark
    public String newString() {
        return new String(chars);
    }

    @Benchmark
    public String unsafe() throws Exception {
        String str = (String) UnsafeUtils.UNSAFE.allocateInstance(String.class);
        UnsafeUtils.UNSAFE.putObject(str, valueOffset, chars);
        return str;
    }

    public void creator_benchmark() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000_000_000; i++) {
            creator();
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("creator : " + millis);
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
