package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.caucho.hessian.io.Hessian2Output;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;

public class EishayWriteBinary {
    static MediaContent mc;
    static Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
    static Blackhole bh = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");

    static {
        try {
            InputStream is = EishayWriteBinary.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2UTF8Bytes(Blackhole bh) {
        byte[] bytes = JSON.toJSONBytes(mc);
        bh.consume(bytes);
    }

    public void fastjson2UTF8Bytes_perf() {
        for (int i = 0; i < 10; i++) {
            fastjson2UTF8Bytes(bh);
        }
    }

    @Benchmark
    public void fastjson2JSONB(Blackhole bh) {
        byte[] bytes = JSONB.toBytes(mc);
        bh.consume(bytes);
    }

    @Benchmark
    public void javaSerialize(Blackhole bh) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(mc);
        objectOutputStream.flush();
        bh.consume(byteArrayOutputStream.toByteArray());
    }

    @Benchmark
    public void hessian(Blackhole bh) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
        hessian2Output.writeObject(mc);
        hessian2Output.flush();
        bh.consume(byteArrayOutputStream.toByteArray());
    }

    @Benchmark
    public void fury(Blackhole bh) {
        byte[] bytes = fury.serialize(mc);
        bh.consume(fury.deserialize(bytes));
    }

    public void fury_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            fury(bh);
        }

        long millis = System.currentTimeMillis() - start;
        System.out.println("fury : " + millis);
    }

    public void fury_perf_test() {
        for (int i = 0; i < 10; i++) {
            fury_perf();
        }
    }

    //    @Test
    public void fastjson2_jsonb_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2JSONB(bh);
        }
    }

    //    @Test
    public void fastjson2UTF8Bytes_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2UTF8Bytes_perf();
        }
    }

    public static void fastjson2_jsonb_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            JSONB.toBytes(mc, JSONWriter.Feature.FieldBased);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("EishayWriteJSONB : " + millis);
        // zulu17.32.13 :
        // zulu11.52.13 :
        // zulu8.58.0.13 :
    }

    public static void main(String[] args) throws RunnerException {
//        new EishayWriteBinary().fury_perf_test();
//        new EishayWriteBinary().fastjson2_jsonb_perf_test();
//        new EishayWriteBinary().fastjson2UTF8Bytes_perf_test();
        Options options = new OptionsBuilder()
                .include(EishayWriteBinary.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
