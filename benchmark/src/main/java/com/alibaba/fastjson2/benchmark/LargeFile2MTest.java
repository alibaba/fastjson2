package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipInputStream;

public class LargeFile2MTest {
    static String str;
    static ObjectMapper mapper = new ObjectMapper();

    static final int COUNT = 100;

    static {
        try (
                InputStream fis = LargeFile2MTest.class.getClassLoader().getResourceAsStream("data/large-file-2m.json.zip");
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
        bh.consume(com.alibaba.fastjson.JSON.parseObject(str));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.parseObject(str));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(str, HashMap.class));
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
            JSON.parseObject(str);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastjson2 millis : " + millis);
        // zulu17.32.13 : 1299 1136
        // zulu11.52.13 : 1187 1145
        // zulu8.58.0.13 : 1154
    }

    public static void jackson_perf() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; ++i) {
            mapper.readValue(str, HashMap.class);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("jackson millis : " + millis);
        // zulu17.32.13 :
        // zulu11.52.13 :
        // zulu8.58.0.13 :
    }

    public static void fastjson1_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; ++i) {
            com.alibaba.fastjson.JSON.parseObject(str);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastjson1 millis : " + millis);
        // zulu17.32.13 :
        // zulu11.52.13 :
        // zulu8.58.0.13 :
    }

    public static void main(String[] args) throws Exception {
        new LargeFile2MTest().fastjson2_perf_test();
//        new LargeFile2MTest().fastjson1_perf_test();
//        new LargeFile2MTest().jackson_perf_test();
//        Options options = new OptionsBuilder()
//                .include(LargeFile2MTest.class.getName())
//                .mode(Mode.Throughput)
//                .timeUnit(TimeUnit.MILLISECONDS)
//                .forks(1)
//                .build();
//        new Runner(options).run();
    }
}
