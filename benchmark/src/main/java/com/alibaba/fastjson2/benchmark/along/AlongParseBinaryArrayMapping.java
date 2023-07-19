package com.alibaba.fastjson2.benchmark.along;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.along.vo.SkillFire_S2C_Msg;
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
    static SkillFire_S2C_Msg mc;
    static byte[] fastjson2JSONBBytes;

    static {
        try {
            InputStream is = AlongParseBinaryArrayMapping.class.getClassLoader().getResourceAsStream("data/along.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str).read(SkillFire_S2C_Msg.class);
            fastjson2JSONBBytes = JSONB.toBytes(mc, JSONWriter.Feature.BeanToArray);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void jsonb(Blackhole bh) {
        bh.consume(JSONB.parseObject(fastjson2JSONBBytes, SkillFire_S2C_Msg.class, SupportArrayToBean, FieldBased));
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
