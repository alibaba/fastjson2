package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class EishayParseTreeUTF8Bytes {
    static byte[] utf8Bytes;
    static ObjectMapper mapper = new ObjectMapper();

    static {
        try {
            InputStream is = EishayParseTreeUTF8Bytes.class.getClassLoader().getResourceAsStream("data/eishay_compact.json");
            utf8Bytes = IOUtils.toString(is, "UTF-8").getBytes(StandardCharsets.UTF_8);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parse(utf8Bytes));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.parseObject(utf8Bytes));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(utf8Bytes, HashMap.class));
    }

    //    @Test
    public void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    public static void fastjson2_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            JSON.parseObject(utf8Bytes);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
        // zulu17.32.13 : 991 981 1008
        // zulu11.52.13 : 1154 965 922
        // zulu8.58.0.13 : 1155 1010
    }

    public void fastjson1_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson1_perf();
        }
    }

    public static void fastjson1_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            com.alibaba.fastjson.JSON.parse(utf8Bytes);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
        // zulu17.32.13 : 1379
        // zulu11.52.13 : 1871
        // zulu8.58.0.13 : 1586
    }

    public static void main(String[] args) throws RunnerException {
//        new EishayParseTreeUTF8Bytes().fastjson1_perf_test();
//        new EishayParseTreeUTF8Bytes().fastjson2_perf_test();
        Options options = new OptionsBuilder()
                .include(EishayParseTreeUTF8Bytes.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
