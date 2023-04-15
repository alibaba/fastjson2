package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class EnumSchemaTest {
    @Test
    public void test() {
        EnumSchema schema = new EnumSchema(
                new BigDecimal("12.0"),
                BigDecimal.valueOf(2147483649L),
                new BigDecimal("214748364921474836492147483649"),
                new BigDecimal("12.34")
        );
        assertEquals(JSONSchema.Type.Enum, schema.getType());
        assertTrue(
                schema.validate(
                        BigDecimal.valueOf(12)
                        ).isSuccess()
        );
        assertTrue(
                schema.validate(2147483649L)
                        .isSuccess()
        );
        assertTrue(
                schema.validate(BigDecimal.valueOf(2147483649L))
                        .isSuccess()
        );
        assertTrue(
                schema.validate(BigInteger.valueOf(2147483649L))
                        .isSuccess()
        );

        assertTrue(
                schema.validate(12)
                        .isSuccess()
        );
        assertTrue(
                schema.validate(12L)
                        .isSuccess()
        );
        assertTrue(
                schema.validate(BigDecimal.valueOf(12))
                        .isSuccess()
        );
        assertTrue(
                schema.validate(BigInteger.valueOf(12))
                        .isSuccess()
        );

        assertTrue(
                schema.validate(
                        new BigInteger("214748364921474836492147483649")
                        ).isSuccess()
        );
        assertTrue(
                schema.validate(
                        new BigDecimal("214748364921474836492147483649")
                        ).isSuccess()
        );
        assertTrue(
                schema.validate(
                        new BigDecimal("12.34")
                ).isSuccess()
        );
    }

    @Test
    public void testStr() {
        JSONSchema schema = JSONSchema.of(
                JSONObject.of(
                        "enum",
                        JSONArray.of("big", "medium", "small")
                )
        );
        assertEquals(JSONSchema.Type.String, schema.getType());
        assertFalse(schema.isValid("abc"));
        assertTrue(schema.isValid("big"));
        assertTrue(schema.isValid("medium"));
        assertTrue(schema.isValid("small"));
    }
}
