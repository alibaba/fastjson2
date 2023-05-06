package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.benchmark.primitves.vo.OffsetDateTime20Field;
import com.fasterxml.jackson.databind.ObjectMapper;
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

public class OffsetDateTime20 {
    static ObjectMapper mapper = new ObjectMapper();
    static OffsetDateTime20Field object = new OffsetDateTime20Field();

    public OffsetDateTime20() {
        try {
            InputStream is = OffsetDateTime20.class.getClassLoader().getResourceAsStream("data/date20.json");
            String str = IOUtils.toString(is, "UTF-8");
            object = JSON.parseObject(str, OffsetDateTime20Field.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        String str = JSON.toJSONString(object);
        bh.consume(
                JSON.parseObject(str, OffsetDateTime20Field.class)
        );
    }

    @Benchmark
    public void fastjson2_jsonb(Blackhole bh) {
        byte[] jsonbBytes = JSONB.toBytes(object);
        bh.consume(
                JSONB.parseObject(jsonbBytes, OffsetDateTime20Field.class)
        );
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        String str = mapper.writeValueAsString(object);
        bh.consume(
                mapper.readValue(str, OffsetDateTime20Field.class)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(OffsetDateTime20.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
