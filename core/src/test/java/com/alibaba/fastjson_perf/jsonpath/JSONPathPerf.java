package com.alibaba.fastjson_perf.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

public class JSONPathPerf {
    @Test
    public void test_of_perf_test() {
        for (int i = 0; i < 10; i++) {
            of_perf();
            // JDK 8 : 246 215
        }
    }

    public void of_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000 * 10; ++i) {
            JSONPath.of("$.id");
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println(millis);
    }
}
