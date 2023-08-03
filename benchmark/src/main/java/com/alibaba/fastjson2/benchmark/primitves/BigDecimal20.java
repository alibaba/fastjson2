package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.primitves.vo.BigDecimal20Field;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class BigDecimal20 {
    static String str;
    static BigDecimal20Field bean;
    static byte[] jsonbBytes;
    static ObjectMapper mapper = new ObjectMapper();
    static Gson gson = new Gson();
    static Kryo kryo;
    static byte[] kryoBytes;

    static byte[] hessianBytes;

//    static io.fury.ThreadSafeFury furyCompatible = io.fury.Fury.builder()
//            .withLanguage(io.fury.Language.JAVA)
//            .withReferenceTracking(true)
//            .disableSecureMode()
//            .withCompatibleMode(io.fury.serializers.CompatibleMode.COMPATIBLE)
//            .buildThreadSafeFury();
//    static byte[] furyCompatibleBytes;

    public BigDecimal20() {
        try {
            InputStream is = BigDecimal20.class.getClassLoader().getResourceAsStream("data/dec20.json");
            str = IOUtils.toString(is, "UTF-8");
            bean = JSON.parseObject(str, BigDecimal20Field.class);
            jsonbBytes = JSONB.toBytes(bean);

            kryo = new Kryo();
            kryo.register(BigDecimal20Field.class);
            kryo.register(BigDecimal.class);

            Output output = new Output(1024, -1);
            kryo.writeObject(output, bean);
            kryoBytes = output.toBytes();

            {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
                hessian2Output.writeObject(bean);
                hessian2Output.flush();
                hessianBytes = byteArrayOutputStream.toByteArray();
            }

//            furyCompatibleBytes = furyCompatible.serialize(bean);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(
                com.alibaba.fastjson.JSON.parseObject(str, BigDecimal20Field.class)
        );
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(
                JSON.parseObject(str, BigDecimal20Field.class)
        );
    }

    public void fastjson2_ser(Blackhole bh) {
        bh.consume(
                JSON.toJSONBytes(bean, JSONWriter.Feature.BeanToArray)
        );
    }

    @Benchmark
    public void jsonb(Blackhole bh) {
        bh.consume(
                JSONB.parseObject(jsonbBytes, BigDecimal20Field.class)
        );
    }

    @Benchmark
    public void kryo(Blackhole bh) {
        Input input = new Input(kryoBytes);
        BigDecimal20Field object = kryo.readObject(input, BigDecimal20Field.class);
        bh.consume(object);
    }

    @Benchmark
    public void hessian(Blackhole bh) throws Exception {
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(hessianBytes);
        Hessian2Input hessian2Input = new Hessian2Input(bytesIn);
        bh.consume(hessian2Input.readObject());
    }

//    @Benchmark
    public void fury(Blackhole bh) {
//        Object object = furyCompatible.deserialize(furyCompatibleBytes);
//        bh.consume(object);
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(
                mapper.readValue(str, BigDecimal20Field.class)
        );
    }

    @Benchmark
    public void gson(Blackhole bh) throws Exception {
        bh.consume(
                gson.fromJson(str, BigDecimal20Field.class)
        );
    }

    @Benchmark
    public void wastjson(Blackhole bh) throws Exception {
        bh.consume(
                io.github.wycst.wast.json.JSON.parseObject(str, BigDecimal20Field.class)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(BigDecimal20.class.getName())
                .exclude(BigDecimal20Tree.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
