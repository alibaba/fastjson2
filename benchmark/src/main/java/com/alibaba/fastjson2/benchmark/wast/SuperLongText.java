package com.alibaba.fastjson2.benchmark.wast;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.benchmark.LargeFile26MTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipInputStream;

/**
 * 超文本解析测试（180MB）
 * <p>文件下载地址： https://codeload.github.com/zemirco/sf-city-lots-json/zip/refs/heads/master
 *
 * author wangy
 * 2022/6/3 8:56
 */
public class SuperLongText {
    private static String result;
    static ObjectMapper mapper = new ObjectMapper();

    static {
        try (
                InputStream fis = LargeFile26MTest.class.getClassLoader().getResourceAsStream("data/citylots.json.zip");
                BufferedInputStream bis = new BufferedInputStream(fis);
                ZipInputStream zipIn = new ZipInputStream(bis)
        ) {
            zipIn.getNextEntry();
            result = IOUtils.toString(zipIn, "UTF-8");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    //    @Benchmark
    public void fastjson(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parseObject(result, Map.class));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(result, Map.class, JSONReader.Feature.UseBigDecimalForDoubles));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(result, Map.class));
    }

    @Benchmark
    public void wastjson(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(result, Map.class));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(SuperLongText.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MINUTES)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
