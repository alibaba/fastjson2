package com.alibaba.fastjson_perf.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.eishay.vo.MediaContent;
import com.alibaba.fastjson_perf.Int2Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;


public class EishayParseString {
    static String str;
    static ObjectMapper mapper = new ObjectMapper();

    static {
        try {
            InputStream is = Int2Test.class.getClassLoader().getResourceAsStream("data/eishay_compact.json");
            str = IOUtils.toString(is, "UTF-8");
            com.alibaba.fastjson2.JSON.parseObject(str, MediaContent.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1() {
        com.alibaba.fastjson.JSON.parseObject(str, MediaContent.class);
    }

    @Benchmark
    public void fastjson2() {
        com.alibaba.fastjson2.JSON.parseObject(str, MediaContent.class);
    }

    @Benchmark
    public void jackson() throws Exception {
        mapper.readValue(str, MediaContent.class);
    }

    @Test
    public void fastjson1_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson1_perf();
        }
    }

    @Test
    public void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    public static void fastjson2_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            com.alibaba.fastjson2.JSON.parseObject(str, MediaContent.class);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
        // zulu17.32.13 : 504 460
        // zulu11.52.13 : 480
        // zulu8.58.0.13 : 517
    }

    public static void fastjson1_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            com.alibaba.fastjson.JSON.parseObject(str, MediaContent.class);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
        // zulu17.32.13 :
        // zulu11.52.13 : 928
        // zulu8.58.0.13 :
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayParseString.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }

}
