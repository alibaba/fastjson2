package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NumberSchemaTest {
    @Test
    public void test() {
        NumberSchema schema = (NumberSchema) JSONSchema.of(
                JSONObject.of(
                        "minimum", 10,
                        "type", "number"
                )
        );
        assertTrue(schema.equals(schema));
        assertFalse(schema.equals(null));
        assertFalse(schema.equals(new Object()));

        assertFalse(
                schema.validate(Long.valueOf(9))
                        .isSuccess()
        );
        assertTrue(
                schema.validate(Long.valueOf(10))
                        .isSuccess()
        );
        assertTrue(
                schema.validate((Long) null)
                        .isSuccess()
        );

        assertFalse(
                schema.validate(Integer.valueOf(9))
                        .isSuccess()
        );
        assertTrue(
                schema.validate(Integer.valueOf(10))
                        .isSuccess()
        );
        assertTrue(
                schema.validate((Integer) null)
                        .isSuccess()
        );

        assertFalse(
                schema.validate(Float.valueOf(9))
                        .isSuccess()
        );
        assertTrue(
                schema.validate(Float.valueOf(10))
                        .isSuccess()
        );
        assertTrue(
                schema.validate((Float) null)
                        .isSuccess()
        );

        assertFalse(
                schema.validate(Double.valueOf(9))
                        .isSuccess()
        );
        assertTrue(
                schema.validate(Double.valueOf(10))
                        .isSuccess()
        );
        assertTrue(
                schema.validate((Double) null)
                        .isSuccess()
        );
    }
}
