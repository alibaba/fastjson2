package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.eishay.vo.Image;
import com.alibaba.fastjson2.benchmark.eishay.vo.Media;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.alibaba.fastjson2.benchmark.protobuf.MediaContentHolder;
import com.alibaba.fastjson2.benchmark.protobuf.MediaContentTransform;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.fury.Fury;
import io.fury.Language;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class EishayParseBinaryArrayMapping {
    static final Fury fury = Fury.builder().withLanguage(Language.JAVA)
            .withRefTracking(false)
            .requireClassRegistration(false)
            .withNumberCompressed(true)
            .build();

    static MediaContent mediaContent;
    static byte[] fastjson2JSONBBytes;
    static byte[] kryoBytes;

    static byte[] protobufBytes;
    static byte[] furyBytes;

    private static final ThreadLocal<Kryo> kryos = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(MediaContent.class);
        kryo.register(ArrayList.class);
        kryo.register(Image.class);
        kryo.register(Image.Size.class);
        kryo.register(Media.class);
        kryo.register(Media.Player.class);
        return kryo;
    });

    static {
        try {
            InputStream is = EishayParseBinaryArrayMapping.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mediaContent = JSONReader.of(str)
                    .read(MediaContent.class);

            fastjson2JSONBBytes = JSONB.toBytes(mediaContent, JSONWriter.Feature.BeanToArray);

            Kryo kryo = new Kryo();
            kryo.register(MediaContent.class);
            kryo.register(ArrayList.class);
            kryo.register(Image.class);
            kryo.register(Image.Size.class);
            kryo.register(Media.class);
            kryo.register(Media.Player.class);

            Output output = new Output(1024, -1);
            kryo.writeObject(output, mediaContent);
            kryoBytes = output.toBytes();

            protobufBytes = MediaContentTransform.forward(mediaContent).toByteArray();
            furyBytes = MediaContentTransform.forward(mediaContent).toByteArray();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fury(Blackhole bh) {
        bh.consume(
                fury.deserializeJavaObject(furyBytes, MediaContent.class)
        );
    }

    @Benchmark
    public void jsonb(Blackhole bh) {
        bh.consume(
                JSONB.parseObject(fastjson2JSONBBytes, MediaContent.class, JSONReader.Feature.SupportArrayToBean)
        );
    }

    @Benchmark
    public void protobuf(Blackhole bh) throws Exception {
        MediaContentHolder.MediaContent protobufObject = MediaContentHolder.MediaContent.parseFrom(protobufBytes);
        MediaContent object = MediaContentTransform.reverse(protobufObject);
        bh.consume(object);
    }

    @Benchmark
    public void kryo(Blackhole bh) throws Exception {
        Input input = new Input(kryoBytes);
        bh.consume(
                kryos.get().readObject(input, MediaContent.class)
        );
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(EishayParseBinaryArrayMapping.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
