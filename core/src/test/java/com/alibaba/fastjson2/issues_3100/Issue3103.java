package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.schema.ValidateResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue3103 {
    @Test
    public void test() {
        String objectStr = "{\n" +
                "\t\"name\": \"John Doe\",\n" +
                "\t\"age\": 41.5\n" +
                "}";
        String schemaStr = "{\n" +
                "\t\"$defs\": {\n" +
                "\t\t\"ageLimit\": {\n" +
                "\t\t\t\"minimum\": 18,\n" +
                "\t\t\t\"maximum\": 60\n" +
                "\t\t},\n" +
                "\t\t\"ageType\": {\n" +
                "\t\t\t\"type\": \"integer\"\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"type\": \"object\",\n" +
                "\t\"properties\": {\n" +
                "\t\t\"name\": {\n" +
                "\t\t\t\"type\": \"string\"\n" +
                "\t\t},\n" +
                "\t\t\"age\": {\n" +
                "\t\t\t\"allOf\": [{\n" +
                "\t\t\t\t\"$ref\": \"#/$defs/ageLimit\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"$ref\": \"#/$defs/ageType\"\n" +
                "\t\t\t}]\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"required\": [\"name\"]\n" +
                "}";
        JSONSchema schema = JSONSchema.of(JSON.parseObject(schemaStr));
        ValidateResult result = schema.validate(JSONObject.parseObject(objectStr));
        assertFalse(result.isSuccess());
    }
}
