package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class EishayWriteStringTree1x {
    static com.alibaba.fastjson.JSONObject mc;
    static final ObjectMapper mapper = new ObjectMapper();
    static final Gson gson = new Gson();

    static {
        try {
            InputStream is = EishayWriteStringTree1x.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = com.alibaba.fastjson.JSON.parseObject(str);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.toJSONString(mc));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.toJSONString(mc));
    }

    public void fastjson2_ReferenceDetection(Blackhole bh) {
        bh.consume(JSON.toJSONString(mc, JSONWriter.Feature.ReferenceDetection));
    }

//    @Benchmark
    public void fastjson2_jsonb(Blackhole bh) {
        bh.consume(JSONB.toBytes(mc));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.writeValueAsString(mc));
    }

    //    @Benchmark
    public void wastjson(Blackhole bh) throws Exception {
        bh.consume(
                io.github.wycst.wast.json.JSON.toJsonString(mc)
        );
    }

    @Benchmark
    public void gson(Blackhole bh) throws Exception {
        bh.consume(
                gson.toJson(mc)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayWriteStringTree1x.class.getName())
                .mode(Mode.Throughput)
                .warmupIterations(3)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
