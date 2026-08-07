package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import org.apache.commons.io.IOUtils;
import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class EishayForyCompatibleWrite {
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

    static ThreadSafeFory foryCompatible = Fory.builder()
            .withLanguage(org.apache.fory.config.Language.JAVA)
            .withRefTracking(true)
            .requireClassRegistration(false)
            .withCompatibleMode(org.apache.fory.config.CompatibleMode.COMPATIBLE)
            .buildThreadSafeFory();

    static {
        try {
            InputStream is = EishayForyCompatibleWrite.class.getClassLoader().getResourceAsStream("data/eishay.json");
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
    public void fory(Blackhole bh) {
        byte[] bytes = foryCompatible.serialize(mc);
        bh.consume(bytes);
    }

    public int forySize() {
        return foryCompatible.serialize(mc).length;
//        return 0;
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayForyCompatibleWrite.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
