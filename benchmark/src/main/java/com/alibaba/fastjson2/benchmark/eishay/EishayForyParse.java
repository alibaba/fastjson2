package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class EishayForyParse {
    static MediaContent mc;
    static JSONReader.Feature[] features = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.IgnoreNoneSerializable,
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased,
            JSONReader.Feature.SupportArrayToBean
    };

    static byte[] jsonbBytes;
    static byte[] foryBytes;

    static org.apache.fory.ThreadSafeFory fory = org.apache.fory.Fory.builder()
            .withLanguage(org.apache.fory.config.Language.JAVA)
            .requireClassRegistration(false)
            .withRefTracking(true)
            .buildThreadSafeFory();

    static {
        try {
            InputStream is = EishayForyParse.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);

            jsonbBytes = JSONB.toBytes(
                    mc,
                    EishayForyWrite.features
            );

            foryBytes = fory.serializeJavaObject(mc);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void jsonb(Blackhole bh) {
        Object object = JSONB.parseObject(jsonbBytes, Object.class, features);
        bh.consume(object);
    }

    @Benchmark
    public void fory(Blackhole bh) {
        Object object = fory.deserialize(foryBytes);
        bh.consume(object);
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(EishayForyParse.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
