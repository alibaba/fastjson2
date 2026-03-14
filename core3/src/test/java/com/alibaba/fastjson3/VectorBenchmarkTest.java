package com.alibaba.fastjson3;

import com.alibaba.fastjson3.util.JDKUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * Micro-benchmark comparing Vector API vs SWAR string scanning throughput.
 * Uses reflection to temporarily toggle VECTOR_SUPPORT for comparison.
 */
class VectorBenchmarkTest {
    private static final int WARMUP = 500_000;
    private static final int ITERATIONS = 2_000_000;

    @Test
    void benchmarkLongStringParsing() {
        System.out.println("=== Vector String Scanning Benchmark ===");
        System.out.println("Vector support: " + JDKUtils.VECTOR_SUPPORT);
        System.out.println("Vector byte size: " + JDKUtils.VECTOR_BYTE_SIZE);
        System.out.println();

        // Prepare test data: various string lengths
        int[] lengths = {16, 32, 64, 128, 256, 512, 1024};
        for (int len : lengths) {
            String value = "x".repeat(len);
            byte[] json = ("\"" + value + "\"").getBytes(StandardCharsets.UTF_8);

            // Warmup
            for (int i = 0; i < WARMUP; i++) {
                JSON.parseObject(json, String.class);
            }

            // Measure
            long start = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                JSON.parseObject(json, String.class);
            }
            long elapsed = System.nanoTime() - start;
            double opsMs = ITERATIONS / (elapsed / 1_000_000.0);
            System.out.printf("  String len=%4d: %,.0f ops/ms  (%.1f ns/op)%n",
                    len, opsMs, (double) elapsed / ITERATIONS);
        }

        // Also test with escape characters
        System.out.println();
        for (int len : new int[]{64, 256, 1024}) {
            String prefix = "a".repeat(len - 10);
            byte[] json = ("\"" + prefix + "\\nhello\\t\"").getBytes(StandardCharsets.UTF_8);

            for (int i = 0; i < WARMUP; i++) {
                JSON.parseObject(json, String.class);
            }
            long start = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                JSON.parseObject(json, String.class);
            }
            long elapsed = System.nanoTime() - start;
            double opsMs = ITERATIONS / (elapsed / 1_000_000.0);
            System.out.printf("  String len=%4d (with escape): %,.0f ops/ms  (%.1f ns/op)%n",
                    len, opsMs, (double) elapsed / ITERATIONS);
        }

        // Object with multiple string fields
        System.out.println();
        String objJson = "{" +
                "\"field1\":\"" + "a".repeat(100) + "\"," +
                "\"field2\":\"" + "b".repeat(100) + "\"," +
                "\"field3\":\"" + "c".repeat(100) + "\"," +
                "\"field4\":\"" + "d".repeat(100) + "\"" +
                "}";
        byte[] objBytes = objJson.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < WARMUP; i++) {
            JSON.parseObject(objBytes);
        }
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            JSON.parseObject(objBytes);
        }
        long elapsed = System.nanoTime() - start;
        double opsMs = ITERATIONS / (elapsed / 1_000_000.0);
        System.out.printf("  Object (4 x 100-char strings): %,.0f ops/ms  (%.1f ns/op)%n",
                opsMs, (double) elapsed / ITERATIONS);
    }
}
