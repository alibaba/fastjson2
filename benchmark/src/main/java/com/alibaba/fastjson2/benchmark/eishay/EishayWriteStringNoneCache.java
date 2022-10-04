package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.benchmark.eishay.mixin.ImageMixin;
import com.alibaba.fastjson2.benchmark.eishay.mixin.MediaContentMixin;
import com.alibaba.fastjson2.benchmark.eishay.mixin.MediaMixin;
import com.alibaba.fastjson2.benchmark.eishay.vo.Image;
import com.alibaba.fastjson2.benchmark.eishay.vo.Media;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
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

public class EishayWriteStringNoneCache {
    static MediaContent mc;
    static {
        try {
            InputStream is = EishayWriteString.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");

            ObjectReaderProvider provider = new ObjectReaderProvider(ObjectReaderCreator.INSTANCE);
            JSONReader.Context readContext = JSONFactory.createReadContext(provider);
            mc = JSON.parseObject(str, MediaContent.class, readContext);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        SerializeConfig config = new SerializeConfig();
        bh.consume(com.alibaba.fastjson.JSON.toJSONString(mc, config));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        bh.consume(JSON.toJSONString(mc, JSONFactory.createWriteContext(provider)));
    }

    @Benchmark
    public void fastjson2Mixin(Blackhole bh) {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.mixIn(MediaContent.class, MediaContentMixin.class);
        provider.mixIn(Media.class, MediaMixin.class);
        provider.mixIn(Image.class, ImageMixin.class);
        bh.consume(JSON.toJSONString(mc, JSONFactory.createWriteContext(provider)));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        bh.consume(mapper.writeValueAsString(mc));
    }

    //    @Benchmark
    public void wastjson(Blackhole bh) throws Exception {
        bh.consume(
                io.github.wycst.wast.json.JSON.toJsonString(mc)
        );
    }

    @Benchmark
    public void gson(Blackhole bh) throws Exception {
        Gson gson = new Gson();
        bh.consume(
                gson.toJson(mc)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayWriteStringNoneCache.class.getName())
                .mode(Mode.Throughput)
                .warmupIterations(3)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
