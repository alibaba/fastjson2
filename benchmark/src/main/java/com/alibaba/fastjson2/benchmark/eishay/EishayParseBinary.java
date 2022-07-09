package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
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
import java.util.concurrent.TimeUnit;

public class EishayParseBinary {
    static MediaContent mc;
    static byte[] fastjson2UTF8Bytes;
    static byte[] fastjson2JSONBBytes;
    static byte[] hessianBytes;
    static byte[] javaSerializeBytes;

    static Fury fury = Fury.builder().withLanguage(Language.JAVA).build();

    static byte[] furyBytes;

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

            furyBytes = fury.serialize(mc);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fury(Blackhole bh) {
        bh.consume(fury.deserialize(furyBytes));
    }

    @Benchmark
    public void fastjson2UTF8Bytes(Blackhole bh) {
        bh.consume(JSON.parseObject(fastjson2UTF8Bytes, MediaContent.class));
    }

    @Benchmark
    public void fastjson2JSONB(Blackhole bh) {
        bh.consume(JSONB.parseObject(fastjson2JSONBBytes, MediaContent.class));
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

    public void fastjson2_jsonb_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_jsonb_perf();
        }
    }

    public static void fastjson2_jsonb_perf() {
        Blackhole bh = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            bh.consume(JSONB.parseObject(fastjson2JSONBBytes, MediaContent.class));
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
        // zulu11.52.13 : 409
        // zulu17.32.13 : 392
        // zulu8.58.0.13 : 318
    }

    public void hessian_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            hessian_perf();
        }
    }

    public static void hessian_perf() throws Exception {
        Blackhole bh = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            bh.consume((MediaContent) new Hessian2Input(new ByteArrayInputStream(hessianBytes)).readObject());
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu8.58.0.13 : 2576
    }

    public void javaSerialize_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            javaSerialize_perf();
        }
    }

    public static void javaSerialize_perf() throws Exception {
        Blackhole bh = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            bh.consume((MediaContent) new ObjectInputStream(new ByteArrayInputStream(javaSerializeBytes)).readObject());
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu8.58.0.13 : 2576
    }

    public static void main(String[] args) throws Exception {
//        MediaContent mc1 = JSONB.parseObject(fastjson2JSONBBytes, MediaContent.class);
//        MediaContent mc2 = JSON.parseObject(fastjson2UTF8Bytes, MediaContent.class);
//        MediaContent mc3 = (MediaContent) new Hessian2Input(new ByteArrayInputStream(hessianBytes)).readObject();
//        MediaContent mc4 = (MediaContent) new ObjectInputStream(new ByteArrayInputStream(javaSerializeBytes)).readObject();

//        new EishayParseBinary().fastjson2_jsonb_perf_test();
//        new EishayParseBinary().hessian_perf_test();
//        new EishayParseBinary().javaSerialize_perf_test();

        Options options = new OptionsBuilder()
                .include(EishayParseBinary.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
