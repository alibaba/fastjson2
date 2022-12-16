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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class JSONPathMultiBenchmark2 {
    private static String str;

    static String[] paths = {
            "$.media.bitrate",
            "$.media.duration",
            "$.media.format",
            "$.media.height",
            "$.media.persons",
            "$.media.player",
            "$.media.size",
            "$.media.title",
            "$.media.uri",
            "$.media.width"
    };
    static Type[] types = {
            Integer.class,
            Long.class,
            String.class,
            Integer.class,
            String[].class,
            String.class,
            Long.class,
            String.class,
            String.class,
            Long.class
    };

    static JSONPath jsonPathMulti = JSONPath.of(
            paths,
            types
    );
    static List<JSONPath> jsonPaths = Arrays.stream(paths)
            .map(JSONPath::of)
            .collect(Collectors.toList());

    static {
        try {
            InputStream is = JSONPathPerf.class.getClassLoader().getResourceAsStream("data/eishay.json");
            str = IOUtils.toString(is, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void extract(Blackhole bh) throws Exception {
        bh.consume(
                jsonPathMulti.extract(str)
        );
    }

    @Benchmark
    public void eval(Blackhole bh) throws Exception {
        JSONObject object = JSON.parseObject(str);
        bh.consume(
                jsonPathMulti.eval(object)
        );
    }

    @Benchmark
    public void evalMulti(Blackhole bh) throws Exception {
        JSONObject object = JSON.parseObject(str);
        Object[] values = new Object[jsonPaths.size()];
        for (int i = 0; i < values.length; i++) {
            JSONPath jsonPath = jsonPaths.get(i);
            Object evalResult = jsonPath.eval(object);
            values[i] = TypeUtils.cast(evalResult, types[i]);
        }
        bh.consume(values);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(JSONPathMultiBenchmark2.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
