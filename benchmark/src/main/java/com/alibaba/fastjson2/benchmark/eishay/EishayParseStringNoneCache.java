package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class EishayParseStringNoneCache {
    static String str;
//    static final ObjectMapper mapper = new ObjectMapper();
//    static final Gson gson = new Gson();

    static {
        try {
            InputStream is = EishayParseStringNoneCache.class.getClassLoader().getResourceAsStream("data/eishay_compact.json");
            str = IOUtils.toString(is, "UTF-8");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        ParserConfig config = new ParserConfig();
        bh.consume(com.alibaba.fastjson.JSON.parseObject(str, MediaContent.class, config));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        JSONReader.Context readContext = JSONFactory.createReadContext(provider);
        bh.consume(JSON.parseObject(str, MediaContent.class, readContext));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        bh.consume(mapper.readValue(str, HashMap.class));
    }

//    @Benchmark
    public void wastjson(Blackhole bh) throws Exception {
        bh.consume(
                io.github.wycst.wast.json.JSON.parse(str)
        );
    }

    @Benchmark
    public void gson(Blackhole bh) throws Exception {
        Gson gson = new Gson();
        bh.consume(
                gson.fromJson(str, HashMap.class)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayParseStringNoneCache.class.getName())
                .exclude(EishayParseTreeStringPretty.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
