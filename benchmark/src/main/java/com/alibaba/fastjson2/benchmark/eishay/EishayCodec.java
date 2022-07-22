package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
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
import java.util.concurrent.TimeUnit;

public class EishayCodec {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");

    static MediaContent mc;
    static byte[] jsonbBytes;
    static byte[] jsonbBytesArrayMapping;
    static byte[] furyBytes;

    static Fury fury = Fury
            .builder()
            .withLanguage(Language.JAVA)
            .ignoreStringReference(true)
            .disableSecureMode()
            .build();

    static final JSONWriter.Feature[] jsonbWriteFeatures = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol
    };

    static final JSONWriter.Feature[] jsonbWriteFeaturesArrayMapping = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol,
            JSONWriter.Feature.BeanToArray
    };

    static final JSONReader.Feature[] jsonbReaderFeatures = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased,
            JSONReader.Feature.SupportArrayToBean
    };

    static {
        try {
            InputStream is = EishayParseBinary.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);

            jsonbBytes = JSONB.toBytes(mc, jsonbWriteFeatures);
            jsonbBytesArrayMapping = JSONB.toBytes(mc, jsonbWriteFeaturesArrayMapping);

            MediaContent obj = (MediaContent) JSONB.parseObject(jsonbBytes, Object.class, jsonbReaderFeatures);
            if (!mc.equals(obj)) {
                throw new JSONException("not equals");
            }

            MediaContent obj2 = (MediaContent) JSONB.parseObject(jsonbBytesArrayMapping, Object.class, jsonbReaderFeatures);
            if (!mc.equals(obj2)) {
                throw new JSONException("not equals");
            }

            furyBytes = fury.serialize(mc);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

//    @Benchmark
    public void deserialize_jsonb(Blackhole bh) {
        MediaContent obj = (MediaContent) JSONB.parseObject(jsonbBytes, Object.class, jsonbReaderFeatures);
        bh.consume(obj);
    }

    @Benchmark
    public void deserialize_jsonbArrayMapping(Blackhole bh) {
        MediaContent obj = (MediaContent) JSONB.parseObject(jsonbBytesArrayMapping, Object.class, jsonbReaderFeatures);
        bh.consume(obj);
    }

    public void deserialize_jsonbArrayMapping_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            deserialize_jsonbArrayMapping(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("jsonb millis : " + millis);
        // zulu11.52.13  :
        // zulu17.32.13  :
        // zulu8.58.0.13 :
    }

    public void deserialize_jsonbArrayMapping_perf_test() {
        for (int i = 0; i < 10; i++) {
            deserialize_jsonbArrayMapping_perf();
        }
    }

    @Benchmark
    public void deserialize_fury(Blackhole bh) {
        MediaContent obj = (MediaContent) fury.deserialize(furyBytes);
        bh.consume(obj);
    }

    public void deserialize_fury_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            deserialize_fury(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fury millis : " + millis);
        // zulu11.52.13  :
        // zulu17.32.13  :
        // zulu8.58.0.13 :
    }

    public void deserialize_fury_perf_test() {
        for (int i = 0; i < 10; i++) {
            deserialize_fury_perf();
        }
    }

//    @Benchmark
    public void serialize_jsonb(Blackhole bh) {
        byte[] bytes = JSONB.toBytes(mc, jsonbWriteFeatures);
        bh.consume(bytes);
    }

    @Benchmark
    public void serialize_jsonb_arrayMapping(Blackhole bh) {
        byte[] bytes = JSONB.toBytes(mc, jsonbWriteFeaturesArrayMapping);
        bh.consume(bytes);
    }

    @Benchmark
    public void serialize_fury(Blackhole bh) {
        byte[] bytes = fury.serialize(mc);
        bh.consume(bytes);
    }

    public void serialize_fury_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            serialize_fury(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fury millis : " + millis);
        // zulu11.52.13  :
        // zulu17.32.13  :
        // zulu8.58.0.13 :
    }

    public void serialize_fury_perf_test() {
        for (int i = 0; i < 10; i++) {
            serialize_fury_perf();
        }
    }

    public void serialize_jsonb_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            serialize_jsonb(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu8.58.0.13 :
    }

    public void serialize_jsonb_arrayMapping_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            serialize_jsonb_arrayMapping(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("jsonb_arrayMapping millis : " + millis);
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu8.58.0.13 :
    }

    public void serialize_jsonb_arrayMapping_perf_test() {
        for (int i = 0; i < 10; i++) {
            serialize_jsonb_arrayMapping_perf();
        }
    }

    public static void main(String[] args) throws RunnerException {
        System.out.println("fury  size : " + fury.serialize(mc).length);
//        System.out.println("jsonb size : " + JSONB.toBytes(mc, jsonbWriteFeatures).length);
        System.out.println("jsonb_array size : " + JSONB.toBytes(mc, jsonbWriteFeaturesArrayMapping).length);

//        new EishayCodec().serialize_jsonb_arrayMapping_perf_test();
//        new EishayCodec().serialize_fury_perf_test();
//        new EishayCodec().deserialize_fury_perf_test();
//        new EishayCodec().deserialize_jsonbArrayMapping_perf_test();
        Options options = new OptionsBuilder()
                .include(EishayCodec.class.getName())
                .exclude(EishayCodecOnlyJSONB.class.getName())
                .mode(Mode.Throughput)
                .warmupIterations(3)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
