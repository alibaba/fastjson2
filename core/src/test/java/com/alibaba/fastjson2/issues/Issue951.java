package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class Issue951 {
    @Test
    public void test() {
        double[] values = new double[1000 * 1000];
        Arrays.fill(values, 123456789.0123456789D);

        try (JSONWriter writer = JSONWriter.ofUTF16()) {
            writer.writeAny(values);

            JSONReader reader = JSONReader.of(writer.toString());
            double[] parsed = reader.read(double[].class);
            assertArrayEquals(values, parsed);
        }

        try (JSONWriter writer = JSONWriter.ofUTF8()) {
            writer.writeAny(values);

            JSONReader reader = JSONReader.of(writer.getBytes());
            double[] parsed = reader.read(double[].class);
            assertArrayEquals(values, parsed);
        }
    }

    @Test
    public void test1() {
        float[] values = new float[1000 * 1000];
        Arrays.fill(values, 123456789.0123456789F);

        try (JSONWriter writer = JSONWriter.ofUTF16()) {
            writer.writeAny(values);

            JSONReader reader = JSONReader.of(writer.toString());
            float[] parsed = reader.read(float[].class);
            assertArrayEquals(values, parsed);
        }

        try (JSONWriter writer = JSONWriter.ofUTF8()) {
            writer.writeAny(values);

            JSONReader reader = JSONReader.of(writer.getBytes());
            float[] parsed = reader.read(float[].class);
            assertArrayEquals(values, parsed);
        }
    }
}
