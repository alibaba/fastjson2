package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;

public class DecodeUTF8BenchmarkJDK17 {
    static byte[] utf8Bytes = "01234567890ABCDEFGHIJKLMNOPQRSTUVWZYZabcdefghijklmnopqrstuvwzyz"
            .getBytes(StandardCharsets.UTF_8);
    static long valueFieldOffset;
    static BiFunction<byte[], Charset, String> stringCreator;

    static {
        try {
            Field valueField = String.class.getDeclaredField("value");
            valueFieldOffset = UnsafeUtils.UNSAFE.objectFieldOffset(valueField);
            stringCreator = getStringCreatorJDK17();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static BiFunction<byte[], Charset, String> getStringCreatorJDK17() throws Throwable {
        // GraalVM not support
        // Android not support
        MethodHandles.Lookup lookup = getLookup();

        MethodHandles.Lookup caller = lookup.in(String.class);
        MethodHandle handle = caller.findStatic(
                String.class, "newStringNoRepl1", MethodType.methodType(String.class, byte[].class, Charset.class)
        );

        CallSite callSite = LambdaMetafactory.metafactory(
                caller,
                "apply",
                MethodType.methodType(BiFunction.class),
                handle.type().generic(),
                handle,
                handle.type()
        );
        return (BiFunction<byte[], Charset, String>) callSite.getTarget().invokeExact();
    }

    private static MethodHandles.Lookup getLookup() throws Exception {
        // GraalVM not support
        // Android not support
        MethodHandles.Lookup lookup;
        if (JVM_VERSION >= 17) {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class);
            constructor.setAccessible(true);
            lookup = constructor.newInstance(
                    String.class,
                    null,
                    -1 // Lookup.TRUSTED
            );
        } else {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);
            lookup = constructor.newInstance(
                    String.class,
                    -1 // Lookup.TRUSTED
            );
        }
        return lookup;
    }

    @Benchmark
    public String unsafeEncodeUTF8_17() throws Exception {
        byte[] buf = new byte[utf8Bytes.length * 2];
        int len = IOUtils.decodeUTF8(utf8Bytes, 0, utf8Bytes.length, buf);
        byte[] chars = Arrays.copyOf(buf, len);
        return stringCreator.apply(chars, StandardCharsets.US_ASCII);
    }

    @Benchmark
    public String newStringUTF8_17() throws Exception {
        return new String(utf8Bytes);
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
