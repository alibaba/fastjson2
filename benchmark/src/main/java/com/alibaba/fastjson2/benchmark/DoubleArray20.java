package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.benchmark.eishay.EishayParseTreeString;
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

public class DoubleArray20 {
    static String str;
    static ObjectMapper mapper = new ObjectMapper();

    static {
        try {
            InputStream is = EishayParseTreeString.class.getClassLoader().getResourceAsStream("data/double_array_20.json");
            str = IOUtils.toString(is, "UTF-8");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.parseObject(str, double[].class));
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu8.58.0.13 : 236.517
    }

    @Benchmark
    public void fastjson2_tree(Blackhole bh) {
        bh.consume(JSON.parse(str, JSONReader.Feature.UseBigDecimalForDoubles));
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu8.58.0.13 :
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(str, double[].class));
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu8.58.0.13 : 309.855
    }

    @Benchmark
    public void wastjson(Blackhole bh) throws Exception {
        bh.consume(
                io.github.wycst.wast.json.JSON.parseObject(str, double[].class)
        );
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu8.58.0.13 :
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DoubleArray20.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .warmupIterations(3)
                .build();
        new Runner(options).run();
    }
}
