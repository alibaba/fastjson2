package com.alibaba.fastjson2.benchmark.simdjson;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.simdjson.SimdJsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson2.benchmark.simdjson.SimdJsonPaddingUtil.padded;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class SchemaBasedParseAndSelectBenchmark {
    private final SimdJsonParser simdJsonParser = new SimdJsonParser();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private byte[] buffer;
    private byte[] bufferPadded;

    @Setup(Level.Trial)
    public void setup() throws IOException {
        try (InputStream is = ParseBenchmark.class.getResourceAsStream("/data/simd-json/twitter.json")) {
            buffer = is.readAllBytes();
            bufferPadded = padded(buffer);
        }
    }

    @Benchmark
    public int countUniqueUsersWithDefaultProfile_simdjson() {
        Set<String> defaultUsers = new HashSet<>();
        SimdJsonTwitter twitter = simdJsonParser.parse(buffer, buffer.length, SimdJsonTwitter.class);
        for (SimdJsonStatus status : twitter.statuses()) {
            SimdJsonUser user = status.user();
            if (user.default_profile()) {
                defaultUsers.add(user.screen_name());
            }
        }
        return defaultUsers.size();
    }

    @Benchmark
    public int countUniqueUsersWithDefaultProfile_simdjsonPadded() {
        Set<String> defaultUsers = new HashSet<>();
        SimdJsonTwitter twitter = simdJsonParser.parse(bufferPadded, buffer.length, SimdJsonTwitter.class);
        for (SimdJsonStatus status : twitter.statuses()) {
            SimdJsonUser user = status.user();
            if (user.default_profile()) {
                defaultUsers.add(user.screen_name());
            }
        }
        return defaultUsers.size();
    }

    @Benchmark
    public int countUniqueUsersWithDefaultProfile_jackson() throws IOException {
        Set<String> defaultUsers = new HashSet<>();
        SimdJsonTwitter twitter = objectMapper.readValue(buffer, SimdJsonTwitter.class);
        for (SimdJsonStatus status : twitter.statuses()) {
            SimdJsonUser user = status.user();
            if (user.default_profile()) {
                defaultUsers.add(user.screen_name());
            }
        }
        return defaultUsers.size();
    }

    @Benchmark
    public int countUniqueUsersWithDefaultProfile_fastjson() {
        Set<String> defaultUsers = new HashSet<>();
        SimdJsonTwitter twitter = JSON.parseObject(buffer, SimdJsonTwitter.class);
        for (SimdJsonStatus status : twitter.statuses()) {
            SimdJsonUser user = status.user();
            if (user.default_profile()) {
                defaultUsers.add(user.screen_name());
            }
        }
        return defaultUsers.size();
    }

    public record SimdJsonUser(boolean default_profile, String screen_name)
            implements Serializable {
    }

    public record SimdJsonStatus(SimdJsonUser user)
            implements Serializable {
    }

    public record SimdJsonTwitter(List<SimdJsonStatus> statuses)
            implements Serializable {
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(SchemaBasedParseAndSelectBenchmark.class.getName())
                .jvmArgsAppend("--add-opens=java.base/java.time=ALL-UNNAMED", "--add-modules=jdk.incubator.vector")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
