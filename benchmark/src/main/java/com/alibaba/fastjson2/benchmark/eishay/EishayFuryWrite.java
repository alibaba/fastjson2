package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
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

public class EishayFuryWrite {
    static MediaContent mc;
//
//    static io.fury.ThreadSafeFury fury = io.fury.Fury.builder()
//            .withLanguage(io.fury.Language.JAVA)
//            .withReferenceTracking(true)
//            .disableSecureMode()
//            .buildThreadSafeFury();

    static JSONWriter.Feature[] features = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.IgnoreNoneSerializable,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.BeanToArray
    };
    static JSONWriter.Context context = new JSONWriter.Context(
            JSONFactory.getDefaultObjectWriterProvider(), features
    );

    static {
        try {
            InputStream is = EishayFuryWrite.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2JSONB(Blackhole bh) {
        bh.consume(
                JSONB.toBytes(mc, context)
        );
    }

//    @Benchmark
    public void fury(Blackhole bh) {
//        byte[] bytes = fury.serialize(mc);
//        bh.consume(bytes);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayFuryWrite.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
