package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.eishay.vo.Image;
import com.alibaba.fastjson2.benchmark.eishay.vo.Media;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.alibaba.fastjson2.benchmark.protobuf.MediaContentTransform;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
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
    static MediaContent mc;
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
            mc = JSONReader.of(str)
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

//    @Benchmark
    public void fastjson1UTF8Bytes(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.toJSONBytes(mc, SerializerFeature.BeanToArray));
    }

    @Benchmark
    public void fastjson2UTF8Bytes(Blackhole bh) {
        bh.consume(JSON.toJSONBytes(mc, JSONWriter.Feature.BeanToArray));
    }

    public int jsonbSize() {
        return JSONB.toBytes(mc, JSONWriter.Feature.BeanToArray).length;
    }

    @Benchmark
    public void jsonb(Blackhole bh) {
        bh.consume(JSONB.toBytes(mc, JSONWriter.Feature.BeanToArray));
    }

    public int kryoSize() {
        Output output = outputs.get();
        output.reset();
        kryos.get().writeObject(output, mc);
        return output.toBytes().length;
    }

    @Benchmark
    public void kryo(Blackhole bh) throws Exception {
        Output output = outputs.get();
        output.reset();
        kryos.get().writeObject(output, mc);
        bh.consume(output.toBytes());
    }

    public int protobufSize() {
        return MediaContentTransform.forward(mc).toByteArray().length;
    }

    @Benchmark
    public void protobuf(Blackhole bh) throws Exception {
        byte[] bytes = MediaContentTransform.forward(mc).toByteArray();
        bh.consume(bytes);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayWriteBinaryArrayMapping.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(2)
                .build();
        new Runner(options).run();
    }
}
