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
    public void test_draft2020_boolean_schema() {
        runTest("schema/draft2020-12/boolean_schema.json");
    }
//
//    @Test
//    public void test_draft2020_const() {
//        runTest("schema/draft2020-12/const.json");
//    }

    @Test
    public void test_draft2020_enum() {
        runTest("schema/draft2020-12/enum.json");
    }

    @Test
    public void test_draft2020_exclusiveMaximum() {
        runTest("schema/draft2020-12/exclusiveMaximum.json");
    }

    @Test
    public void test_draft2020_exclusiveMinimum() {
        runTest("schema/draft2020-12/exclusiveMinimum.json");
    }

    @Test
    public void test_draft2020_format() {
        runTest("schema/draft2020-12/format.json");
    }
//
//    @Test
//    public void test_draft2020_if_then_else() {
//        runTest("schema/draft2020-12/if-then-else.json");
//    }

    @Test
    public void test_draft2020_items() {
        runTest("schema/draft2020-12/items.json");
    }

    @Test
    public void test_draft2020_maxContains() {
        runTest("schema/draft2020-12/maxContains.json");
    }

    @Test
    public void test_draft2020_maximum() {
        runTest("schema/draft2020-12/maximum.json");
    }

    @Test
    public void test_draft2020_maxItems() {
        runTest("schema/draft2020-12/maxItems.json");
    }

    @Test
    public void test_draft2020_maxLength() {
        runTest("schema/draft2020-12/maxLength.json");
    }

    @Test
    public void test_draft2020_maxProperties() {
        runTest("schema/draft2020-12/maxProperties.json");
    }

    @Test
    public void test_draft2020_minContains() {
        runTest("schema/draft2020-12/minContains.json");
    }

    @Test
    public void test_draft2020_minimum() {
        runTest("schema/draft2020-12/minimum.json");
    }

    @Test
    public void test_draft2020_minItems() {
        runTest("schema/draft2020-12/minItems.json");
    }

    @Test
    public void test_draft2020_minLength() {
        runTest("schema/draft2020-12/minLength.json");
    }

    @Test
    public void test_draft2020_minProperties() {
        runTest("schema/draft2020-12/minProperties.json");
    }

    @Test
    public void test_draft2020_multipleOf() {
        runTest("schema/draft2020-12/multipleOf.json");
    }

    @Test
    public void test_draft2020_not() {
        runTest("schema/draft2020-12/not.json");
    }

    @Test
    public void test_draft2020_oneOf() {
        runTest("schema/draft2020-12/oneOf.json");
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
    public void test_prefixItems() {
        runTest("schema/draft2020-12/prefixItems.json");
    }

    @Test
    public void test_properties() {
        runTest("schema/draft2020-12/properties.json");
    }

    @Test
    public void test_propertyNames() {
        runTest("schema/draft2020-12/propertyNames.json");
    }
//
//    @Test
//    public void test_ref() {
//        runTest("schema/draft2020-12/ref.json");
//    }

    @Test
    public void test_draft2020_required() {
        runTest("schema/draft2020-12/required.json");
    }

    @Test
    public void test_draft2020_type() {
        runTest("schema/draft2020-12/type.json");
    }

    @Test
    public void test_draft2020_uniqueItems() {
        runTest("schema/draft2020-12/uniqueItems.json");
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
        Object object = item.get("schema");

        JSONSchema schema;
        try {
            schema = JSONSchema.parseSchema(
                    JSON.toJSONString(object));
        } catch (Exception ex) {
            throw new IllegalStateException("parse schema " + i + " error : " + object, ex);
        }

        JSONArray tests = item.getJSONArray("tests");
        for (int j = 0; j < tests.size(); j++) {
            validate(i, j, object, schema, tests);
        }
    }

    private void validate(int i, int j, Object object, JSONSchema schema, JSONArray tests) {
        JSONObject test = tests.getJSONObject(j);

        String description = test.getString("description");
        boolean valid = test.getBooleanValue("valid");
        Object data = test.get("data");

        ValidateResult result;
        try {
            result = schema.validate(data);
        } catch (Exception ex) {
            throw new JSONSchemaValidException("schema " + i + ", test " + j + " valid error, " + description);
        }

        if (result.isSuccess() != valid) {
            System.out.println(object);
            System.out.println("============================== expect " + valid);
            System.out.println(JSON.toJSONString(data, JSONWriter.Feature.WriteNulls));
            throw new JSONSchemaValidException("schema " + i + ", test " + j + " valid not match, " + description + ", cause " + result.getMessage());
        }
    }
}
