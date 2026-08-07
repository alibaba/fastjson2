package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipInputStream;

public class LargeFile26MTest {
    static String str;
    static ObjectMapper mapper = new ObjectMapper();

    static final int COUNT = 10;

    static {
        try (
                InputStream fis = LargeFile26MTest.class.getClassLoader().getResourceAsStream("data/large-file.json.zip");
                BufferedInputStream bis = new BufferedInputStream(fis);
                ZipInputStream zipIn = new ZipInputStream(bis)
        ) {
            zipIn.getNextEntry();
            str = IOUtils.toString(zipIn, "UTF-8");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parseObject(str, ArrayList.class));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.parseObject(str, ArrayList.class));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(str, ArrayList.class));
    }

    //    @Test
    public void fastjson1_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson1_perf();
        }
    }

    //    @Test
    public void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    public void jackson_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson_perf();
        }
    }

    public static void fastjson2_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; ++i) {
            JSON.parseObject(str, ArrayList.class);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastjson2 millis : " + millis);
        // zulu17.32.13 : 446
        // zulu11.52.13 : 506
        // zulu8.58.0.13 : 492
    }

    public static void jackson_perf() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; ++i) {
            mapper.readValue(str, ArrayList.class);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("jackson millis : " + millis);
        // zulu17.32.13 : 502
        // zulu11.52.13 : 562
        // zulu8.58.0.13 : 498
    }

    public static void fastjson1_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; ++i) {
            com.alibaba.fastjson.JSON.parseObject(str, ArrayList.class);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastjson1 millis : " + millis);
        // zulu17.32.13 :
        // zulu11.52.13 :
        // zulu8.58.0.13 : 589
    }

    public static void main(String[] args) throws Exception {
//        new LargeFileTest().fastjson2_perf_test();
//        new LargeFileTest().fastjson1_perf_test();
//        new LargeFileTest().jackson_perf_test();
        Options options = new OptionsBuilder()
                .include(LargeFile26MTest.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
