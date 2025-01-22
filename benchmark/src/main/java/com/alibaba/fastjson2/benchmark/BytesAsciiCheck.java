package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.benchmark.eishay.EishayParseBinaryArrayMapping;
import com.alibaba.fastjson2.util.JDKUtils;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.TimeUnit;

public class BytesAsciiCheck {
    static byte[] bytes;
    static char[] chars;
    static String str;
    static final MethodHandle INDEX_OF_CHAR;

    static {
        MethodHandle indexOfChar = null;
        try {
            try {
                Class<?> cStringLatin1 = Class.forName("java.lang.StringLatin1");
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(cStringLatin1);
                indexOfChar = lookup.findStatic(
                        cStringLatin1,
                        "indexOfChar",
                        MethodType.methodType(int.class, byte[].class, int.class, int.class, int.class));
            } catch (Throwable ignored) {
                // ignore
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        INDEX_OF_CHAR = indexOfChar;
        try {
            InputStream is = EishayParseBinaryArrayMapping.class.getClassLoader().getResourceAsStream("data/eishay.json");
            str = IOUtils.toString(is, "UTF-8");
            bytes = str.getBytes();
            chars = str.toCharArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Benchmark
    public void handler(Blackhole bh) throws Throwable {
        bh.consume(
                JDKUtils.METHOD_HANDLE_HAS_NEGATIVE.invoke(bytes, 0, bytes.length)
        );
    }

//    @Benchmark
    public void lambda(Blackhole bh) throws Throwable {
        bh.consume(
                JDKUtils.PREDICATE_IS_ASCII.test(bytes)
        );
    }

    // @Benchmark
    public void direct(Blackhole bh) throws Throwable {
        bh.consume(hasNegatives(bytes, 0, bytes.length));
    }

//    @Benchmark
    public void isASCII(Blackhole bh) throws Throwable {
        bh.consume(com.alibaba.fastjson2.util.IOUtils.isASCII(bytes, 0, bytes.length));
    }

    @Benchmark
    public void isLatin1(Blackhole bh) throws Throwable {
        bh.consume(com.alibaba.fastjson2.util.IOUtils.isLatin1(chars, 0, chars.length));
    }

//    @Benchmark
    public void isASCIIJDK(Blackhole bh) throws Throwable {
        bh.consume(com.alibaba.fastjson2.util.JDKUtils.PREDICATE_IS_ASCII.test(bytes));
    }

//    @Benchmark
    public void indexOfSlash(Blackhole bh) throws Throwable {
        bh.consume(com.alibaba.fastjson2.util.IOUtils.indexOfSlash(bytes, 0, bytes.length));
    }

//    @Benchmark
    public void indexOfSlashV(Blackhole bh) throws Throwable {
        bh.consume(com.alibaba.fastjson2.util.IOUtils.indexOfSlashV(bytes, 0, bytes.length));
    }

//    @Benchmark
    public void indexOfChar(Blackhole bh) throws Throwable {
        bh.consume(indexOfChar(bytes, '\'', 0, bytes.length));
    }

//    @Benchmark
    public void indexOfString(Blackhole bh) throws Throwable {
        bh.consume(str.indexOf('\\'));
    }

    private static int indexOfChar(byte[] bytes, int ch, int fromIndex, int toIndex) {
        try {
            return (int) INDEX_OF_CHAR.invokeExact(bytes, ch, fromIndex, toIndex);
        } catch (Throwable ignored) {
            throw new JSONException("");
        }
    }

    public static boolean hasNegatives(byte[] ba, int off, int len) {
        for (int i = off; i < off + len; i++) {
            if (ba[i] < 0) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(BytesAsciiCheck.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .threads(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
