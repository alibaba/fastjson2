package com.alibaba.fastjson2;

import com.alibaba.fastjson2.schema.JSONSchema;
import org.junit.jupiter.api.Test;

public class JSONSchemaGenClassTest {
    @Test
    public void test() {
        JSONObject object = JSON.parseObject(JSONSchemaGenClassTest.class.getClassLoader().getResource("data/twitter.json"));
        JSONSchema jsonSchema = JSONSchema.ofValue(object);
        System.out.println(JSON.toJSONString(jsonSchema, JSONWriter.Feature.PrettyFormat));
    }
}
