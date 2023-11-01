package com.alibaba.fastjson2.benchmark.along;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.along.vo.SkillFire_S2C_Msg;
import io.fury.Fury;
import io.fury.config.Language;
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

public class AlongWriteBinaryArrayMapping {
    static SkillFire_S2C_Msg object;
    static Fury fury;

    /**
     * 从资源文件 "data/along.json" 中加载 JSON 字符串。
     * 使用 Fury 的 JSONReader 解析 JSON 字符串并将其转换为 SkillFire_S2C_Msg 对象。
     * 配置 Fury 实例。
     * */
    static {
        try {
            InputStream is = AlongWriteBinaryArrayMapping.class.getClassLoader().getResourceAsStream("data/along.json");
            String str = IOUtils.toString(is, "UTF-8");
            object = JSONReader.of(str)
                    .read(SkillFire_S2C_Msg.class);

            fury = Fury.builder()
                    .withLanguage(Language.JAVA)
                    .withRefTracking(false)
                    .requireClassRegistration(false)
                    .withNumberCompressed(true)
                    .build();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 通过 Fastjson2 将 object 序列化为字节数组，并返回字节数组的长度。
     * */
    public int jsonbSize() {
        return JSONB.toBytes(object, JSONWriter.Feature.BeanToArray).length;
    }

    @Benchmark
    public void jsonb(Blackhole bh) {
        bh.consume(JSONB.toBytes(object, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.FieldBased));
    }

    @Benchmark
    public void fury(Blackhole bh) {
        bh.consume(fury.serialize(object));
    }

    public int furySize() {
        return fury.serialize(object).length;
    }

//    @Benchmark
    public void json(Blackhole bh) {
        bh.consume(JSON.toJSONBytes(object, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.FieldBased));
    }

//    @Benchmark
    public void jsonStr(Blackhole bh) {
        bh.consume(JSON.toJSONString(object, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.FieldBased));
    }

    /**
     * 使用 JMH 的 Runner 运行性能测试。
     * */
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(AlongWriteBinaryArrayMapping.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
