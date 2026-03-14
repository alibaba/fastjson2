package com.alibaba.fastjson3;

import com.alibaba.fastjson3.reader.ObjectReaderCreator;
import com.alibaba.fastjson3.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson3.writer.ObjectWriterCreator;
import com.alibaba.fastjson3.writer.ObjectWriterCreatorASM;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Comprehensive performance benchmark measuring throughput of all key paths.
 * Run with: {@code mvn test -Dtest=BenchmarkTest -Dgroups=performance}
 */
@Tag("performance")
class BenchmarkTest {
    // ==================== Test data ====================

    public static class SmallBean {
        public int id;
        public String name;
        public boolean active;
    }

    public static class MediumBean {
        public int id;
        public String name;
        public long version;
        public double score;
        public boolean active;
        public String email;
        public String phone;
        public int age;
    }

    public static class LargeBean {
        public int id;
        public String name;
        public long version;
        public double score;
        public boolean active;
        public String email;
        public String phone;
        public int age;
        public String address;
        public String city;
        public String country;
        public String zipCode;
        public double latitude;
        public double longitude;
        public String bio;
        public List<String> tags;
    }

    private static final String SMALL_JSON =
            "{\"active\":true,\"id\":1,\"name\":\"Alice\"}";
    private static final String MEDIUM_JSON =
            "{\"active\":true,\"age\":30,\"email\":\"a@b.c\",\"id\":1,\"name\":\"Alice\","
                    + "\"phone\":\"+1234\",\"score\":9.5,\"version\":100}";
    private static final String LARGE_JSON =
            "{\"active\":true,\"address\":\"123 Main St\",\"age\":30,\"bio\":\"Software engineer\","
                    + "\"city\":\"NYC\",\"country\":\"US\",\"email\":\"a@b.c\",\"id\":1,"
                    + "\"latitude\":40.7128,\"longitude\":-74.006,\"name\":\"Alice\","
                    + "\"phone\":\"+1234\",\"score\":9.5,\"tags\":[\"dev\",\"ops\"],"
                    + "\"version\":100,\"zipCode\":\"10001\"}";

    private static final byte[] SMALL_BYTES = SMALL_JSON.getBytes(StandardCharsets.UTF_8);
    private static final byte[] MEDIUM_BYTES = MEDIUM_JSON.getBytes(StandardCharsets.UTF_8);
    private static final byte[] LARGE_BYTES = LARGE_JSON.getBytes(StandardCharsets.UTF_8);

    private static final int WARMUP = 200_000;
    private static final int ITERATIONS = 2_000_000;

    // ==================== Read benchmarks ====================

    @Test
    void benchReadSmall() {
        System.out.println("=== Read Performance (2M iterations) ===");
        System.out.println();
        benchRead("Small (3 fields)", SmallBean.class, SMALL_BYTES);
    }

    @Test
    void benchReadMedium() {
        benchRead("Medium (8 fields)", MediumBean.class, MEDIUM_BYTES);
    }

    @Test
    void benchReadLarge() {
        benchRead("Large (16 fields)", LargeBean.class, LARGE_BYTES);
    }

    private <T> void benchRead(String label, Class<T> type, byte[] bytes) {
        ObjectReader<T> refReader = ObjectReaderCreator.createObjectReader(type);
        ObjectReader<T> asmReader = ObjectReaderCreatorASM.createObjectReader(type);
        ObjectMapper mapper = ObjectMapper.shared();

        // Warmup all paths
        for (int i = 0; i < WARMUP; i++) {
            readBytes(refReader, bytes);
            readBytes(asmReader, bytes);
            mapper.readValue(bytes, type);
        }

        long refNs = measureRead(refReader, bytes, ITERATIONS);
        long asmNs = measureRead(asmReader, bytes, ITERATIONS);
        long mapperNs = measureMapper(mapper, type, bytes, ITERATIONS);

        double refOps = opsPerMs(ITERATIONS, refNs);
        double asmOps = opsPerMs(ITERATIONS, asmNs);
        double mapperOps = opsPerMs(ITERATIONS, mapperNs);

        System.out.printf("  Read %-20s  Reflection: %,8.0f ops/ms  |  ASM: %,8.0f ops/ms (%.2fx)  |  Mapper: %,8.0f ops/ms%n",
                label, refOps, asmOps, asmOps / refOps, mapperOps);
    }

    // ==================== Write benchmarks ====================

    @Test
    void benchWriteSmall() {
        SmallBean bean = new SmallBean();
        bean.id = 1;
        bean.name = "Alice";
        bean.active = true;

        System.out.println();
        System.out.println("=== Write Performance (2M iterations) ===");
        System.out.println();
        benchWrite("Small (3 fields)", SmallBean.class, bean);
    }

    @Test
    void benchWriteMedium() {
        MediumBean bean = new MediumBean();
        bean.id = 1;
        bean.name = "Alice";
        bean.version = 100;
        bean.score = 9.5;
        bean.active = true;
        bean.email = "a@b.c";
        bean.phone = "+1234";
        bean.age = 30;

        benchWrite("Medium (8 fields)", MediumBean.class, bean);
    }

    @Test
    void benchWriteLarge() {
        LargeBean bean = new LargeBean();
        bean.id = 1;
        bean.name = "Alice";
        bean.version = 100;
        bean.score = 9.5;
        bean.active = true;
        bean.email = "a@b.c";
        bean.phone = "+1234";
        bean.age = 30;
        bean.address = "123 Main St";
        bean.city = "NYC";
        bean.country = "US";
        bean.zipCode = "10001";
        bean.latitude = 40.7128;
        bean.longitude = -74.006;
        bean.bio = "Software engineer";

        benchWrite("Large (16 fields)", LargeBean.class, bean);
    }

    @SuppressWarnings("unchecked")
    private <T> void benchWrite(String label, Class<T> type, T bean) {
        ObjectWriter<T> refWriter = ObjectWriterCreator.createObjectWriter(type);
        ObjectWriter<T> asmWriter = ObjectWriterCreatorASM.createObjectWriter(type);
        ObjectMapper mapper = ObjectMapper.shared();

        // Warmup
        for (int i = 0; i < WARMUP; i++) {
            writeString(refWriter, bean);
            writeString(asmWriter, bean);
            mapper.writeValueAsString(bean);
        }

        long refNs = measureWrite(refWriter, bean, ITERATIONS);
        long asmNs = measureWrite(asmWriter, bean, ITERATIONS);
        long mapperNs = measureMapperWrite(mapper, bean, ITERATIONS);

        double refOps = opsPerMs(ITERATIONS, refNs);
        double asmOps = opsPerMs(ITERATIONS, asmNs);
        double mapperOps = opsPerMs(ITERATIONS, mapperNs);

        System.out.printf("  Write %-20s Reflection: %,8.0f ops/ms  |  ASM: %,8.0f ops/ms (%.2fx)  |  Mapper: %,8.0f ops/ms%n",
                label, refOps, asmOps, asmOps / refOps, mapperOps);
    }

    // ==================== UTF-8 byte[] read vs String read ====================

    @Test
    void benchReadStringVsBytes() {
        ObjectMapper mapper = ObjectMapper.shared();

        System.out.println();
        System.out.println("=== String vs byte[] Read (2M iterations) ===");
        System.out.println();

        for (int i = 0; i < WARMUP; i++) {
            mapper.readValue(MEDIUM_JSON, MediumBean.class);
            mapper.readValue(MEDIUM_BYTES, MediumBean.class);
        }

        long stringNs = measureMapperString(mapper, MediumBean.class, MEDIUM_JSON, ITERATIONS);
        long bytesNs = measureMapper(mapper, MediumBean.class, MEDIUM_BYTES, ITERATIONS);

        double stringOps = opsPerMs(ITERATIONS, stringNs);
        double bytesOps = opsPerMs(ITERATIONS, bytesNs);

        System.out.printf("  Read String:  %,8.0f ops/ms%n", stringOps);
        System.out.printf("  Read byte[]:  %,8.0f ops/ms (%.2fx faster)%n", bytesOps, bytesOps / stringOps);
    }

    // ==================== Write String vs byte[] ====================

    @Test
    void benchWriteStringVsBytes() {
        ObjectMapper mapper = ObjectMapper.shared();
        MediumBean bean = new MediumBean();
        bean.id = 1;
        bean.name = "Alice";
        bean.version = 100;
        bean.score = 9.5;
        bean.active = true;
        bean.email = "a@b.c";
        bean.phone = "+1234";
        bean.age = 30;

        System.out.println();
        System.out.println("=== String vs byte[] Write (2M iterations) ===");
        System.out.println();

        for (int i = 0; i < WARMUP; i++) {
            mapper.writeValueAsString(bean);
            mapper.writeValueAsBytes(bean);
        }

        long stringNs = measureMapperWrite(mapper, bean, ITERATIONS);
        long bytesNs = measureMapperWriteBytes(mapper, bean, ITERATIONS);

        double stringOps = opsPerMs(ITERATIONS, stringNs);
        double bytesOps = opsPerMs(ITERATIONS, bytesNs);

        System.out.printf("  Write String: %,8.0f ops/ms%n", stringOps);
        System.out.printf("  Write byte[]: %,8.0f ops/ms (%.2fx)%n", bytesOps, bytesOps / stringOps);
    }

    // ==================== Measurement helpers ====================

    private <T> long measureRead(ObjectReader<T> reader, byte[] bytes, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            readBytes(reader, bytes);
        }
        return System.nanoTime() - start;
    }

    private <T> long measureWrite(ObjectWriter<T> writer, T bean, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            writeString(writer, bean);
        }
        return System.nanoTime() - start;
    }

    private <T> long measureMapper(ObjectMapper mapper, Class<T> type, byte[] bytes, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            mapper.readValue(bytes, type);
        }
        return System.nanoTime() - start;
    }

    private <T> long measureMapperString(ObjectMapper mapper, Class<T> type, String json, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            mapper.readValue(json, type);
        }
        return System.nanoTime() - start;
    }

    private long measureMapperWrite(ObjectMapper mapper, Object bean, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            mapper.writeValueAsString(bean);
        }
        return System.nanoTime() - start;
    }

    private long measureMapperWriteBytes(ObjectMapper mapper, Object bean, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            mapper.writeValueAsBytes(bean);
        }
        return System.nanoTime() - start;
    }

    private static <T> T readBytes(ObjectReader<T> reader, byte[] bytes) {
        try (JSONParser parser = JSONParser.of(bytes)) {
            return reader.readObject(parser, null, null, 0);
        }
    }

    private static <T> String writeString(ObjectWriter<T> writer, T bean) {
        try (JSONGenerator gen = JSONGenerator.of()) {
            writer.write(gen, bean, null, null, 0);
            return gen.toString();
        }
    }

    private static double opsPerMs(int iterations, long nanos) {
        return (double) iterations / (nanos / 1_000_000.0);
    }
}
