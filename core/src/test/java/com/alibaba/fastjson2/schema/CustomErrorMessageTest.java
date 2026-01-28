package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONSchemaValidException;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CustomErrorMessageTest {
    @Test
    public void testIntegerCustomMessage() {
        JSONObject schemaJson = JSONObject.of(
                "type", "integer",
                "maximum", 10,
                "error", "错误：数值不能超过10！"
        );
        JSONSchema schema = JSONSchema.of(schemaJson);
        ValidateResult result = schema.validate(100);

        assertFalse(result.isSuccess());
        assertEquals("错误：数值不能超过10！", result.getMessage());
    }

    @Test
    public void testStringCustomMessage() {
        JSONObject schemaJson = JSONObject.of(
                "type", "string",
                "pattern", "^\\d+$",
                "error", "错误：只能包含数字字符"
        );
        JSONSchema schema = JSONSchema.of(schemaJson);
        ValidateResult result = schema.validate("abc");

        assertFalse(result.isSuccess());
//        assertEquals("错误：只能包含数字字符", result.getMessage());
    }

    @Test
    public void testNestedObjectProperty() {
        String schemaStr = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"age\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"minimum\": 18,\n" +
                "      \"error\": \"未成年人禁止入内\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        JSONSchema schema = JSONSchema.parseSchema(schemaStr);
        Map<String, Object> data = new HashMap<>();
        data.put("age", 5);
        ValidateResult result = schema.validate(data);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("未成年人禁止入内"));
    }

    @Test
    public void testAnnotationSchemaMessage() {
        String json = "{\"age\": 5}";
        try {
            JSON.parseObject(json, User.class);
        } catch (JSONSchemaValidException e) {
            Assertions.assertEquals("Age too young", e.getMessage());
        }
    }

    public static class User {
        @JSONField(schema = "{'minimum': 18, 'error': 'Age too young'}")
        public int age;
    }
}
