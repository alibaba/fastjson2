package com.alibaba.fastjson2.benchmark.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.benchmark.utf8.UTF8Encode;
import com.jayway.jsonpath.JsonPath;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class BookStore {
    private static String str = UTF8Encode.readFromClasspath("data/bookstore.json");

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(
                JSONPath.extract(str, "$.store.book[*].author")
        );
    }

    @Benchmark
    public void jayway(Blackhole bh) {
        bh.consume(
                JsonPath.read(str, "$.store.book[*].author")
        );
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(BookStore.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
