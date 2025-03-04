package com.alibaba.fastjson2.benchmark.simdjson;

import com.alibaba.fastjson2.JSON;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.simdjson.JsonValue;
import org.simdjson.SimdJsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson2.benchmark.simdjson.SimdJsonPaddingUtil.padded;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ParseBenchmark {
    @Param({"/data/simd-json/twitter.json", "/data/simd-json/gsoc-2018.json", "/data/simd-json/github_events.json"})
    String fileName;

    private final SimdJsonParser simdJsonParser = new SimdJsonParser();

    private byte[] buffer;
    private byte[] bufferPadded;

    @Setup(Level.Trial)
    public void setup() throws IOException {
        try (InputStream is = ParseBenchmark.class.getResourceAsStream(fileName)) {
            byte[] buf = new byte[1024 * 1024 * 64];
            int count = is.read(buf);
            buffer = Arrays.copyOf(buf, count);
            bufferPadded = padded(buffer);
        }
    }

    @Benchmark
    public JsonValue simdjson() {
        return simdJsonParser.parse(buffer, buffer.length);
    }

    @Benchmark
    public Object fastjson() {
        return JSON.parse(buffer);
    }

    @Benchmark
    public JsonValue simdjsonPadded() {
        return simdJsonParser.parse(bufferPadded, buffer.length);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ParseBenchmark.class.getName())
                .jvmArgsAppend("--add-opens=java.base/java.time=ALL-UNNAMED", "--add-modules=jdk.incubator.vector")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
