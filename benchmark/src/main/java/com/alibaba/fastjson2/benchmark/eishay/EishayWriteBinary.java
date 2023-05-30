package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.alibaba.fastjson2.benchmark.protobuf.MediaContentTransform;
import com.caucho.hessian.io.Hessian2Output;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.msgpack.jackson.dataformat.MessagePackFactory;
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
    static ObjectMapper msgpackMapper = new ObjectMapper(new MessagePackFactory());

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

//    @Benchmark
    public void fastjson2UTF8Bytes(Blackhole bh) {
        bh.consume(JSON.toJSONBytes(mc));
    }

    public int jsonbSize() {
        return JSONB.toBytes(mc).length;
    }

    @Benchmark
    public void jsonb(Blackhole bh) {
        bh.consume(JSONB.toBytes(mc));
    }

//    @Benchmark
    public void javaSerialize(Blackhole bh) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(mc);
        objectOutputStream.flush();
        bh.consume(byteArrayOutputStream.toByteArray());
    }

//    @Benchmark
    public void hessian(Blackhole bh) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
        hessian2Output.writeObject(mc);
        hessian2Output.flush();
        bh.consume(byteArrayOutputStream.toByteArray());
    }

    @Benchmark
    public void msgpack(Blackhole bh) throws Exception {
        bh.consume(msgpackMapper.writeValueAsBytes(mc));
    }

    public int msgpackSize() throws Exception {
        return msgpackMapper.writeValueAsBytes(mc).length;
    }

    @Benchmark
    public void protobuf(Blackhole bh) throws Exception {
        bh.consume(
                MediaContentTransform.forward(mc)
                        .toByteArray()
        );
    }

    public int protobufSize() throws Exception {
        return MediaContentTransform.forward(mc)
                .toByteArray().length;
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayWriteBinary.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
