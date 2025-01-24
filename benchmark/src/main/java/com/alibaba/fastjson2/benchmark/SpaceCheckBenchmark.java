package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.benchmark.utf8.UTF8Encode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class SpaceCheckBenchmark {
    static char[] chars = UTF8Encode.readFromClasspath("data/eishay.json").toCharArray();
    static final long SPACE = (1L << ' ') | (1L << '\n') | (1L << '\r') | (1L << '\f') | (1L << '\t') | (1L << '\b');

    @Benchmark
    public void spaceBitAnd(Blackhole bh) {
        int spaceCount = 0;
        for (char ch : chars) {
            boolean space = ch <= ' ' && ((1L << ch) & SPACE) != 0;
            if (space) {
                spaceCount++;
            }
        }
        bh.consume(spaceCount);
    }

    @Benchmark
    public void spaceOr(Blackhole bh) {
        int spaceCount = 0;
        for (char ch : chars) {
            boolean space = ch == ' '
                    || ch == '\n'
                    || ch == '\r'
                    || ch == '\f'
                    || ch == '\t'
                    || ch == '\b';
            if (space) {
                spaceCount++;
            }
        }
        bh.consume(spaceCount);
    }

    @Benchmark
    public void spaceOrPreCheck(Blackhole bh) {
        int spaceCount = 0;
        for (char ch : chars) {
            boolean space = (ch == ' '
                    || ch == '\n'
                    || ch == '\r'
                    || ch == '\f'
                    || ch == '\t'
                    || ch == '\b'
            );
            if (space) {
                spaceCount++;
            }
        }
        bh.consume(spaceCount);
    }

    @Benchmark
    public void CharacterIsWhitespace(Blackhole bh) {
        int spaceCount = 0;
        for (char ch : chars) {
            boolean space = Character.isWhitespace(ch);
            if (space) {
                spaceCount++;
            }
        }
        bh.consume(spaceCount);
    }

    @Benchmark
    public void spaceSwitch(Blackhole bh) {
        int spaceCount = 0;
        for (char ch : chars) {
            boolean space;
            switch (ch) {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                case '\b':
                case '\f':
                    space = true;
                    break;
                default:
                    space = false;
                    break;
            }
            if (space) {
                spaceCount++;
            }
        }
        bh.consume(spaceCount);
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(SpaceCheckBenchmark.class.getName())
                .mode(Mode.Throughput)
                .warmupIterations(1)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
