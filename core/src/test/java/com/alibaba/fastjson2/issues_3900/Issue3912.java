package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.schema.ValidateResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Issue3912 {
    static final String basicSchemaStr = "{\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"f1\": { \"type\": \"boolean\" },\n" +
            "    \"f2\": { \"type\": \"integer\" }\n" +
            "  }\n" +
            "}";
    static final JSONSchema basicSchema = JSONSchema.parseSchema(basicSchemaStr);
    static final JSONObject basicBadData = JSONObject.of("f1", "wrong", "f2", "wrong");

    @Test
    public void testHandler_ReturnTrue_CollectsAll() {
        List<String> errors = new ArrayList<>();
        basicSchema.validate(basicBadData, (s, v, m, p) -> {
            errors.add(p);
            return true; // 继续校验
        });
        assertEquals(2, errors.size());
        assertTrue(errors.contains("$.f1"));
        assertTrue(errors.contains("$.f2"));
    }

    @Test
    public void testHandler_ReturnFalse_FailFast() {
        List<String> errors = new ArrayList<>();
        basicSchema.validate(basicBadData, (s, v, m, p) -> {
            errors.add(p);
            return false; // 立即停止
        });
        assertEquals(1, errors.size());
    }

    static final String complexSchemaStr = "{\n" +
            "  \"type\": \"object\",\n" +
            "  \"required\": [\"orderId\"],\n" +
            "  \"properties\": {\n" +
            "    \"orderId\": { \"type\": \"string\", \"pattern\": \"^ORD-\\\\d+$\" },\n" +
            "    \"products\": {\n" +
            "      \"type\": \"array\",\n" +
            "      \"items\": {\n" +
            "        \"type\": \"object\",\n" +
            "        \"required\": [\"name\", \"price\"],\n" +
            "        \"properties\": {\n" +
            "          \"name\": { \"type\": \"string\" },\n" +
            "          \"price\": { \"type\": \"integer\", \"minimum\": 0 }\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"meta\": {\n" +
            "       \"type\": \"object\",\n" +
            "       \"properties\": {\n" +
            "           \"status\": { \"enum\": [\"active\", \"inactive\"] }\n" +
            "       }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    static final JSONSchema complexSchema = JSONSchema.parseSchema(complexSchemaStr);

    @Test
    public void testComplexValidation_CollectsAllTypesOfErrors() {
        JSONObject data = JSONObject.of(
                // 错误1: orderId 格式不对 (Regex mismatch)
                "orderId", "INVALID-ID",

                "products", JSONArray.of(
                        JSONObject.of("name", "Apple", "price", 10),
                        // 错误2: 缺少必填项 price (Missing required)
                        JSONObject.of("name", "Banana"),
                        // 错误3: price 为负数 (Minimum violation)
                        JSONObject.of("name", "Orange", "price", -5)
                ),

                "meta", JSONObject.of(
                        // 错误4: 枚举值错误 (Enum violation)
                        "status", "deleted"
                )
        );

        List<String> errorPaths = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        ValidateResult result = complexSchema.validate(data, (parent, value, msg, path) -> {
            errorPaths.add(path);
            errorMessages.add(msg);
            return true;
        });

        assertFalse(result.isSuccess());
        assertEquals(4, errorPaths.size(), "Should detect exactly 4 errors");

        assertTrue(errorPaths.contains("$.orderId"), "Missing regex error");

        boolean hasMissingField = errorMessages.stream().anyMatch(m -> m.contains("price"));
        assertTrue(hasMissingField, "Missing required field error for product[1]");

        assertTrue(errorPaths.contains("$.products[2].price"), "Missing range error for product[2]");
        assertTrue(errorPaths.contains("$.meta.status"), "Missing enum error");
    }

    @Test
    public void testMissingTopLevelRequiredField() {
        // 缺少顶层 orderId
        JSONObject data = JSONObject.of(
                "products", JSONArray.of()
        );

        List<String> errors = new ArrayList<>();
        complexSchema.validate(data, (s, v, m, p) -> {
            errors.add(m);
            return true;
        });

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("orderId"));
    }

    static final String dependentSchemaStr = "{\n" +
            "  \"type\": \"object\",\n" +
            "  \"dependentRequired\": {\n" +
            "    \"creditCard\": [\"billingAddress\"]\n" +
            "  }\n" +
            "}";
    static final JSONSchema dependentSchema = JSONSchema.parseSchema(dependentSchemaStr);

    @Test
    public void testDependentRequired_PassesNullToHandler() {
        // 场景：有 creditCard，但没有 billingAddress
        // 验证目标：Handler 接收到的 value 必须是 null (表示缺失)，path 必须指向 billingAddress
        JSONObject data = JSONObject.of("creditCard", "1234-5678");

        List<String> errors = new ArrayList<>();
        dependentSchema.validate(data, (s, value, msg, path) -> {
            if (path.endsWith("billingAddress")) {
                if (value == null) {
                    errors.add("OK: Value is null for " + path);
                } else {
                    errors.add("FAIL: Value should be null, but got: " + value);
                }
            }
            return true;
        });

        assertEquals(1, errors.size());
        assertEquals("OK: Value is null for $.billingAddress", errors.get(0));
    }

    static final String noAdditionalPropertiesSchemaStr = "{\n" +
            "  \"type\": \"object\",\n" +
            "  \"additionalProperties\": false,\n" +
            "  \"properties\": {\n" +
            "    \"allowed\": { \"type\": \"string\" }\n" +
            "  }\n" +
            "}";
    static final JSONSchema noAdditionalPropertiesSchema = JSONSchema.parseSchema(noAdditionalPropertiesSchemaStr);

    @Test
    public void testAdditionalProperties_PassesActualValueToHandler() {
        // 场景：additionalProperties: false，传入了多余字段
        // 验证目标：Handler 接收到的 value 应该是那个多余字段的值 ("forbiddenValue")
        JSONObject data = JSONObject.of(
                "allowed", "ok",
                "extraField", "forbiddenValue"
        );

        List<String> collectedValues = new ArrayList<>();
        noAdditionalPropertiesSchema.validate(data, (s, value, msg, path) -> {
            if (path.endsWith("extraField")) {
                collectedValues.add((String) value);
            }
            return true;
        });

        assertEquals(1, collectedValues.size());
        assertEquals("forbiddenValue", collectedValues.get(0), "Handler should receive the value of the extra property");
    }

    static final String arrayUniqueSchemaStr = "{\n" +
            "  \"type\": \"array\",\n" +
            "  \"uniqueItems\": true\n" +
            "}";
    static final JSONSchema arrayUniqueSchema = JSONSchema.parseSchema(arrayUniqueSchemaStr);

    @Test
    public void testArrayUniqueItems_PassesDuplicateValueToHandler() {
        // 场景：数组中有重复项
        // 验证目标：Handler 接收到的 path 应该是具体的下标，value 应该是那个重复的值
        JSONArray data = JSONArray.of(10, 20, 10); // 10 重复

        List<String> errors = new ArrayList<>();
        arrayUniqueSchema.validate(data, (s, value, msg, path) -> {
            errors.add(String.format("Path:%s, Value:%s", path, value));
            return true;
        });

        assertEquals(1, errors.size());
        assertEquals("Path:$[2], Value:10", errors.get(0));
    }
}
