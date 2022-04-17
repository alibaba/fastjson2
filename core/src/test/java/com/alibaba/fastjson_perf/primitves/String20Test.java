package com.alibaba.fastjson_perf.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.eishay.vo.MediaContent;
import com.alibaba.fastjson2_vo.String20;
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

public class String20Test {
    static String str;
    static ObjectMapper mapper = new ObjectMapper();

    public String20Test() {
        try {
            InputStream is = String20Test.class.getClassLoader().getResourceAsStream("data/String20_compact.json");
            str = IOUtils.toString(is, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1() {
        com.alibaba.fastjson.JSON.parseObject(str, String20.class);
    }

    @Benchmark
    public void fastjson2() {
        com.alibaba.fastjson2.JSON.parseObject(str, String20.class);
    }

    @Benchmark
    public void jackson() throws Exception {
        mapper.readValue(str, String20.class);
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
            JSON.parseObject(str, String20.class);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("String20Test : " + millis);
        // zulu17.32.13 :
        // zulu11.52.13 :
        // zulu8.58.0.13 :
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(String20Test.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
