package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.util.JDKUtils;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class CSVToArrowTest {
    static final int ROW_COUNT = 200;
    File file;

    @BeforeEach
    public void setUp() throws Exception {
        file = File.createTempFile("tmp", "csv");
        try (CSVWriter writer = CSVWriter.of(file, StandardCharsets.UTF_8)) {
            writer.writeRow("id", "id2", "value", "d1");
            for (int i = 0; i < ROW_COUNT; i++) {
                Object[] row = {
                        Integer.valueOf(i),
                        Long.valueOf(1000 + i),
                        "A" + i,
                        BigDecimal.valueOf(i, 1)
                };
                writer.writeRow(row);
            }
        }
    }

    @Test
    public void testStat() throws Exception {
        try (CSVReader reader = CSVReader.of(file, StandardCharsets.UTF_8)) {
            assertEquals(0, reader.rowCount());
            reader.readHeader();
            assertEquals(0, reader.rowCount());
            reader.statAll();
            assertEquals(ROW_COUNT, reader.rowCount());

            assertEquals(Integer.class, reader.getColumnStat(0).getInferType());
            assertEquals(Integer.class, reader.getColumnStat(1).getInferType());
            assertEquals(String.class, reader.getColumnStat(2).getInferType());

            assertSame(reader.getColumnStat(0), reader.getColumnStat("id"));
            assertSame(reader.getColumnStat(1), reader.getColumnStat("id2"));
            assertSame(reader.getColumnStat(2), reader.getColumnStat("value"));
        }
    }

    @Test
    public void testReadAll() throws Exception {
        if (JDKUtils.JVM_VERSION > 11) {
            return;
        }

        int fileRowCount = CSVReader.rowCount(file);
        assertEquals(ROW_COUNT + 1, fileRowCount);

        Schema schema = new Schema(
                asList(
                        new Field("id",
                                FieldType.nullable(new ArrowType.Int(32, true)),
                                /*children*/null
                        ),
                        new Field("id2",
                                FieldType.nullable(new ArrowType.Int(64, true)),
                                /*children*/null
                        ),
                        new Field("value",
                                FieldType.nullable(new ArrowType.Utf8()),
                                /*children*/null
                        ),
                        new Field("d1",
                                FieldType.nullable(new ArrowType.Decimal(9, 2, 64)),
                                /*children*/null
                        )
                ), /*metadata*/ null
        );

        try (
                CSVReader reader = CSVReader.of(file, StandardCharsets.UTF_8);
                BufferAllocator allocator = new RootAllocator();
                VectorSchemaRoot root = VectorSchemaRoot.create(schema, allocator);
        ) {
            reader.readHeader();

            int rowCount = fileRowCount - 1;

            ArrowByteArrayConsumer consumer = new ArrowByteArrayConsumer(root);
            consumer.allocateNew(rowCount);
            reader.readAll(consumer);

            System.out.println("VectorSchemaRoot created: \n" + root.contentToTSVString());
        }
    }
}
