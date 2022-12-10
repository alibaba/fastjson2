package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.SymbolTable;
import com.alibaba.fastjson2.benchmark.eishay.vo.Image;
import com.alibaba.fastjson2.benchmark.eishay.vo.Media;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class EishayParseBinaryAutoType {
    static final SymbolTable symbolTable = JSONB.symbolTable(
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

    static MediaContent mc;
    static byte[] fastjson2JSONBBytes;
    static byte[] fastjson2JSONBBytes_arrayMapping;
    static byte[] fastjson2JSONBBytes_symbols;
    static byte[] hessianBytes;
    static byte[] javaSerializeBytes;

    static JSONReader.AutoTypeBeforeHandler autoTypeFilter = JSONReader.autoTypeFilter(true, Media.class, MediaContent.class, Image.class);

    static {
        try {
            InputStream is = EishayParseBinaryAutoType.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);

            fastjson2JSONBBytes = JSONB.toBytes(mc, JSONWriter.Feature.WriteClassName,
                    JSONWriter.Feature.IgnoreNoneSerializable,
                    JSONWriter.Feature.FieldBased,
                    JSONWriter.Feature.ReferenceDetection,
                    JSONWriter.Feature.WriteNulls,
                    JSONWriter.Feature.NotWriteDefaultValue,
                    JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                    JSONWriter.Feature.WriteNameAsSymbol);

            fastjson2JSONBBytes_arrayMapping = JSONB.toBytes(mc, JSONWriter.Feature.WriteClassName,
                    JSONWriter.Feature.IgnoreNoneSerializable,
                    JSONWriter.Feature.FieldBased,
                    JSONWriter.Feature.ReferenceDetection,
                    JSONWriter.Feature.WriteNulls,
                    JSONWriter.Feature.NotWriteDefaultValue,
                    JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                    JSONWriter.Feature.BeanToArray
            );

            fastjson2JSONBBytes_symbols = JSONB.toBytes(
                    mc,
                    symbolTable,
                    JSONWriter.Feature.WriteClassName,
                    JSONWriter.Feature.IgnoreNoneSerializable,
                    JSONWriter.Feature.FieldBased,
                    JSONWriter.Feature.ReferenceDetection,
                    JSONWriter.Feature.WriteNulls,
                    JSONWriter.Feature.NotWriteDefaultValue,
                    JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                    JSONWriter.Feature.WriteNameAsSymbol
                    );

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
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2JSONB(Blackhole bh) {
        bh.consume(
                JSONB.parseObject(
                        fastjson2JSONBBytes,
                        Object.class,
                        JSONReader.Feature.SupportAutoType,
                        JSONReader.Feature.IgnoreNoneSerializable,
                        JSONReader.Feature.UseDefaultConstructorAsPossible,
                        JSONReader.Feature.UseNativeObject,
                        JSONReader.Feature.FieldBased)
        );
    }

    @Benchmark
    public void fastjson2JSONB_autoTypeFilter(Blackhole bh) {
        bh.consume(
                JSONB.parseObject(
                        fastjson2JSONBBytes,
                        Object.class,
                        autoTypeFilter,
                        JSONReader.Feature.IgnoreNoneSerializable,
                        JSONReader.Feature.UseDefaultConstructorAsPossible,
                        JSONReader.Feature.UseNativeObject,
                        JSONReader.Feature.FieldBased)
        );
    }

    public void fastjson2JSONB_symbols(Blackhole bh) {
        bh.consume(
                JSONB.parseObject(
                        fastjson2JSONBBytes_symbols,
                        Object.class,
                        symbolTable,
                        JSONReader.Feature.SupportAutoType,
                        JSONReader.Feature.IgnoreNoneSerializable,
                        JSONReader.Feature.UseDefaultConstructorAsPossible,
                        JSONReader.Feature.UseNativeObject,
                        JSONReader.Feature.FieldBased)
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

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(EishayParseBinaryAutoType.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
