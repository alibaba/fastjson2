package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.*;
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

public class EishayWriteUTF8BytesTree {
    static JSONObject mc;
    static final ObjectMapper mapper = new ObjectMapper();

    static {
        try {
            InputStream is = EishayWriteUTF8BytesTree.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = (JSONObject) JSONReader.of(str)
                    .readAny();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.toJSONBytes(mc));
    }

    public void fastjson2_ReferenceDetection(Blackhole bh) {
        bh.consume(JSON.toJSONString(mc, JSONWriter.Feature.ReferenceDetection));
    }

//    @Benchmark
    public void fastjson2_jsonb(Blackhole bh) {
        bh.consume(JSONB.toBytes(mc));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.writeValueAsBytes(mc));
    }

    //    @Benchmark
    public void wastjson(Blackhole bh) throws Exception {
        bh.consume(
                io.github.wycst.wast.json.JSON.toJsonBytes(mc)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayWriteUTF8BytesTree.class.getName())
                .mode(Mode.Throughput)
                .warmupIterations(3)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
