package com.alibaba.fastjson3.benchmark;

import com.alibaba.fastjson3.JSONParser;
import com.alibaba.fastjson3.JSONPath;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Performance comparison: fastjson3 TypedMultiPath vs fastjson2 JSONPathTypedMulti.
 *
 * Run:
 * <pre>
 * mvn package -pl benchmark3 -DskipTests && \
 * java -jar benchmark3/target/benchmarks.jar JSONPathTypedMultiBenchmark
 * </pre>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 3)
@Fork(1)
@State(Scope.Benchmark)
public class JSONPathTypedMultiBenchmark {

    // ==================== Test data ====================

    static final String SMALL_JSON =
            "{\"id\":1001,\"name\":\"DataWorks\",\"score\":99.5}";

    static final String MEDIUM_JSON =
            "{\"id\":1001,\"name\":\"DataWorks\",\"score\":99.5,"
                    + "\"active\":true,\"version\":42,\"email\":\"test@example.com\","
                    + "\"phone\":\"+86-13800138000\",\"age\":25}";

    static final String LARGE_JSON;
    static {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < 50; i++) {
            sb.append("\"padding_field_").append(i).append("\":\"value_").append(i).append("\",");
        }
        sb.append("\"id\":1001,\"name\":\"DataWorks\",\"score\":99.5}");
        LARGE_JSON = sb.toString();
    }

    static final String NESTED_JSON =
            "{\"data\":{\"id\":1001,\"name\":\"DataWorks\",\"score\":99.5}}";

    static final String ARRAY_NESTED_JSON =
            "[{\"id\":1001,\"name\":\"DataWorks\",\"score\":99.5}]";

    static final String INDEX_JSON = "[101,\"DataWorks\",99.5,true,42]";

    static final byte[] SMALL_JSON_BYTES = SMALL_JSON.getBytes();
    static final byte[] LARGE_JSON_BYTES = LARGE_JSON.getBytes();

    // --- fastjson3 paths ---

    static final JSONPath FJ3_SMALL = JSONPath.of(
            new String[]{"$.id", "$.name", "$.score"},
            new Type[]{Long.class, String.class, BigDecimal.class}
    );

    static final JSONPath FJ3_MEDIUM = JSONPath.of(
            new String[]{"$.id", "$.name", "$.score", "$.active", "$.version",
                    "$.email", "$.phone", "$.age"},
            new Type[]{Long.class, String.class, BigDecimal.class, Boolean.class,
                    Long.class, String.class, String.class, Integer.class}
    );

    static final JSONPath FJ3_LARGE = JSONPath.of(
            new String[]{"$.id", "$.name", "$.score"},
            new Type[]{Long.class, String.class, BigDecimal.class}
    );

    static final JSONPath FJ3_NESTED = JSONPath.of(
            new String[]{"$.data.id", "$.data.name", "$.data.score"},
            new Type[]{Long.class, String.class, BigDecimal.class}
    );

    static final JSONPath FJ3_ARRAY_NESTED = JSONPath.of(
            new String[]{"$[0].id", "$[0].name", "$[0].score"},
            new Type[]{Long.class, String.class, BigDecimal.class}
    );

    static final JSONPath FJ3_INDEX = JSONPath.of(
            new String[]{"$[0]", "$[1]", "$[2]"},
            new Type[]{Long.class, String.class, BigDecimal.class}
    );

    // --- fastjson2 paths ---

    static final com.alibaba.fastjson2.JSONPath FJ2_SMALL = com.alibaba.fastjson2.JSONPath.of(
            new String[]{"$.id", "$.name", "$.score"},
            new Type[]{Long.class, String.class, BigDecimal.class}
    );

    static final com.alibaba.fastjson2.JSONPath FJ2_MEDIUM = com.alibaba.fastjson2.JSONPath.of(
            new String[]{"$.id", "$.name", "$.score", "$.active", "$.version",
                    "$.email", "$.phone", "$.age"},
            new Type[]{Long.class, String.class, BigDecimal.class, Boolean.class,
                    Long.class, String.class, String.class, Integer.class}
    );

    static final com.alibaba.fastjson2.JSONPath FJ2_LARGE = com.alibaba.fastjson2.JSONPath.of(
            new String[]{"$.id", "$.name", "$.score"},
            new Type[]{Long.class, String.class, BigDecimal.class}
    );

    static final com.alibaba.fastjson2.JSONPath FJ2_NESTED = com.alibaba.fastjson2.JSONPath.of(
            new String[]{"$.data.id", "$.data.name", "$.data.score"},
            new Type[]{Long.class, String.class, BigDecimal.class}
    );

    static final com.alibaba.fastjson2.JSONPath FJ2_ARRAY_NESTED = com.alibaba.fastjson2.JSONPath.of(
            new String[]{"$[0].id", "$[0].name", "$[0].score"},
            new Type[]{Long.class, String.class, BigDecimal.class}
    );

    static final com.alibaba.fastjson2.JSONPath FJ2_INDEX = com.alibaba.fastjson2.JSONPath.of(
            new String[]{"$[0]", "$[1]", "$[2]"},
            new Type[]{Long.class, String.class, BigDecimal.class}
    );

    // ==================== SingleName: 3 fields, small JSON ====================

    @Benchmark
    public void singleName_small_fj3(Blackhole bh) {
        bh.consume(FJ3_SMALL.extract(SMALL_JSON));
    }

    @Benchmark
    public void singleName_small_fj2(Blackhole bh) {
        bh.consume(FJ2_SMALL.extract(SMALL_JSON));
    }

    // ==================== SingleName: 8 fields, medium JSON ====================

    @Benchmark
    public void singleName_medium_fj3(Blackhole bh) {
        bh.consume(FJ3_MEDIUM.extract(MEDIUM_JSON));
    }

    @Benchmark
    public void singleName_medium_fj2(Blackhole bh) {
        bh.consume(FJ2_MEDIUM.extract(MEDIUM_JSON));
    }

    // ==================== SingleName: 3 fields, large JSON (50 padding fields) ====================

    @Benchmark
    public void singleName_large_fj3(Blackhole bh) {
        bh.consume(FJ3_LARGE.extract(LARGE_JSON));
    }

    @Benchmark
    public void singleName_large_fj2(Blackhole bh) {
        bh.consume(FJ2_LARGE.extract(LARGE_JSON));
    }

    // ==================== SingleName: small JSON, byte[] input ====================

    @Benchmark
    public void singleName_bytes_fj3(Blackhole bh) {
        try (JSONParser parser = JSONParser.of(SMALL_JSON_BYTES)) {
            bh.consume(FJ3_SMALL.extract(parser));
        }
    }

    @Benchmark
    public void singleName_bytes_fj2(Blackhole bh) {
        bh.consume(FJ2_SMALL.extract(
                com.alibaba.fastjson2.JSONReader.of(SMALL_JSON_BYTES)));
    }

    // ==================== PrefixName: $.data.x ====================

    @Benchmark
    public void prefixName_fj3(Blackhole bh) {
        bh.consume(FJ3_NESTED.extract(NESTED_JSON));
    }

    @Benchmark
    public void prefixName_fj2(Blackhole bh) {
        bh.consume(FJ2_NESTED.extract(NESTED_JSON));
    }

    // ==================== PrefixIndex: $[0].x ====================

    @Benchmark
    public void prefixIndex_fj3(Blackhole bh) {
        bh.consume(FJ3_ARRAY_NESTED.extract(ARRAY_NESTED_JSON));
    }

    @Benchmark
    public void prefixIndex_fj2(Blackhole bh) {
        bh.consume(FJ2_ARRAY_NESTED.extract(ARRAY_NESTED_JSON));
    }

    // ==================== SingleIndex: $[0], $[1], $[2] ====================

    @Benchmark
    public void singleIndex_fj3(Blackhole bh) {
        bh.consume(FJ3_INDEX.extract(INDEX_JSON));
    }

    @Benchmark
    public void singleIndex_fj2(Blackhole bh) {
        bh.consume(FJ2_INDEX.extract(INDEX_JSON));
    }

    // ==================== Runner ====================

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JSONPathTypedMultiBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
