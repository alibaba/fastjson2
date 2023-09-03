package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.eishay.vo.Image;
import com.alibaba.fastjson2.benchmark.eishay.vo.Media;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.alibaba.fastjson2.benchmark.protobuf.MediaContentTransform;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
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
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class EishayWriteBinaryArrayMapping {
    static final Fury fury = Fury.builder().withLanguage(Language.JAVA)
            .withRefTracking(false)
            .requireClassRegistration(false)
            .withNumberCompressed(true)
            .build();

    static MediaContent mediaContent;
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

    private static final ThreadLocal<Output> outputs = ThreadLocal.withInitial(() -> new Output(1024, -1));

    static {
        try {
            InputStream is = EishayWriteBinaryArrayMapping.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mediaContent = JSONReader.of(str)
                    .read(MediaContent.class);

            Kryo kryo = new Kryo();
            kryo.register(MediaContent.class);
            kryo.register(ArrayList.class);
            kryo.register(Image.class);
            kryo.register(Image.Size.class);
            kryo.register(Media.class);
            kryo.register(Media.Player.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public int furySize() {
        return fury.serialize(mediaContent).length;
    }

    @Benchmark
    public void fury(Blackhole bh) {
        bh.consume(
                fury.serialize(mediaContent)
        );
    }

    public int jsonbSize() {
        return JSONB.toBytes(mediaContent, JSONWriter.Feature.BeanToArray).length;
    }

    @Benchmark
    public void jsonb(Blackhole bh) {
        bh.consume(
                JSONB.toBytes(mediaContent, JSONWriter.Feature.BeanToArray)
        );
    }

    public int kryoSize() {
        Output output = outputs.get();
        output.reset();
        kryos.get().writeObject(output, mediaContent);
        return output.toBytes().length;
    }

    @Benchmark
    public void kryo(Blackhole bh) throws Exception {
        Output output = outputs.get();
        output.reset();
        kryos.get().writeObject(output, mediaContent);
        bh.consume(output.toBytes());
    }

    public int protobufSize() {
        return MediaContentTransform.forward(mediaContent).toByteArray().length;
    }

    @Benchmark
    public void protobuf(Blackhole bh) throws Exception {
        byte[] bytes = MediaContentTransform.forward(mediaContent).toByteArray();
        bh.consume(bytes);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayWriteBinaryArrayMapping.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
