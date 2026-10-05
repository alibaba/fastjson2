package com.alibaba.fastjson2.support.arrow;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.types.FloatingPointPrecision;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ArrowConversionRegressionTest {
    @BeforeEach
    public void requireArrowMemoryAccess() throws NoSuchFieldException {
        try {
            Buffer.class.getDeclaredField("address").setAccessible(true);
        } catch (RuntimeException error) {
            assumeTrue(false, "Arrow requires --add-opens=java.base/java.nio=ALL-UNNAMED");
        }
    }

    @Test
    public void utf8Strings() {
        try (BufferAllocator allocator = new RootAllocator();
                VarCharVector vector = new VarCharVector("value", allocator)) {
            vector.allocateNew(128, 1);
            for (String value : new String[]{"ascii", "café", "中文"}) {
                ArrowUtils.setString(vector, 0, value);
                assertEquals(value, new String(vector.get(0), StandardCharsets.UTF_8));
            }
        }
    }

    @Test
    public void doublePrecisionAndRange() {
        String[] values = {"1.2345678901234567", "1.0E100", "1.0E-100"};
        Schema schema = new Schema(Collections.singletonList(
                Field.nullable("value", new ArrowType.FloatingPoint(FloatingPointPrecision.DOUBLE))));
        ArrowByteArrayConsumer consumer = new ArrowByteArrayConsumer(schema, values.length, (root, block) -> {
            Float8Vector vector = (Float8Vector) root.getVector(0);
            for (int i = 0; i < values.length; i++) {
                assertEquals(Double.parseDouble(values[i]), vector.get(i));
            }
        }, null);
        try {
            for (int i = 0; i < values.length; i++) {
                byte[] bytes = values[i].getBytes(StandardCharsets.UTF_8);
                consumer.accept(i, 0, bytes, 0, bytes.length, StandardCharsets.UTF_8);
                consumer.afterRow(i);
            }
        } finally {
            consumer.root.close();
            consumer.allocator.close();
        }
    }
}
