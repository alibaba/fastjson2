package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class EishayFuryCompatibleParse {
    static MediaContent mc;
    static JSONReader.Feature[] features = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.IgnoreNoneSerializable,
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased
    };

    static JSONReader.Context context = new JSONReader.Context(
            JSONFactory.getDefaultObjectReaderProvider(), features
    );

    static byte[] jsonbBytes;
    static byte[] furyCompatibleBytes;
//
//    static io.fury.ThreadSafeFury furyCompatible = io.fury.Fury.builder()
//            .withLanguage(io.fury.Language.JAVA)
//            .withReferenceTracking(true)
//            .disableSecureMode()
//            .withCompatibleMode(io.fury.serializers.CompatibleMode.COMPATIBLE)
//            .buildThreadSafeFury();

    static {
        try {
            InputStream is = EishayFuryCompatibleParse.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);

            jsonbBytes = JSONB.toBytes(mc, EishayFuryCompatibleWrite.features);
//            furyCompatibleBytes = furyCompatible.serialize(mc);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void jsonb(Blackhole bh) {
        Object object = JSONB.parseObject(jsonbBytes, Object.class, context);
        bh.consume(object);
    }

//    @Benchmark
    public void fury(Blackhole bh) {
//        Object object = furyCompatible.deserialize(furyCompatibleBytes);
//        bh.consume(object);
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(EishayFuryCompatibleParse.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
