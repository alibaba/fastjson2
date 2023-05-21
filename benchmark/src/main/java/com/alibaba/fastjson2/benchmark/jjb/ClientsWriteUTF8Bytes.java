package com.alibaba.fastjson2.benchmark.jjb;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class ClientsWriteUTF8Bytes {
    static Clients clients;
    static final ObjectMapper mapper = new ObjectMapper();
    static final Gson gson = new Gson();
    static final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().includeServiceLoader());

    static {
        try {
            InputStream is = ClientsWriteUTF8Bytes.class.getClassLoader().getResourceAsStream("data/jjb/client.json");
            String str = IOUtils.toString(is, "UTF-8");
            clients = JSONReader.of(str)
                    .read(Clients.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.toJSONBytes(clients));
    }

    @Benchmark
    public void dsljson(Blackhole bh) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        dslJson.serialize(clients, bytesOut);
        byte[] bytes = bytesOut.toByteArray();
        bh.consume(bytes);
    }

//    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.writeValueAsBytes(clients));
    }

//    @Benchmark
    public void gson(Blackhole bh) throws Exception {
        bh.consume(gson
                .toJson(clients)
                .getBytes(StandardCharsets.UTF_8)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ClientsWriteUTF8Bytes.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
