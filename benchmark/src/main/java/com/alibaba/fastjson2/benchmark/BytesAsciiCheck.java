package com.alibaba.fastjson2.benchmark;

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
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson2.util.JDKUtils.ARRAY_BYTE_BASE_OFFSET;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

public class BytesAsciiCheck {
    static byte[] bytes;

    static {
        try {
            InputStream is = EishayParseBinaryArrayMapping.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            bytes = str.getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Benchmark
    public void handler(Blackhole bh) throws Throwable {
        bh.consume(
                JDKUtils.METHOD_HANDLE_HAS_NEGATIVE.invoke(bytes, 0, bytes.length)
        );
    }

    @Benchmark
    public void lambda(Blackhole bh) throws Throwable {
        bh.consume(
                JDKUtils.PREDICATE_IS_ASCII.test(bytes)
        );
    }

    @Benchmark
    public void direct(Blackhole bh) throws Throwable {
        bh.consume(hasNegatives(bytes, 0, bytes.length));
    }

    @Benchmark
    public void direct8(Blackhole bh) throws Throwable {
        bh.consume(hasNegatives_8(bytes, 0, bytes.length));
    }

    public static boolean hasNegatives(byte[] ba, int off, int len) {
        for (int i = off; i < off + len; i++) {
            if (ba[i] < 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasNegatives_8(byte[] bytes, int off, int len) {
        int i = off;
        while (i + 8 <= off + len) {
            if ((UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + i) & 0x8080808080808080L) != 0) {
                return true;
            }
            i += 8;
        }

        for (; i < off + len; i++) {
            if (bytes[i] < 0) {
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
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
