package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
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
import java.util.concurrent.TimeUnit;

public class EishayParseUTF8Bytes {
    static byte[] utf8Bytes;
    static ObjectMapper mapper = new ObjectMapper();

    static {
        try {
            InputStream is = EishayParseUTF8Bytes.class.getClassLoader().getResourceAsStream("data/eishay_compact.json");
            utf8Bytes = IOUtils.toString(is, "UTF-8").getBytes(StandardCharsets.UTF_8);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parseObject(utf8Bytes, MediaContent.class));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.parseObject(utf8Bytes, MediaContent.class));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(utf8Bytes, MediaContent.class));
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
            JSON.parseObject(utf8Bytes, MediaContent.class);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("EishayParseUTF8Bytes : " + millis);
        // zulu17.32.13 : 722 586
        // zulu11.52.13 : 565
        // zulu8.58.0.13 :
    }

    public void fastjson1_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson1_perf();
        }
    }

    public static void fastjson1_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            com.alibaba.fastjson.JSON.parseObject(utf8Bytes, MediaContent.class);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
        // zulu17.32.13 : 588
        // zulu11.52.13 :
        // zulu8.58.0.13 :
    }

    public static void main(String[] args) throws RunnerException {
//        new EishayParseUTF8Bytes().fastjson1_perf_test();
//        new EishayParseUTF8Bytes().fastjson2_perf_test();
        Options options = new OptionsBuilder()
                .include(EishayParseUTF8Bytes.class.getName())
                .exclude(EishayParseUTF8BytesPretty.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
