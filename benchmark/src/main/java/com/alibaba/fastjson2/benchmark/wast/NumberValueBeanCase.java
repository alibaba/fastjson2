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

import java.util.concurrent.TimeUnit;

public class NumberValueBeanCase {
    static String result;
    static ObjectMapper mapper = new ObjectMapper();

    static {
        NumberValueBean numberValueBean = new NumberValueBean();
        numberValueBean.setValue1(-1547783865);
        numberValueBean.setValue2(-764506995);
        numberValueBean.setValue3(-3476207302658863324L);
        numberValueBean.setValue4(-1673529357825104963L);
        numberValueBean.setValue5(0.36136854F);
        numberValueBean.setValue6(0.9946881F);
        numberValueBean.setValue7(0.194469629135542);
        numberValueBean.setValue8(0.18867346119788797);
        numberValueBean.setValue9(1.23456789E107);
        numberValueBean.setValue10(1.23456789E-97);

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
