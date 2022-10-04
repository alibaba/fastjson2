package com.alibaba.fastjson2.benchmark.schema;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.schema.JSONSchema;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class JSONSchemaBenchmark1 {
    static final JSONSchema SCHEMA_UUID = JSONObject.of("type", "string", "format", "uuid").to(JSONSchema::of);
    static final JSONSchema SCHEMA_DATETIME = JSONObject.of("type", "string", "format", "date-time").to(JSONSchema::of);
    static final JSONSchema SCHEMA_DATE = JSONObject.of("type", "string", "format", "date").to(JSONSchema::of);
    static final JSONSchema SCHEMA_TIME = JSONObject.of("type", "string", "format", "time").to(JSONSchema::of);
    static final JSONSchema SCHEMA_NUMBER = JSONObject.of("type", "number", "minimum", 10).to(JSONSchema::of);
    static final JSONSchema SCHEMA_INTEGER = JSONObject.of("type", "integer", "minimum", 10).to(JSONSchema::of);

    @Benchmark
    public void format_uuid(Blackhole bh) {
        bh.consume(
                SCHEMA_UUID.isValid("a7f41390-39a9-4ca6-a13b-88cf07a41108")
        );
    }

    @Benchmark
    public void format_datetime(Blackhole bh) {
        bh.consume(
                SCHEMA_DATETIME.isValid("2017-07-21 12:13:14")
        );
    }

    @Benchmark
    public void format_date(Blackhole bh) {
        bh.consume(
                SCHEMA_DATE.isValid("2017-07-21")
        );
    }

    @Benchmark
    public void format_time(Blackhole bh) {
        bh.consume(
                SCHEMA_TIME.isValid("12:13:14")
        );
    }

    public static void format_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000 * 100; ++i) {
//            SCHEMA_UUID.isValid("a7f41390-39a9-4ca6-a13b-88cf07a41108");
//            SCHEMA_DATETIME.isValid("2017-07-21 12:13:14"); // 123
//            SCHEMA_DATE.isValid("2017-07-21"); // 48
//            SCHEMA_TIME.isValid("12:13:14"); //
//            SCHEMA_NUMBER.isValid(9); // 42
//            SCHEMA_NUMBER.isValid(11); // 302 120
//            SCHEMA_NUMBER.isValid(11D); //
            SCHEMA_NUMBER.isValid(9D); //
//            SCHEMA_INTEGER.isValid(9); // 87
//            SCHEMA_INTEGER.isValid(11); //
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
        // zulu17.32.13 :
        // zulu11.52.13 :
        // zulu8.58.0.13 :
    }

    public static void format_perf_test() {
        for (int i = 0; i < 10; i++) {
            format_perf();
        }
    }

    public static void main(String[] args) throws RunnerException {
        format_perf_test();
//
//        Options options = new OptionsBuilder()
//                .include(JSONSchemaBenchmark.class.getName())
//                .mode(Mode.Throughput)
//                .timeUnit(TimeUnit.MILLISECONDS)
//                .forks(1)
//                .build();
//        new Runner(options).run();
    }
}
