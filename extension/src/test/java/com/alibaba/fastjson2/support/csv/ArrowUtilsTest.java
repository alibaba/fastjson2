package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.support.arrow.ArrowByteArrayConsumer;
import com.alibaba.fastjson2.support.arrow.ArrowUtils;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrowUtilsTest {
    @Test
    public void decimal() {
        if (JDKUtils.JVM_VERSION > 11) {
            return;
        }
        String[] strings = new String[]{
                "0",
                "123.45",
                "-123.45",
                "1234.5",
                "-1234.5",
                "-123.456",
                "123456",
                "-12356",
                "-9223372036854775808",
                "-.9223372036854775808",
                "-9223372036854775808.",
                "-.9223372036854775808",
                "-922337203685477580.8",
                "-92233720368547758.08",
                "92233720368547758.08",
                "-922337203685477.5808",
                "922337203685477.5808",
                "-192233720368547758.08",
                "192233720368547758.08",
                "-1922337203685477.5808",
                "1922337203685477.5808",
        };

        try (BufferAllocator allocator = new RootAllocator()) {
            DecimalVector vector = new DecimalVector("value", allocator, 38, 2);
            vector.allocateNew(1);

            for (int i = 0; i < strings.length; i++) {
                String str = strings[i];
                BigDecimal decimal = new BigDecimal(str);
                BigDecimal decimalScaled = decimal.setScale(vector.getScale(), BigDecimal.ROUND_CEILING);

                vector.set(0, decimalScaled);
                assertEquals(decimalScaled, vector.getObject(0));

                ArrowUtils.setDecimal(vector, 0, decimal);
                assertEquals(decimalScaled, vector.getObject(0));

                byte[] bytes = str.getBytes();
                ArrowUtils.setDecimal(vector, 0, bytes, 0, bytes.length);
                assertEquals(decimalScaled, vector.getObject(0));
            }

            vector.close();
        }
    }

    @Test
    public void decimal1() {
        if (JDKUtils.JVM_VERSION > 11) {
            return;
        }

        String[] strings = new String[]{
                "0",
                "123.45",
                "-123.45",
                "1234.5",
                "-1234.5",
                "-123.456",
                "123456",
                "-12356",
                "-9223372036854775808",
                "-.9223372036854775808",
                "-9223372036854775808.",
                "-.9223372036854775808",
                "-922337203685477580.8",
                "-92233720368547758.08",
                "92233720368547758.08",
                "-922337203685477.5808",
                "922337203685477.5808",
                "-192233720368547758.08",
                "192233720368547758.08",
                "-1922337203685477.5808",
                "1922337203685477.5808",
        };

        try (BufferAllocator allocator = new RootAllocator()) {
            DecimalVector vector = new DecimalVector("value", allocator, 38, 19);

            vector.allocateNew(1);

            for (int i = 0; i < strings.length; i++) {
                String str = strings[i];
                BigDecimal decimal = new BigDecimal(str);
                BigDecimal decimalScaled = decimal.setScale(vector.getScale(), BigDecimal.ROUND_CEILING);

                vector.set(0, decimalScaled);
                assertEquals(decimalScaled, vector.getObject(0));

                ArrowUtils.setDecimal(vector, 0, decimal);
                assertEquals(decimalScaled, vector.getObject(0));

                byte[] bytes = str.getBytes();
                ArrowUtils.setDecimal(vector, 0, bytes, 0, bytes.length);
                assertEquals(decimalScaled, vector.getObject(0));
            }

            vector.close();
        }
    }

    @Test
    public void write() throws IOException {
        if (JDKUtils.JVM_VERSION > 11) {
            return;
        }

        try (BufferAllocator allocator = new RootAllocator()) {
            BitVector v0 = new BitVector("boolean", allocator);
            VarCharVector v1 = new VarCharVector("varchar", allocator);
            TinyIntVector v2 = new TinyIntVector("tinyint", allocator);
            SmallIntVector v3 = new SmallIntVector("smallint", allocator);
            IntVector v4 = new IntVector("int", allocator);
            BigIntVector v5 = new BigIntVector("bigint", allocator);
            Float4Vector v6 = new Float4Vector("float", allocator);
            Float8Vector v7 = new Float8Vector("float", allocator);
            DecimalVector v8 = new DecimalVector("dec", allocator, 38, 2);
            DecimalVector v9 = new DecimalVector("float", allocator, 60, 2);
            DateMilliVector v10 = new DateMilliVector("float", allocator);

            long millis = LocalDate.of(2023, 4, 10).atStartOfDay()
                    .atZone(DateUtils.DEFAULT_ZONE_ID)
                    .toInstant()
                    .toEpochMilli();

            List<FieldVector> vectors = Arrays.asList(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);
            VectorSchemaRoot root = new VectorSchemaRoot(vectors);
            root.allocateNew();

            root.setRowCount(1);
            int row = 0;
            v0.set(row, 1);
            v1.set(row, "abc".getBytes());
            v2.set(row, (byte) 101);
            v3.set(row, (short) 1001);
            v4.set(row, 10001);
            v5.set(row, 100001);
            v6.set(row, 2.1F);
            v7.set(row, 2.1D);
            v8.set(row, BigDecimal.valueOf(1234, 2));
            v9.set(row, BigDecimal.valueOf(12345, 2));
            v10.set(row, millis);

            CSVWriter writer = CSVWriter.of();
            ArrowUtils.write(writer, root);
            String csv = writer.toString();
            assertEquals("1,abc,101,1001,10001,100001,2.1,2.1,12.34,123.45,2023-04-10\n", csv);

            VectorSchemaRoot root2 = new VectorSchemaRoot(vectors);

            byte[] utf8Bytes = csv.getBytes(StandardCharsets.UTF_8);
            CSVReader csvReader = CSVReader.of(
                    utf8Bytes,
                    new ArrowByteArrayConsumer(
                            root2.getSchema(),
                            1,
                            (VectorSchemaRoot o, int blockIndex) -> {
                                System.out.println("write block " + blockIndex + " [" + root2.getRowCount() + "]");
                            },
                            null
                    ));
            csvReader.readAll();

            CSVWriter writer2 = CSVWriter.of();
            ArrowUtils.write(writer2, root2);
            String csv2 = writer2.toString();
            assertEquals(csv, csv2);

            for (FieldVector vector : vectors) {
                vector.close();
            }
        }
    }

    @Test
    public void writeDecimal() throws IOException {
        if (JDKUtils.JVM_VERSION > 11) {
            return;
        }

        try (BufferAllocator allocator = new RootAllocator()) {
            DecimalVector vector = new DecimalVector("dec", allocator, 19, 2);

            VectorSchemaRoot root = new VectorSchemaRoot(Arrays.asList(vector));
            root.allocateNew();
            root.allocateNew();
            root.allocateNew();
            vector.set(0, BigDecimal.valueOf(12345, 2));
            vector.set(1, new BigDecimal("123.45"));
            root.setRowCount(3);

            CSVWriter writer = CSVWriter.of();
            ArrowUtils.write(writer, root);
            assertEquals("123.45\n123.45\n\n", writer.toString());

            vector.close();
        }
    }
}
