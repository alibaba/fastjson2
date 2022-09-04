package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.benchmark.eishay.vo.Image;
import com.alibaba.fastjson2.benchmark.eishay.vo.Media;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
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

public class EishayParseBinary {
    static MediaContent mc;
    static byte[] fastjson2UTF8Bytes;
    static byte[] fastjson2JSONBBytes;
    static byte[] hessianBytes;
    static byte[] javaSerializeBytes;
    static Kryo kryo;
    static byte[] kryoBytes;

    static JSONReader.Context context = JSONFactory.createReadContext();

    static {
        try {
            InputStream is = EishayParseBinary.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);

            fastjson2UTF8Bytes = JSON.toJSONBytes(mc);
            fastjson2JSONBBytes = JSONB.toBytes(mc);

            {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
                hessian2Output.writeObject(mc);
                hessian2Output.flush();
                hessianBytes = byteArrayOutputStream.toByteArray();
            }
            {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(mc);
                objectOutputStream.flush();
                javaSerializeBytes = byteArrayOutputStream.toByteArray();
            }

            kryo = new Kryo();
            kryo.register(MediaContent.class);
            kryo.register(ArrayList.class);
            kryo.register(Image.class);
            kryo.register(Image.Size.class);
            kryo.register(Media.class);
            kryo.register(Media.Player.class);

            Output output = new Output(1024, -1);
            kryo.writeObject(output, mc);
            kryoBytes = output.toBytes();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2UTF8Bytes(Blackhole bh) {
        bh.consume(
                JSON.parseObject(fastjson2UTF8Bytes, MediaContent.class)
        );
    }

    @Benchmark
    public void fastjson2JSONB(Blackhole bh) {
        bh.consume(
                JSONB.parseObject(fastjson2JSONBBytes, MediaContent.class)
        );
    }

    @Benchmark
    public void javaSerialize(Blackhole bh) throws Exception {
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(javaSerializeBytes);
        ObjectInputStream objectIn = new ObjectInputStream(bytesIn);
        bh.consume(objectIn.readObject());
    }

    @Benchmark
    public void hessian(Blackhole bh) throws Exception {
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(hessianBytes);
        Hessian2Input hessian2Input = new Hessian2Input(bytesIn);
        bh.consume(hessian2Input.readObject());
    }

    public void kryo(Blackhole bh) throws Exception {
        Input input = new Input(kryoBytes);
        bh.consume(
                kryo.readObject(input, MediaContent.class)
        );
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(EishayParseBinary.class.getName())
                .exclude(EishayParseBinaryAutoType.class.getName())
                .exclude(EishayParseBinaryArrayMapping.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
