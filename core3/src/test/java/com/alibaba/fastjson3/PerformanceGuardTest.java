package com.alibaba.fastjson3;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Performance regression guard: measures key operations and asserts
 * minimum throughput thresholds to catch significant regressions.
 *
 * <p>Thresholds are set conservatively (well below normal throughput)
 * to avoid flaky failures while still catching >2x regressions.</p>
 *
 * <p>Run with: {@code mvn test -Dgroups=performance}</p>
 */
@Tag("performance")
class PerformanceGuardTest {
    // Warm-up iterations to trigger JIT compilation
    private static final int WARMUP = 50_000;
    // Measured iterations
    private static final int ITERATIONS = 500_000;

    // ==================== POJO Read ====================

    public static class User {
        public String name;
        public int age;
        public boolean active;
        public double score;
    }

    private static final String USER_JSON = "{\"name\":\"Alice\",\"age\":30,\"active\":true,\"score\":9.5}";
    private static final byte[] USER_BYTES = USER_JSON.getBytes();

    @Test
    void readPojoFromStringThroughput() {
        ObjectMapper mapper = ObjectMapper.shared();
        // Warm up
        for (int i = 0; i < WARMUP; i++) {
            mapper.readValue(USER_JSON, User.class);
        }

        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            mapper.readValue(USER_JSON, User.class);
        }
        long elapsed = System.nanoTime() - start;
        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);

        System.out.printf("Read POJO (String): %.0f ops/ms%n", opsPerMs);
        assertTrue(opsPerMs > 500, "Read POJO throughput too low: " + opsPerMs + " ops/ms");
    }

    @Test
    void readPojoFromBytesThroughput() {
        ObjectMapper mapper = ObjectMapper.shared();
        for (int i = 0; i < WARMUP; i++) {
            mapper.readValue(USER_BYTES, User.class);
        }

        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            mapper.readValue(USER_BYTES, User.class);
        }
        long elapsed = System.nanoTime() - start;
        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);

        System.out.printf("Read POJO (byte[]): %.0f ops/ms%n", opsPerMs);
        assertTrue(opsPerMs > 500, "Read POJO (bytes) throughput too low: " + opsPerMs + " ops/ms");
    }

    // ==================== POJO Write ====================

    @Test
    void writePojoToStringThroughput() {
        ObjectMapper mapper = ObjectMapper.shared();
        User user = new User();
        user.name = "Alice";
        user.age = 30;
        user.active = true;
        user.score = 9.5;

        for (int i = 0; i < WARMUP; i++) {
            mapper.writeValueAsString(user);
        }

        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            mapper.writeValueAsString(user);
        }
        long elapsed = System.nanoTime() - start;
        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);

        System.out.printf("Write POJO (String): %.0f ops/ms%n", opsPerMs);
        assertTrue(opsPerMs > 500, "Write POJO throughput too low: " + opsPerMs + " ops/ms");
    }

    @Test
    void writePojoToBytesThroughput() {
        ObjectMapper mapper = ObjectMapper.shared();
        User user = new User();
        user.name = "Alice";
        user.age = 30;
        user.active = true;
        user.score = 9.5;

        for (int i = 0; i < WARMUP; i++) {
            mapper.writeValueAsBytes(user);
        }

        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            mapper.writeValueAsBytes(user);
        }
        long elapsed = System.nanoTime() - start;
        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);

        System.out.printf("Write POJO (byte[]): %.0f ops/ms%n", opsPerMs);
        assertTrue(opsPerMs > 500, "Write POJO (bytes) throughput too low: " + opsPerMs + " ops/ms");
    }

    // ==================== JSON Parse (raw) ====================

    @Test
    void parseRawJsonThroughput() {
        String json = "{\"id\":1,\"name\":\"test\",\"tags\":[\"a\",\"b\"],\"nested\":{\"x\":1.5}}";
        for (int i = 0; i < WARMUP; i++) {
            JSON.parse(json);
        }

        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            JSON.parse(json);
        }
        long elapsed = System.nanoTime() - start;
        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);

        System.out.printf("Parse raw JSON: %.0f ops/ms%n", opsPerMs);
        assertTrue(opsPerMs > 300, "Parse throughput too low: " + opsPerMs + " ops/ms");
    }

    // ==================== Round-trip (read + write) ====================

    public static class Order {
        public long id;
        public String product;
        public int quantity;
        public double price;
        public boolean shipped;
        public String note;
        public List<String> tags;
    }

    @Test
    void roundTripThroughput() {
        String json = "{\"id\":12345,\"product\":\"Widget\",\"quantity\":10,\"price\":29.99,"
                + "\"shipped\":true,\"note\":\"rush\",\"tags\":[\"sale\",\"new\"]}";
        ObjectMapper mapper = ObjectMapper.shared();

        for (int i = 0; i < WARMUP; i++) {
            Order o = mapper.readValue(json, Order.class);
            mapper.writeValueAsString(o);
        }

        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            Order o = mapper.readValue(json, Order.class);
            mapper.writeValueAsString(o);
        }
        long elapsed = System.nanoTime() - start;
        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);

        System.out.printf("Round-trip (read+write): %.0f ops/ms%n", opsPerMs);
        assertTrue(opsPerMs > 200, "Round-trip throughput too low: " + opsPerMs + " ops/ms");
    }
}
