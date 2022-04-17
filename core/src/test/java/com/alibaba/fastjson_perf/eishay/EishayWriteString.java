package com.alibaba.fastjson_perf.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.eishay.vo.MediaContent;
import com.alibaba.fastjson_perf.Int2Test;
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

public class EishayWriteString {
    static MediaContent mc;
    static ObjectMapper mapper = new ObjectMapper();

    static {
        try {
            InputStream is = Int2Test.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1() {
        com.alibaba.fastjson.JSON.toJSONString(mc);
    }

    @Benchmark
    public void fastjson2() {
        JSON.toJSONString(mc);
    }

    @Benchmark
    public void jackson() throws Exception {
        mapper.writeValueAsString(mc);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayWriteString.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }

}
