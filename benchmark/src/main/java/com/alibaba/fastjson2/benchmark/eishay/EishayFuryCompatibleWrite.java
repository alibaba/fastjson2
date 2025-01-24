package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.alibaba.fastjson2.benchmark.utf8.UTF8Encode;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class EishayFuryCompatibleWrite {
    static MediaContent mc;

    static JSONWriter.Feature[] features = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.IgnoreNoneSerializable,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName
    };
    static JSONWriter.Context context = new JSONWriter.Context(
            JSONFactory.getDefaultObjectWriterProvider(), features
    );

    static ThreadSafeFury furyCompatible = Fury.builder()
            .withLanguage(org.apache.fury.config.Language.JAVA)
            .withRefTracking(true)
            .requireClassRegistration(false)
            .withCompatibleMode(org.apache.fury.config.CompatibleMode.COMPATIBLE)
            .buildThreadSafeFury();

    static {
        String str = UTF8Encode.readFromClasspath("data/eishay.json");
        mc = JSONReader.of(str)
                .read(MediaContent.class);
    }

    @Benchmark
    public void jsonb(Blackhole bh) {
        byte[] bytes = JSONB.toBytes(mc, context);
        bh.consume(bytes);
    }

    public int jsonbSize() {
        return JSONB.toBytes(mc, context).length;
    }

    @Benchmark
    public void fury(Blackhole bh) {
        byte[] bytes = furyCompatible.serialize(mc);
        bh.consume(bytes);
    }

    public int furySize() {
//        return furyCompatible.serialize(mc).length;
        return 0;
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayFuryCompatibleWrite.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
