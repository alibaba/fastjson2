package com.alibaba.fastjson2.benchmark.wast;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NumberValueBeanCase {
    static String result;
    static Random random = new Random();
    static ObjectMapper mapper = new ObjectMapper();

    static {
        NumberValueBean numberValueBean = new NumberValueBean();
        numberValueBean.setValue1(random.nextInt());
        numberValueBean.setValue2(random.nextInt());
        numberValueBean.setValue3(random.nextLong());
        numberValueBean.setValue4(random.nextLong());
        numberValueBean.setValue5(random.nextFloat());
        numberValueBean.setValue6(random.nextFloat());
        numberValueBean.setValue7(random.nextDouble());
        numberValueBean.setValue8(random.nextDouble());
        numberValueBean.setValue9(123456.789e102);
        numberValueBean.setValue10(123456.789e-102);

        result = io.github.wycst.wast.json.JSON.toJsonString(numberValueBean);
    }

    @Benchmark
    public void jackson(Blackhole bh) throws JsonProcessingException {
        bh.consume(mapper.readValue(result, NumberValueBean.class));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.parseObject(result, NumberValueBean.class));
    }

    @Benchmark
    public void wastjson(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(result, NumberValueBean.class));
    }

    public static void main(String[] args) throws RunnerException {
//        io.github.wycst.wast.json.JSON.parseObject(result, NumberValueBean.class);
        Options options = new OptionsBuilder()
                .include(NumberValueBeanCase.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
