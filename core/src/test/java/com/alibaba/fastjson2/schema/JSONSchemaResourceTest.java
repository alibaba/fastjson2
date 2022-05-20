package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.net.URL;

public class JSONSchemaResourceTest {
    @Test
    public void test_draft2020_additionalProperties() {
        runTest("schema/draft2020-12/additionalProperties.json");
    }

    @Test
    public void test_draft2020_allOf() {
        runTest("schema/draft2020-12/allOf.json");
    }

    @Test
    public void test_draft2020_anyOf() {
        runTest("schema/draft2020-12/anyOf.json");
    }

    @Test
    public void test_draft2020_items() {
        runTest("schema/draft2020-12/items.json");
    }

    @Test
    public void test_draft2020_not() {
        runTest("schema/draft2020-12/not.json");
    }

    @Test
    public void test_draft2020_multipleOf() {
        runTest("schema/draft2020-12/multipleOf.json");
    }

    @Test
    public void test_pattern() {
        runTest("schema/draft2020-12/pattern.json");
    }

    @Test
    public void test_patternProperties() {
        runTest("schema/draft2020-12/patternProperties.json");
    }

    @Test
    public void test_draft2020_type() {
        runTest("schema/draft2020-12/type.json");
    }

    @Test
    public void test_draft2020_uniqueItems() {
        runTest("schema/draft2020-12/uniqueItems.json");
    }

    @Test
    public void test_draft2020_maximum() throws Exception {
        runTest("schema/draft2020-12/maximum.json");
    }

    @Test
    public void test_draft2020_maxItems() throws Exception {
        runTest("schema/draft2020-12/maxItems.json");
    }

    public void runTest(String path) {
        URL url = JSONSchemaResourceTest.class.getClassLoader().getResource(path);
        JSONArray array = JSON.parseArray(url);

        for (int i = 0; i < array.size(); i++) {
            validate(array, i);
        }
    }

    private void validate(JSONArray array, int i) {
        JSONObject item = array.getJSONObject(i);
        JSONObject object = item.getJSONObject("schema");

        JSONSchema schema;
        try {
            schema = JSONSchema.of(object);
        } catch (Exception ex) {
            throw new IllegalStateException("parse schema " + i + " error : " + object, ex);
        }

        JSONArray tests = item.getJSONArray("tests");
        for (int j = 0; j < tests.size(); j++) {
            validate(i, j, object, schema, tests);
        }
    }

    private void validate(int i, int j, JSONObject object, JSONSchema schema, JSONArray tests) {
        JSONObject test = tests.getJSONObject(j);

        String description = test.getString("description");
        boolean valid = test.getBooleanValue("valid");
        Object data = test.get("data");
        ValidateResult result = schema.validate(data);
        if (result.isSuccess() != valid) {
            System.out.println(object);
            System.out.println("============================== expect " + valid);
            System.out.println(JSON.toJSONString(data, JSONWriter.Feature.WriteNulls));
            throw new JSONSchemaValidException("schema " + i + ", test " + j + " valid not match, " + description + ", cause " + result.getMessage());
        }
    }
}
