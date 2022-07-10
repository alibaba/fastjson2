package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.eishay.vo.Image;
import com.alibaba.fastjson2.benchmark.eishay.vo.Media;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import io.fury.Fury;
import io.fury.Language;
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

public class EishayCodec {
    static MediaContent mc;
    static byte[] fastjson2JSONBBytes;

    static Fury fury = Fury
            .builder()
            .withLanguage(Language.JAVA)
            .build();
    static byte[] furyBytes;

    static final JSONWriter.Feature[] jsonbWriteFeatures = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.IgnoreNoneSerializable,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol
    };

    static final JSONReader.Feature[] jsonbReaderFeatures = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.IgnoreNoneSerializable,
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased
    };

    static {
        fury.register(MediaContent.class);
        fury.register(Image.class);
        fury.register(Image.Size.class);
        fury.register(Media.class);
        fury.register(Media.Player.class);

        try {
            InputStream is = EishayParseBinary.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);

            fastjson2JSONBBytes = JSONB.toBytes(mc, JSONWriter.Feature.WriteClassName);

            furyBytes = fury.serialize(mc);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void deserialize_jsonb(Blackhole bh) {
        bh.consume(
                JSONB.parseObject(fastjson2JSONBBytes, Object.class, jsonbReaderFeatures)
        );
    }

    @Benchmark
    public void deserialize_fury(Blackhole bh) {
        bh.consume(
                fury.deserialize(furyBytes)
        );
    }

    @Benchmark
    public void serialize_jsonb(Blackhole bh) {
        byte[] bytes = JSONB.toBytes(mc, jsonbWriteFeatures);
        bh.consume(bytes);
    }

    @Benchmark
    public void serialize_fury(Blackhole bh) {
        byte[] bytes = fury.serialize(mc);
        bh.consume(bytes);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayCodec.class.getName())
                .exclude(EishayCodecOnlyJSONB.class.getName())
                .mode(Mode.Throughput)
                .warmupIterations(3)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
