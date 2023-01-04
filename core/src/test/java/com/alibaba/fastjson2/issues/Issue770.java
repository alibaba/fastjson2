package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.schema.ValidateResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue770 {
    @Test
    public void test() {
        String schemaStr = "{\n" +
                "  \"required\":[\"memory\"],\n" +
                "  \"properties\": {\n" +
                "    \"memory\": {\n" +
                "      \"type\":\"string\",\n" +
                "      \"pattern\": \"^([1-9][0-9]{0,1}|100)$\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JSONObject js = JSONObject.of("memory", "101");
        JSONSchema jsonSchema = JSONSchema.parseSchema(schemaStr);
        ValidateResult vr = jsonSchema.validate(js);
        assertFalse(vr.isSuccess());
        assertNotNull(vr.getMessage());
    }
}
