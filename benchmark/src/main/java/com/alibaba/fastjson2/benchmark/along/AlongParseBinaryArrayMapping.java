package com.alibaba.fastjson2.benchmark.along;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.along.vo.HarmDTO;
import com.alibaba.fastjson2.benchmark.along.vo.SkillCategory;
import com.alibaba.fastjson2.benchmark.along.vo.SkillFire_S2C_Msg;
import io.fury.Fury;
import io.fury.config.Language;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson2.JSONReader.Feature.FieldBased;
import static com.alibaba.fastjson2.JSONReader.Feature.SupportArrayToBean;

public class AlongParseBinaryArrayMapping {
    static Fury fury;

    static SkillFire_S2C_Msg object;
    static byte[] fastjson2JSONBBytes;
    static byte[] furyBytes;

    static {
        try {
            InputStream is = AlongParseBinaryArrayMapping.class.getClassLoader().getResourceAsStream("data/along.json");
            String str = IOUtils.toString(is, "UTF-8");
            object = JSONReader.of(str).read(SkillFire_S2C_Msg.class);

            fury = Fury.builder().withLanguage(Language.JAVA)
                    .withRefTracking(false)
                    .requireClassRegistration(false)
                    .withNumberCompressed(true)
                    .build();

            fury.register(SkillCategory.class);
            fury.register(SkillFire_S2C_Msg.class);
            fury.register(HarmDTO.class);

            fastjson2JSONBBytes = JSONB.toBytes(object, JSONWriter.Feature.BeanToArray);
            furyBytes = fury.serializeJavaObject(object);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void jsonb(Blackhole bh) {
        bh.consume(JSONB.parseObject(fastjson2JSONBBytes, SkillFire_S2C_Msg.class, FieldBased, SupportArrayToBean));
    }

    @Benchmark
    public void fury(Blackhole bh) {
        bh.consume(fury.deserializeJavaObject(furyBytes, SkillFire_S2C_Msg.class));
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(AlongParseBinaryArrayMapping.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
