package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.eishay.gen.EishayClassGen;
import com.alibaba.fastjson2.util.DynamicClassLoader;
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

public class EishayFuryWriteNoneCache {
    static final Class[] classes = new Class[10_000];
    static final Object[] objects = new Object[classes.length];
    static int index;

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
            InputStream is = EishayFuryWriteNoneCache.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");

            DynamicClassLoader classLoader = DynamicClassLoader.getInstance();
            EishayClassGen gen = new EishayClassGen();
            for (int i = 0; i < classes.length; i++) {
                Class objectClass = gen.genMedia(classLoader, "com/alibaba/fastjson2/benchmark/eishay" + i);
                classes[i] = objectClass;
                objects[i] = JSONReader.of(str).read(objectClass);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2JSONB(Blackhole bh) {
        Object object = objects[(index++) % objects.length];
        bh.consume(
                JSONB.toBytes(object, context)
        );
    }

//    @Benchmark
    public void fury(Blackhole bh) {
//        Object object = objects[(index++) % objects.length];
//        byte[] bytes = fury.serialize(object);
//        bh.consume(bytes);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayFuryWriteNoneCache.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
