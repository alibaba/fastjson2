package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBReadAnyTest {
    @Test
    public void test_jsonb_float_0() {
        Object[] values = new Object[]{
                1.1F, 1.1D, 2, 3D,
                new BigInteger("1234567890123"),
                new BigDecimal("1234567890123"),
                UUID.randomUUID().toString(),
                "中国"
        };

        for (Object value : values) {
            assertEquals(value,
                    JSONB.parse(
                            JSONB.toBytes(value)));
            assertEquals(value,
                    JSONReader
                            .ofJSONB(
                                    JSONB.toBytes(value))
                            .readAny());

            assertEquals(value,
                    JSONB.parse(
                            JSONB.toBytes(value, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName),
                            JSONReader.Feature.SupportAutoType
                    ));
        }
    }
}
