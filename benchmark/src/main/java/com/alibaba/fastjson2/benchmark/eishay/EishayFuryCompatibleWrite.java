package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import io.fury.Fury;
import io.fury.ThreadSafeFury;
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

public class EishayFuryCompatibleWrite {
    static MediaContent mc;

    static JSONWriter.Feature[] features = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.IgnoreNoneSerializable,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.WriteNameAsSymbol,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName
    };
    static JSONWriter.Context context = new JSONWriter.Context(
            JSONFactory.getDefaultObjectWriterProvider(), features
    );

    static ThreadSafeFury furyCompatible = Fury.builder()
            .withLanguage(io.fury.config.Language.JAVA)
            .withRefTracking(true)
            .requireClassRegistration(false)
            .withCompatibleMode(io.fury.config.CompatibleMode.COMPATIBLE)
            .buildThreadSafeFury();

    static {
        try {
            InputStream is = EishayFuryCompatibleWrite.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
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
