package com.alibaba.fastjson2.benchmark.eishay;

import org.openjdk.jmh.runner.RunnerException;

public class EishayCodecOnlyJSONBTest {
    public static void main(String[] args) throws RunnerException {
        new EishayCodecOnlyJSONB().serialize_jsonb_arrayMapping_perf_test();
    }
}
