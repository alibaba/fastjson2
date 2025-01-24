package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.alibaba.fastjson2.benchmark.utf8.UTF8Encode;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class EishayWriteUTF8Bytes {
    static MediaContent mc;
    static final ObjectMapper mapper = new ObjectMapper();
    static final Gson gson = new Gson();
    static final ObjectWriterProvider featuresProvider;
    static final JSONWriter.Context featuresContext;
    static {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.setDisableReferenceDetect(true);
        provider.setDisableJSONB(true);
        provider.setDisableArrayMapping(true);
        provider.setDisableAutoType(true);
        featuresProvider = provider;
        featuresContext = new JSONWriter.Context(provider);
        String str = UTF8Encode.readFromClasspath("data/eishay.json");
        mc = JSONReader.of(str)
                .read(MediaContent.class);
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.toJSONBytes(mc));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.toJSONBytes(mc));
    }

    public void fastjson2_features(Blackhole bh) {
        bh.consume(JSON.toJSONBytes(mc, StandardCharsets.UTF_8, featuresContext));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.writeValueAsBytes(mc));
    }

    @Benchmark
    public void gson(Blackhole bh) throws Exception {
        bh.consume(gson
                .toJson(mc)
                .getBytes(StandardCharsets.UTF_8)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayWriteUTF8Bytes.class.getName())
                .exclude(EishayWriteUTF8BytesTree.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
