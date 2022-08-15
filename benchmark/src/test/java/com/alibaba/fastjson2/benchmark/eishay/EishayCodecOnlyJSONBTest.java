package com.alibaba.fastjson2.benchmark.eishay;

import org.openjdk.jmh.runner.RunnerException;

public class EishayCodecOnlyJSONBTest {
    public static void main(String[] args) throws RunnerException {
        EishayCodecOnlyJSONB benchmark = new EishayCodecOnlyJSONB();
//        benchmark.serialize_jsonb_arrayMapping_perf_test();
        benchmark.deserialize_jsonbArrayMapping_perf_test();
    }
}
