package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class StringField20 {
    static String str;
    static ObjectMapper mapper = new ObjectMapper();

    public StringField20() {
        try {
            InputStream is = StringField20.class.getClassLoader().getResourceAsStream("data/String20_compact.json");
            str = IOUtils.toString(is, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public com.alibaba.fastjson2.benchmark.primitves.vo.StringField20 fastjson1() {
        return com.alibaba.fastjson.JSON.parseObject(str, com.alibaba.fastjson2.benchmark.primitves.vo.StringField20.class);
    }

    @Benchmark
    public com.alibaba.fastjson2.benchmark.primitves.vo.StringField20 fastjson2() {
        return JSON.parseObject(str, com.alibaba.fastjson2.benchmark.primitves.vo.StringField20.class);
    }

    @Benchmark
    public com.alibaba.fastjson2.benchmark.primitves.vo.StringField20 jackson() throws Exception {
        return mapper.readValue(str, com.alibaba.fastjson2.benchmark.primitves.vo.StringField20.class);
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
            JSON.parseObject(str, com.alibaba.fastjson2.benchmark.primitves.vo.StringField20.class);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println(com.alibaba.fastjson2.benchmark.primitves.vo.StringField20.class.getSimpleName() + " : " + millis);
        // zulu17.32.13 : 418
        // zulu11.52.13 :
        // zulu8.58.0.13 : 598
    }

    //    @Test
    public void fastjson1_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson1_perf();
        }
    }

    public static void fastjson1_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            com.alibaba.fastjson.JSON.parseObject(str, com.alibaba.fastjson2.benchmark.primitves.vo.StringField20.class);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println(com.alibaba.fastjson2.benchmark.primitves.vo.StringField20.class.getSimpleName() + " : " + millis);
        // zulu17.32.13 : 365
        // zulu11.52.13 :
        // zulu8.58.0.13 : 605
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(StringField20.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
