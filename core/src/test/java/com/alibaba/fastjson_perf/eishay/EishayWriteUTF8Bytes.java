package com.alibaba.fastjson_perf.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
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

public class EishayWriteUTF8Bytes {
    static MediaContent mc;
    static ObjectMapper mapper = new ObjectMapper();

    static {
        try {
            InputStream is = Int2Test.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);
            fastjson2_perf();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1() {
        com.alibaba.fastjson.JSON.toJSONBytes(mc);
    }

    @Benchmark
    public void fastjson2() {
        JSON.toJSONBytes(mc);
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
            JSON.toJSONBytes(mc);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
        // zulu17.32.13 : 330
        // zulu8.58.0.13 : 379 332
    }

    @Benchmark
    public void jackson() throws Exception {
        mapper.writeValueAsBytes(mc);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayWriteUTF8Bytes.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }

}
