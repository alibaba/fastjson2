package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.JDKUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.invoke.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.lang.invoke.MethodType.methodType;

public class AsciiToChars {
    static final Function<byte[], char[]> TO_CHARS;
    static final MethodHandle INFLATE;

    static {
        Function<byte[], char[]> toChars = null;
        MethodHandle inflate = null;
        if (JDKUtils.JVM_VERSION > 9) {
            try {
                Class<?> latin1Class = Class.forName("java.lang.StringLatin1");
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(latin1Class);
                MethodHandle handle = lookup.findStatic(
                        latin1Class, "toChars", MethodType.methodType(char[].class, byte[].class)
                );

                CallSite callSite = LambdaMetafactory.metafactory(
                        lookup,
                        "apply",
                        methodType(Function.class),
                        methodType(Object.class, Object.class),
                        handle,
                        methodType(char[].class, byte[].class)
                );
                toChars = (Function<byte[], char[]>) callSite.getTarget().invokeExact();

                inflate = lookup.findStatic(
                        latin1Class,
                        "inflate",
                        MethodType.methodType(void.class, byte[].class, int.class, char[].class, int.class, int.class)
                );
            } catch (Throwable ignored) {
                // ignored
            }
        }

        if (toChars == null) {
            toChars = AsciiToChars::toAsciiCharArray;
        }
        TO_CHARS = toChars;

        INFLATE = inflate;
    }

    @Benchmark
    public void for_cast(Blackhole bh) throws Throwable {
        for (int i = 0; i < bytesArray.length; i++) {
            byte[] bytes = bytesArray[i];
            char[] chars = toAsciiCharArray(bytes);
            bh.consume(chars);
        }
    }

    @Benchmark
    public void lambda_cast(Blackhole bh) throws Throwable {
        for (int i = 0; i < bytesArray.length; i++) {
            byte[] bytes = bytesArray[i];
            char[] chars = TO_CHARS.apply(bytes);
            bh.consume(chars);
        }
    }

    @Benchmark
    public void mh_inflate(Blackhole bh) throws Throwable {
        if (INFLATE == null) {
            return;
        }

        for (int i = 0; i < bytesArray.length; i++) {
            byte[] bytes = bytesArray[i];
            char[] chars = new char[bytes.length];
            INFLATE.invokeExact(bytes, 0, chars, 0, bytes.length);
            bh.consume(chars);
        }
    }

    public static char[] toAsciiCharArray(byte[] bytes) {
        char[] charArray = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            charArray[i] = (char) bytes[i];
        }
        return charArray;
    }

    static final byte[][] bytesArray;
    static {
        String[] strings = {
                "567988.735",
                "-811227.824",
                "17415.508",
                "668069.440",
                "77259.887",

                "733032.058",
                "44402.415",
                "99328.975",
                "759431.827",
                "651998.851"
        };

        byte[][] array2 = new byte[strings.length][];
        for (int i = 0; i < strings.length; i++) {
            array2[i] = strings[i].getBytes();
        }
        bytesArray = array2;
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(AsciiToChars.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
