package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.net.URL;

public class JSONSchemaResourceTest {
//    @Test
//    public void test_items() throws Exception{
//        runTest("schema/draft4/items.json");
//    }
//
//    @Test
//    public void test_additionalItems() throws Exception{
//        runTest("schema/draft4/additionalItems.json");
//    }

   @Test
    public void test0() throws Exception{
        runTest("schema/draft4/maximum.json");
    }

    @Test
    public void test1() throws Exception{
        runTest("schema/draft4/maxItems.json");
    }

    public void runTest(String path) {
        URL url = JSONSchemaResourceTest.class.getClassLoader().getResource(path);
        JSONArray array = JSON.parseArray(url);
        System.out.println(array);

        for (int i = 0; i < array.size(); i++) {
            JSONObject item = array.getJSONObject(i);

            JSONSchema schema = JSONSchema.of(
                    item.getJSONObject("schema")
            );

            JSONArray tests = item.getJSONArray("tests");
            for (int j = 0; j < tests.size(); j++) {
                JSONObject test = tests.getJSONObject(j);

                String description = test.getString("description");
                boolean valid = test.getBooleanValue("valid");
                Object data = test.get("data");
                JSONSchema.ValidateResult result = schema.validate(data);
                if (result.isSuccess() != valid) {
                    throw new JSONSchemaValidException("schema " + i + ", test " + j + " valid not match, " + description + ", cause " + result.getMessage());
                }
            }
        }
    }
}
