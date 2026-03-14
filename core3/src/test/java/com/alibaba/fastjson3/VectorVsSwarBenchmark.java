package com.alibaba.fastjson3;

import com.alibaba.fastjson3.util.JDKUtils;
import com.alibaba.fastjson3.util.VectorizedScanner;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * A/B benchmark: compares Vector API scanner vs SWAR scanner directly,
 * and also end-to-end JSON parsing throughput.
 */
class VectorVsSwarBenchmark {
    private static final int WARMUP = 500_000;
    private static final int ITERATIONS = 5_000_000;

    // SWAR constants (duplicated from JSONParser.UTF8 for direct comparison)
    private static final long SWAR_QUOTE = 0x2222222222222222L;
    private static final long SWAR_ESCAPE = 0x5C5C5C5C5C5C5C5CL;
    private static final long SWAR_LO = 0x0101010101010101L;
    private static final long SWAR_HI = 0x8080808080808080L;

    static int scanSWAR(byte[] buf, int off, int limit) {
        while (off + 8 <= limit) {
            long word = JDKUtils.getLongDirect(buf, off);
            long v1 = word ^ SWAR_QUOTE;
            long v2 = word ^ SWAR_ESCAPE;
            long detect = ((v1 - SWAR_LO) & ~v1)
                    | ((v2 - SWAR_LO) & ~v2)
                    | word;
            if ((detect & SWAR_HI) != 0) {
                return off + (Long.numberOfTrailingZeros(detect & SWAR_HI) >> 3);
            }
            off += 8;
        }
        return off;
    }

    @Test
    void compareScanners() {
        System.out.println("=== Vector vs SWAR Scanner Micro-Benchmark ===");
        System.out.println("Vector byte size: " + JDKUtils.VECTOR_BYTE_SIZE);
        System.out.println();

        int[] lengths = {16, 32, 64, 128, 256, 512, 1024};

        System.out.printf("%-10s %14s %14s %8s%n", "Length", "Vector(ops/ms)", "SWAR(ops/ms)", "Speedup");
        System.out.println("-".repeat(50));

        for (int len : lengths) {
            // Create a buffer: ASCII content + closing quote
            byte[] buf = new byte[len + 1];
            for (int i = 0; i < len; i++) {
                buf[i] = 'x';
            }
            buf[len] = '"';

            // Warmup Vector
            for (int i = 0; i < WARMUP; i++) {
                VectorizedScanner.scanStringSimple(buf, 0, buf.length);
            }
            long start = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                VectorizedScanner.scanStringSimple(buf, 0, buf.length);
            }
            long vecElapsed = System.nanoTime() - start;
            double vecOps = ITERATIONS / (vecElapsed / 1_000_000.0);

            // Warmup SWAR
            for (int i = 0; i < WARMUP; i++) {
                scanSWAR(buf, 0, buf.length);
            }
            start = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                scanSWAR(buf, 0, buf.length);
            }
            long swarElapsed = System.nanoTime() - start;
            double swarOps = ITERATIONS / (swarElapsed / 1_000_000.0);

            System.out.printf("%-10d %,12.0f %,12.0f %7.2fx%n",
                    len, vecOps, swarOps, vecOps / swarOps);
        }

        // End-to-end JSON parse comparison
        System.out.println();
        System.out.println("=== End-to-End JSON Parse (byte[]) ===");
        System.out.printf("%-20s %14s%n", "Case", "ops/ms");
        System.out.println("-".repeat(36));

        // Short fields (typical business JSON)
        benchE2E("short_fields", "{\"name\":\"Alice\",\"age\":30,\"active\":true,\"score\":9.5}");
        // Medium string
        benchE2E("str_64", "{\"v\":\"" + "x".repeat(64) + "\"}");
        // Long string
        benchE2E("str_256", "{\"v\":\"" + "x".repeat(256) + "\"}");
        // Multiple long strings
        benchE2E("4x100_str", "{\"a\":\"" + "a".repeat(100)
                + "\",\"b\":\"" + "b".repeat(100)
                + "\",\"c\":\"" + "c".repeat(100)
                + "\",\"d\":\"" + "d".repeat(100) + "\"}");
    }

    private void benchE2E(String label, String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < WARMUP; i++) {
            JSON.parseObject(bytes);
        }
        long start = System.nanoTime();
        int iters = 2_000_000;
        for (int i = 0; i < iters; i++) {
            JSON.parseObject(bytes);
        }
        long elapsed = System.nanoTime() - start;
        double ops = iters / (elapsed / 1_000_000.0);
        System.out.printf("%-20s %,12.0f%n", label, ops);
    }
}
