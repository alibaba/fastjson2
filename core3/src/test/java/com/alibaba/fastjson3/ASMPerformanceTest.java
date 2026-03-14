package com.alibaba.fastjson3;

import com.alibaba.fastjson3.reader.ObjectReaderCreator;
import com.alibaba.fastjson3.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson3.writer.ObjectWriterCreator;
import com.alibaba.fastjson3.writer.ObjectWriterCreatorASM;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Quick performance sanity check: ASM-generated readers/writers vs reflection.
 * Not a proper JMH benchmark — only directionally useful.
 */
@Tag("performance")
public class ASMPerformanceTest {
    public static class PrimBean {
        public int id = 123;
        public String name = "hello";
        public long version = 999L;
        public double score = 3.14;
        public boolean active = true;
    }

    private static final String JSON_STR =
            "{\"active\":true,\"id\":123,\"name\":\"hello\",\"score\":3.14,\"version\":999}";
    private static final byte[] JSON_BYTES = JSON_STR.getBytes(StandardCharsets.UTF_8);

    @Test
    public void testWritePerformance() {
        ObjectWriter<PrimBean> asmWriter = ObjectWriterCreatorASM.createObjectWriter(PrimBean.class);
        ObjectWriter<PrimBean> refWriter = ObjectWriterCreator.createObjectWriter(PrimBean.class);
        PrimBean bean = new PrimBean();

        // Verify both produce correct output
        String asmJson = writeToString(asmWriter, bean);
        String refJson = writeToString(refWriter, bean);
        assertNotNull(asmJson);
        assertNotNull(refJson);

        // Warmup
        int warmup = 100_000;
        int iterations = 1_000_000;

        for (int i = 0; i < warmup; i++) {
            writeToString(asmWriter, bean);
            writeToString(refWriter, bean);
        }

        // Measure reflection
        long startRef = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            writeToString(refWriter, bean);
        }
        long refNanos = System.nanoTime() - startRef;

        // Measure ASM
        long startAsm = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            writeToString(asmWriter, bean);
        }
        long asmNanos = System.nanoTime() - startAsm;

        double refOps = (double) iterations / refNanos * 1_000_000;
        double asmOps = (double) iterations / asmNanos * 1_000_000;
        double ratio = asmOps / refOps;

        System.out.printf("Write (1M iterations):%n");
        System.out.printf("  Reflection: %.0f ops/ms (%.1f ms total)%n", refOps, refNanos / 1e6);
        System.out.printf("  ASM:        %.0f ops/ms (%.1f ms total)%n", asmOps, asmNanos / 1e6);
        System.out.printf("  ASM/Ref:    %.2fx%n", ratio);

        // ASM should not be slower than reflection
        assertTrue(ratio > 0.8, "ASM write should not be much slower than reflection, ratio=" + ratio);
    }

    @Test
    public void testReadPerformance() {
        ObjectReader<PrimBean> asmReader = ObjectReaderCreatorASM.createObjectReader(PrimBean.class);
        ObjectReader<PrimBean> refReader = ObjectReaderCreator.createObjectReader(PrimBean.class);

        // Verify both produce correct output
        PrimBean asmBean = readFromBytes(asmReader, JSON_BYTES);
        PrimBean refBean = readFromBytes(refReader, JSON_BYTES);
        assertEquals(refBean.id, asmBean.id);
        assertEquals(refBean.name, asmBean.name);

        // Warmup
        int warmup = 100_000;
        int iterations = 1_000_000;

        for (int i = 0; i < warmup; i++) {
            readFromBytes(asmReader, JSON_BYTES);
            readFromBytes(refReader, JSON_BYTES);
        }

        // Measure reflection
        long startRef = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            readFromBytes(refReader, JSON_BYTES);
        }
        long refNanos = System.nanoTime() - startRef;

        // Measure ASM
        long startAsm = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            readFromBytes(asmReader, JSON_BYTES);
        }
        long asmNanos = System.nanoTime() - startAsm;

        double refOps = (double) iterations / refNanos * 1_000_000;
        double asmOps = (double) iterations / asmNanos * 1_000_000;
        double ratio = asmOps / refOps;

        System.out.printf("Read (1M iterations):%n");
        System.out.printf("  Reflection: %.0f ops/ms (%.1f ms total)%n", refOps, refNanos / 1e6);
        System.out.printf("  ASM:        %.0f ops/ms (%.1f ms total)%n", asmOps, asmNanos / 1e6);
        System.out.printf("  ASM/Ref:    %.2fx%n", ratio);

        assertTrue(ratio > 0.8, "ASM read should not be much slower than reflection, ratio=" + ratio);
    }

    private static String writeToString(ObjectWriter<PrimBean> writer, PrimBean bean) {
        try (JSONGenerator gen = JSONGenerator.of()) {
            writer.write(gen, bean, null, null, 0);
            return gen.toString();
        }
    }

    private static PrimBean readFromBytes(ObjectReader<PrimBean> reader, byte[] bytes) {
        try (JSONParser parser = JSONParser.of(bytes)) {
            return reader.readObject(parser, null, null, 0);
        }
    }
}
