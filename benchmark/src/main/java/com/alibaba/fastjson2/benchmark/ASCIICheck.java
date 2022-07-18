package com.alibaba.fastjson2.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class ASCIICheck {
    static char[] chars = "http://javaone.com/keynote_large.jpg".toCharArray();

    @Benchmark
    public void f0_vec(Blackhole bh) {
        boolean ascii = true;
        {
            int i = 0;
            while (i + 4 <= chars.length) {
                char c0 = chars[i];
                char c1 = chars[i + 1];
                char c2 = chars[i + 2];
                char c3 = chars[i + 3];
                if (c0 > 0x007F || c1 > 0x007F || c2 > 0x007F || c3 > 0x007F) {
                    ascii = false;
                    break;
                }
                i += 4;
            }
            if (ascii) {
                for (; i < chars.length; ++i) {
                    if (chars[i] > 0x007F) {
                        ascii = false;
                        break;
                    }
                }
            }
        }
        bh.consume(ascii);
    }

    @Benchmark
    public void f1(Blackhole bh) {
        boolean ascii = true;
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] > 0x007F) {
                ascii = false;
                break;
            }
        }
        bh.consume(ascii);
    }

    @Benchmark
    public void f2_vec(Blackhole bh) {
        boolean ascii = true;
        {
            int i = 0;
            while (i + 4 <= chars.length) {
                char c0 = chars[i];
                char c1 = chars[i + 1];
                char c2 = chars[i + 2];
                char c3 = chars[i + 3];
                if (c0 > 0x007F || c1 > 0x007F || c2 > 0x007F || c3 > 0x007F) {
                    ascii = false;
                    break;
                }
                i += 4;
            }
            if (ascii) {
                for (; i < chars.length; ++i) {
                    if (chars[i] > 0x007F) {
                        ascii = false;
                        break;
                    }
                }
            }
        }
        bh.consume(ascii);
    }

    @Benchmark
    public void f3(Blackhole bh) {
        boolean ascii = true;
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] > 0x007F) {
                ascii = false;
                break;
            }
        }
        bh.consume(ascii);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ASCIICheck.class.getName())
                .mode(Mode.Throughput)
                .warmupIterations(3)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
