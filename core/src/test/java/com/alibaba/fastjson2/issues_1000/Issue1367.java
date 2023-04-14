package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.schema.JSONSchema;
import org.junit.jupiter.api.Test;

public class Issue1367 {
    @Test
    public void test() {
        String schemaStr = "{\"type\": \"object\",\"properties\": {\"姓\": {\"type\": \"string\",\"enum\": [\"1\", \"2\"]}}}";
        String schemaStr2 = "{\"type\":\"object\",\"properties\":{\"姓\":{\"type\":\"string\",\"const\":\"1\"}}}";
        JSONSchema schema = JSONSchema.of(JSON.parseObject(schemaStr));
        JSONSchema schema2 = JSONSchema.of(JSON.parseObject(schemaStr2));
        String jsonStr = "{\"姓\": \"3\"}";
        System.out.println(schema.isValid(JSON.parseObject(jsonStr)));
        System.out.println(schema2.isValid(JSON.parseObject(jsonStr)));
    }
}
