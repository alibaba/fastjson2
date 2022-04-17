package com.alibaba.fastjson_perf.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson_perf.Int100Test;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jayway.jsonpath.JsonPath;

public class JSONPathPerf {
    private static String str;

    static {
        try {
            InputStream is = Int100Test.class.getClassLoader().getResourceAsStream("data/path_02.json");
            str = IOUtils.toString(is, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void perf_extract_id_test() throws Exception {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                fastjsonReaderAuthors();
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println(millis); // 447 438 361
        }

    }

    @Benchmark
    public void fastjsonReaderAuthors() {
        JSONPath.extract(str, "$.store.book[*].author");
    }

    @Benchmark
    public void jaywayReadAuthors() {
        List<String> authors = JsonPath.read(str, "$.store.book[*].author");
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(JSONPathPerf.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
