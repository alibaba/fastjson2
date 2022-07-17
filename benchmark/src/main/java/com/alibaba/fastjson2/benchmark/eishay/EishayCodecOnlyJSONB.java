package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
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

public class EishayCodecOnlyJSONB {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");

    static MediaContent mc;
    static byte[] jsonbBytes;
    static byte[] jsonbBytesArrayMapping;

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
            JSONWriter.Feature.NotWriteDefaultValue,
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
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
    @Benchmark
    public void deserialize_jsonb(Blackhole bh) {
        MediaContent obj = (MediaContent) JSONB.parseObject(jsonbBytes, Object.class, jsonbReaderFeatures);
        bh.consume(obj);
    }

    @Benchmark
    public void deserialize_jsonbArrayMapping(Blackhole bh) {
        MediaContent obj = (MediaContent) JSONB.parseObject(jsonbBytesArrayMapping, Object.class, jsonbReaderFeatures);
        bh.consume(obj);
    }

    @Benchmark
    public void serialize_jsonb(Blackhole bh) {
        byte[] bytes = JSONB.toBytes(mc, jsonbWriteFeatures);
        bh.consume(bytes);
    }

    @Benchmark
    public void serialize_jsonb_arrayMapping(Blackhole bh) {
        byte[] bytes = JSONB.toBytes(mc, jsonbWriteFeaturesArrayMapping);
        bh.consume(bytes);
    }

    public void serialize_jsonb_arrayMapping_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            serialize_jsonb_arrayMapping(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("serialize_jsonb_arrayMapping_perf millis : " + millis);
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu8.58.0.13 : 263
    }

    public void serialize_jsonb_arrayMapping_perf_test() {
        for (int i = 0; i < 10; i++) {
            serialize_jsonb_arrayMapping_perf();
        }
    }

    public void deserialize_jsonbArrayMapping_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            deserialize_jsonbArrayMapping(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("deserialize_jsonbArrayMapping_perf millis : " + millis);
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu8.58.0.13 : 310 261
    }

    public void deserialize_jsonbArrayMapping_perf_test() {
        for (int i = 0; i < 10; i++) {
            deserialize_jsonbArrayMapping_perf(); //
        }
    }

    public static void main(String[] args) throws RunnerException {
//        new EishayCodecOnlyJSONB().serialize_jsonb_arrayMapping_perf_test();
//        new EishayCodecOnlyJSONB().deserialize_jsonbArrayMapping_perf_test();
        Options options = new OptionsBuilder()
                .include(EishayCodecOnlyJSONB.class.getName())
                .mode(Mode.Throughput)
                .warmupIterations(3)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
