package com.alibaba.fastjson2.benchmark.jjb;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.benchmark.eishay.EishayParseUTF8BytesPretty;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class ClientsParseUTF8Bytes {
    static byte[] utf8Bytes;
    static final ObjectMapper mapper = new ObjectMapper();
    static final Gson gson = new Gson();
    static final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().includeServiceLoader());
    static {
        try {
            InputStream is = ClientsParseUTF8Bytes.class.getClassLoader().getResourceAsStream("data/jjb/client.json");
            utf8Bytes = IOUtils.toString(is, "UTF-8").getBytes(StandardCharsets.UTF_8);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.parseObject(utf8Bytes, Clients.class));
    }

    @Benchmark
    public void dsljson(Blackhole bh) throws IOException {
        bh.consume(dslJson.deserialize(Clients.class, utf8Bytes, utf8Bytes.length));
    }

//    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(utf8Bytes, Clients.class));
    }

//    @Benchmark
    public void gson(Blackhole bh) throws Exception {
        bh.consume(gson
                .fromJson(
                        new String(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.UTF_8),
                        Clients.class)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ClientsParseUTF8Bytes.class.getName())
                .exclude(EishayParseUTF8BytesPretty.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(2)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
