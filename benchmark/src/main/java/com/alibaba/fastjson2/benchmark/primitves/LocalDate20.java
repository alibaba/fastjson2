package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.benchmark.primitves.vo.LocalDate20Field;
import com.alibaba.fastjson2.benchmark.primitves.vo.LocalDateTime20Field;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class LocalDate20 {
    static String str = "{\n" +
            "  \"v0000\" : \"2001-07-01\",\n" +
            "  \"v0001\" : \"2011-06-02\",\n" +
            "  \"v0002\" : \"2021-11-03\",\n" +
            "  \"v0003\" : \"2001-11-14\",\n" +
            "  \"v0004\" : \"2002-10-07\",\n" +
            "  \"v0005\" : \"2003-09-12\",\n" +
            "  \"v0006\" : \"2006-08-16\",\n" +
            "  \"v0007\" : \"2002-01-30\",\n" +
            "  \"v0008\" : \"2009-02-27\",\n" +
            "  \"v0009\" : \"2011-04-26\",\n" +
            "  \"v0010\" : \"2012-06-23\",\n" +
            "  \"v0011\" : \"2022-02-18\",\n" +
            "  \"v0012\" : \"2021-08-17\",\n" +
            "  \"v0013\" : \"2021-01-17\",\n" +
            "  \"v0014\" : \"2020-03-14\",\n" +
            "  \"v0015\" : \"2019-02-14\",\n" +
            "  \"v0016\" : \"2018-12-14\",\n" +
            "  \"v0017\" : \"2007-10-14\",\n" +
            "  \"v0018\" : \"2008-02-14\",\n" +
            "  \"v0019\" : \"2011-03-14\"\n" +
            "}\n";

    static ObjectMapper mapper = new ObjectMapper();

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(
                JSON.parseObject(str, LocalDate20Field.class, "yyyy-MM-dd")
        );
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(
                com.alibaba.fastjson.JSON.parseObject(str, LocalDateTime20Field.class)
        );
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(
                mapper.readValue(str, LocalDate20Field.class)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(LocalDate20.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
