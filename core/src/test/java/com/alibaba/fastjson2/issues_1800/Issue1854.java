package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONSchemaValidException;
import com.alibaba.fastjson2.schema.JSONSchema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1854 {
    @Test
    public void test() {
        JSONSchema jsonSchema = JSONSchema.of(JSONObject.of("type", "integer", "maximum", 10));
        try {
            Object o = 11;
            jsonSchema.assertValidate(o);
        } catch (JSONSchemaValidException e) {
            assertEquals("maximum not match, expect <= 10, but 11", e.getMessage());
        }
        try {
            long l = 11L;
            jsonSchema.assertValidate(l);
        } catch (JSONSchemaValidException e) {
            assertEquals("maximum not match, expect <= 10, but 11", e.getMessage());
        }
        try {
            Long l = 11L;
            jsonSchema.assertValidate(l);
        } catch (JSONSchemaValidException e) {
            assertEquals("maximum not match, expect <= 10, but 11", e.getMessage());
        }
        try {
            Integer i = 11;
            jsonSchema.assertValidate(i);
        } catch (JSONSchemaValidException e) {
            assertEquals("maximum not match, expect <= 10, but 11", e.getMessage());
        }
        jsonSchema = JSONSchema.of(JSONObject.of("type", "number", "maximum", 10));
        try {
            Object o = 11;
            jsonSchema.assertValidate(o);
        } catch (JSONSchemaValidException e) {
            assertEquals("maximum not match, expect <= 10, but 11", e.getMessage());
        }
        try {
            long l = 11L;
            jsonSchema.assertValidate(l);
        } catch (JSONSchemaValidException e) {
            assertEquals("maximum not match, expect <= 10, but 11", e.getMessage());
        }
        try {
            double d = 11;
            jsonSchema.assertValidate(d);
        } catch (JSONSchemaValidException e) {
            assertEquals("maximum not match, expect <= 10, but 11.0", e.getMessage());
        }

        jsonSchema = JSONSchema.of(JSONObject.of("type", "integer", "minimum", 10));
        try {
            Object o = 9;
            jsonSchema.assertValidate(o);
        } catch (JSONSchemaValidException e) {
            assertEquals("minimum not match, expect >= 10, but 9", e.getMessage());
        }
        try {
            long l = 9L;
            jsonSchema.assertValidate(l);
        } catch (JSONSchemaValidException e) {
            assertEquals("minimum not match, expect >= 10, but 9", e.getMessage());
        }
        try {
            Long l = 9L;
            jsonSchema.assertValidate(l);
        } catch (JSONSchemaValidException e) {
            assertEquals("minimum not match, expect >= 10, but 9", e.getMessage());
        }
        try {
            Integer i = 9;
            jsonSchema.assertValidate(i);
        } catch (JSONSchemaValidException e) {
            assertEquals("minimum not match, expect >= 10, but 9", e.getMessage());
        }

        jsonSchema = JSONSchema.of(JSONObject.of("type", "number", "minimum", 10));
        try {
            Object o = 9;
            jsonSchema.assertValidate(o);
        } catch (JSONSchemaValidException e) {
            assertEquals("minimum not match, expect >= 10, but 9", e.getMessage());
        }
        try {
            long l = 9L;
            jsonSchema.assertValidate(l);
        } catch (JSONSchemaValidException e) {
            assertEquals("minimum not match, expect >= 10, but 9", e.getMessage());
        }
        try {
            double d = 9;
            jsonSchema.assertValidate(d);
        } catch (JSONSchemaValidException e) {
            assertEquals("minimum not match, expect >= 10, but 9.0", e.getMessage());
        }
    }
}
