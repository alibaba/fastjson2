package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
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

public class EishayFuryParse {
    static MediaContent mc;
    static byte[] fastjson2JSONBBytes;
    static byte[] fastjson2JSONBBytes_arrayMapping;
    static byte[] furyBytes;
    static byte[] furyCompatibleBytes;
//
//    static io.fury.ThreadSafeFury fury = io.fury.Fury.builder()
//            .withLanguage(io.fury.Language.JAVA)
//            .withReferenceTracking(true)
//            .disableSecureMode()
//            .buildThreadSafeFury();
//
//    static io.fury.ThreadSafeFury furyCompatible = io.fury.Fury.builder()
//            .withLanguage(io.fury.Language.JAVA)
//            .withReferenceTracking(true)
//            .disableSecureMode()
//            .withCompatibleMode(io.fury.serializers.CompatibleMode.COMPATIBLE)
//            .buildThreadSafeFury();

    static {
        try {
            InputStream is = EishayFuryParse.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);

            fastjson2JSONBBytes = JSONB.toBytes(mc, JSONWriter.Feature.WriteClassName,
                    JSONWriter.Feature.IgnoreNoneSerializable,
                    JSONWriter.Feature.FieldBased,
                    JSONWriter.Feature.ReferenceDetection,
                    JSONWriter.Feature.WriteNulls,
                    JSONWriter.Feature.NotWriteDefaultValue,
                    JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                    JSONWriter.Feature.WriteNameAsSymbol);

            fastjson2JSONBBytes_arrayMapping = JSONB.toBytes(mc, JSONWriter.Feature.WriteClassName,
                    JSONWriter.Feature.IgnoreNoneSerializable,
                    JSONWriter.Feature.FieldBased,
                    JSONWriter.Feature.ReferenceDetection,
                    JSONWriter.Feature.WriteNulls,
                    JSONWriter.Feature.NotWriteDefaultValue,
                    JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                    JSONWriter.Feature.WriteNameAsSymbol,
                    JSONWriter.Feature.BeanToArray
            );
//
//            furyBytes = fury.serialize(mc);
//            furyCompatibleBytes = furyCompatible.serialize(mc);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2JSONB(Blackhole bh) {
        bh.consume(
                JSONB.parseObject(
                        fastjson2JSONBBytes,
                        Object.class,
                        JSONReader.Feature.SupportAutoType,
                        JSONReader.Feature.IgnoreNoneSerializable,
                        JSONReader.Feature.UseDefaultConstructorAsPossible,
                        JSONReader.Feature.UseNativeObject,
                        JSONReader.Feature.FieldBased)
        );
    }
//
//    @Benchmark
//    public void furyCompatible(Blackhole bh) {
//        bh.consume(furyCompatible.deserialize(furyCompatibleBytes));
//    }

    @Benchmark
    public void fastjson2JSONB_arrayMapping(Blackhole bh) {
        bh.consume(
                JSONB.parseObject(
                        fastjson2JSONBBytes_arrayMapping,
                        Object.class,
                        JSONReader.Feature.SupportAutoType,
                        JSONReader.Feature.IgnoreNoneSerializable,
                        JSONReader.Feature.UseDefaultConstructorAsPossible,
                        JSONReader.Feature.UseNativeObject,
                        JSONReader.Feature.FieldBased,
                        JSONReader.Feature.SupportArrayToBean
                )
        );
    }
//
//    @Benchmark
//    public void fury(Blackhole bh) {
//        bh.consume(fury.deserialize(furyBytes));
//    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(EishayFuryParse.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
