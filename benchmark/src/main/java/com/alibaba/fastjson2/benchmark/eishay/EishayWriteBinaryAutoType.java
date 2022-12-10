package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.caucho.hessian.io.Hessian2Output;
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

public class EishayWriteBinaryAutoType {
    static MediaContent mc;
    static SymbolTable symbolTable = JSONB.symbolTable(
            "com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent",
            "media",
            "images",
            "height",
            "size",
            "title",
            "uri",
            "width",
            "bitrate",
            "duration",
            "format",
            "persons",
            "player"
    );

    static {
        try {
            InputStream is = EishayWriteBinaryAutoType.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2UTF8Bytes(Blackhole bh) {
        bh.consume(JSON.toJSONBytes(mc, JSONWriter.Feature.WriteClassName));
    }

    @Benchmark
    public void fastjson2JSONB(Blackhole bh) {
        bh.consume(
                JSONB.toBytes(
                        mc,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.IgnoreNoneSerializable,
                        JSONWriter.Feature.FieldBased,
                        JSONWriter.Feature.ReferenceDetection,
                        JSONWriter.Feature.WriteNulls,
                        JSONWriter.Feature.NotWriteDefaultValue,
                        JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                        JSONWriter.Feature.WriteNameAsSymbol)
        );
    }

    public void fastjson2JSONB_symbols(Blackhole bh) {
        bh.consume(
                JSONB.toBytes(
                        mc,
                        symbolTable,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.IgnoreNoneSerializable,
                        JSONWriter.Feature.FieldBased,
                        JSONWriter.Feature.ReferenceDetection,
                        JSONWriter.Feature.WriteNulls,
                        JSONWriter.Feature.NotWriteDefaultValue,
                        JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                        JSONWriter.Feature.WriteNameAsSymbol)
        );
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

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayWriteBinaryAutoType.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
