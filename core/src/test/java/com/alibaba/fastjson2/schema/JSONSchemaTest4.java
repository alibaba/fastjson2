package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONSchemaTest4 {
    @Test
    public void max() {
        JSONSchema schema = JSONSchema.of(
                JSONObject.of(
                        "properties",
                        JSONObject.of(
                                "age",
                                JSONObject.of("maximum", 120)
                        )
                )
        );

        ValidateResult result = schema.validate(JSONObject.of("age", 121));
        assertTrue(result.getMessage().contains("age"));
    }

    @Test
    public void min() {
        JSONSchema schema = JSONSchema.of(
                JSONObject.of(
                        "properties",
                        JSONObject.of(
                                "age",
                                JSONObject.of("minimum", 0)
                        )
                )
        );

        ValidateResult result = schema.validate(JSONObject.of("age", -10));
        assertTrue(result.getMessage().contains("age"));
    }
}
