package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.benchmark.eishay.mixin.ImageMixin;
import com.alibaba.fastjson2.benchmark.eishay.mixin.MediaContentMixin;
import com.alibaba.fastjson2.benchmark.eishay.mixin.MediaMixin;
import com.alibaba.fastjson2.benchmark.eishay.vo.Image;
import com.alibaba.fastjson2.benchmark.eishay.vo.Media;
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
import java.util.concurrent.TimeUnit;

public class EishayParseString {
    static String str;
    static final ObjectMapper mapper = new ObjectMapper();
    static final Gson gson = new Gson();
    static final ObjectReaderProvider provider = new ObjectReaderProvider();

    static {
        try {
            InputStream is = EishayParseString.class.getClassLoader().getResourceAsStream("data/eishay_compact.json");
            str = IOUtils.toString(is, "UTF-8");
            JSON.parseObject(str, MediaContent.class);

            provider.mixIn(MediaContent.class, MediaContentMixin.class);
            provider.mixIn(Image.class, ImageMixin.class);
            provider.mixIn(Media.class, MediaMixin.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parseObject(str, MediaContent.class));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.parseObject(str, MediaContent.class));
    }

//    @Benchmark
    public void fastjson2Mixin(Blackhole bh) {
        bh.consume(JSON.parseObject(str, MediaContent.class, JSONFactory.createReadContext(provider)));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(str, MediaContent.class));
    }

//    @Benchmark
    public void wastjson(Blackhole bh) throws Exception {
        bh.consume(
                io.github.wycst.wast.json.JSON.parseObject(str, MediaContent.class)
        );
    }

    @Benchmark
    public void gson(Blackhole bh) throws Exception {
        bh.consume(
                gson.fromJson(str, MediaContent.class)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayParseString.class.getName())
                .exclude(EishayParseStringPretty.class.getName())
                .exclude(EishayParseStringNoneCache.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
