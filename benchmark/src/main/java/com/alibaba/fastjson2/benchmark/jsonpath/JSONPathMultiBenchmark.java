package com.alibaba.fastjson2.benchmark.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.util.TypeUtils;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class JSONPathMultiBenchmark {
    private static String str;

    static JSONPath jsonPath;

    static JSONPath path0;
    static JSONPath path1;
    static {
        try {
            InputStream is = JSONPathPerf.class.getClassLoader().getResourceAsStream("data/path_02.json");
            str = IOUtils.toString(is, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        jsonPath = JSONPath.of(
                new String[] {"$.store.bicycle.color", "$.store.bicycle.price"},
                new Type[] {String.class, BigDecimal.class}
        );

        path0 = JSONPath.of("$.store.bicycle.color");
        path1 = JSONPath.of("$.store.bicycle.price");
    }

    @Benchmark
    public void extract(Blackhole bh) throws Exception {
        bh.consume(
                jsonPath.extract(str)
        );
    }

    @Benchmark
    public void eval(Blackhole bh) throws Exception {
        JSONObject object = JSON.parseObject(str);
        bh.consume(
                jsonPath.eval(object)
        );
    }

    @Benchmark
    public void evalMulti(Blackhole bh) throws Exception {
        JSONObject object = JSON.parseObject(str);
        Object[] values = new Object[2];
        values[0] = TypeUtils.cast(path0.eval(object), String.class);
        values[1] = TypeUtils.cast(path1.eval(object), BigDecimal.class);
        bh.consume(values);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(JSONPathMultiBenchmark.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
