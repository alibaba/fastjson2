package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("schema")
public class DurationValidatorTest {
    @Test
    public void test() {
        JSONSchema schema = JSONSchema.of(JSONObject.of("format", "duration"));

        assertFalse(schema.isValid(""));
        assertTrue(schema.isValid("P2D"));
        assertFalse(schema.isValid("1 days"));
    }
}
