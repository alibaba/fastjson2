package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A schema export has to describe the same constraints as the schema it came from, otherwise the
 * restored schema silently accepts values the original rejected.
 * <details><summary>中文</summary>
 * 模式导出后必须表达与原始模式相同的约束，否则还原得到的模式会静默接受原模式拒绝的值。
 * </details>
 */
@Tag("schema")
public class CombinationKeywordRoundTripTest {
    static JSONSchema restored(String text) {
        JSONSchema original = JSONSchema.parseSchema(text);
        return JSONSchema.parseSchema(original.toString());
    }

    /**
     * Same schema, restored from the exported JSON object instead of the exported JSON text:
     * {@code toJSONObject()} output has to be accepted by {@code JSONSchema.of()}.
     * <details><summary>中文</summary>
     * 与 {@link #restored} 相同，但改为从导出的 JSON 对象还原，即 {@code toJSONObject()} 的结果必须能被
     * {@code JSONSchema.of()} 接受。
     * </details>
     */
    static JSONSchema restoredInMemory(String text) {
        JSONSchema original = JSONSchema.parseSchema(text);
        return JSONSchema.of(original.toJSONObject());
    }

    @Test
    public void notConstraintSurvivesRoundTrip() {
        JSONSchema schema = restored("{\"not\":{\"type\":\"string\"}}");
        assertTrue(schema.validate(123).isSuccess());
        assertFalse(schema.validate("s").isSuccess());
    }

    @Test
    public void allOfConstraintSurvivesRoundTrip() {
        JSONSchema schema = restored("{\"allOf\":[{\"type\":\"integer\"},{\"minimum\":5}]}");
        assertTrue(schema.validate(7).isSuccess());
        assertFalse(schema.validate(3).isSuccess());
    }

    @Test
    public void oneOfConstraintSurvivesRoundTrip() {
        JSONSchema schema = restored("{\"oneOf\":[{\"type\":\"integer\"},{\"type\":\"string\"}]}");
        assertTrue(schema.validate(7).isSuccess());
        assertFalse(schema.validate(1.5D).isSuccess());
    }

    @Test
    public void enumConstraintSurvivesRoundTrip() {
        JSONSchema schema = restored("{\"enum\":[1,2]}");
        assertTrue(schema.validate(1).isSuccess());
        assertFalse(schema.validate(3).isSuccess());
    }

    @Test
    public void nullTypeSurvivesRoundTrip() {
        JSONSchema schema = restored("{\"type\":\"null\"}");
        assertTrue(schema.validate((Object) null).isSuccess());
        assertFalse(schema.validate(1).isSuccess());
    }

    @Test
    public void nestedNotConstraintSurvivesRoundTrip() {
        JSONSchema schema = restored("{\"type\":\"object\",\"properties\":{\"x\":{\"not\":{\"type\":\"string\"}}}}");
        assertTrue(schema.validate(JSON.parseObject("{\"x\":1}")).isSuccess());
        assertFalse(schema.validate(JSON.parseObject("{\"x\":\"s\"}")).isSuccess());
    }

    /**
     * The export of a combination keyword keeps live child schemas, so restoring it in memory has to
     * work as well as restoring the exported text.
     * <details><summary>中文</summary>
     * 组合关键字的导出结果保留了子模式对象，因此内存还原必须与文本还原同样可用。
     * </details>
     */
    @Test
    public void anyOfInMemoryRoundTrip() {
        JSONSchema original = JSONSchema.parseSchema("{\"anyOf\":[{\"type\":\"integer\"},{\"type\":\"string\"}]}");

        JSONSchema fromText = JSONSchema.parseSchema(original.toString());
        assertTrue(fromText.validate(1).isSuccess());
        assertFalse(fromText.validate(1.5D).isSuccess());

        JSONSchema fromObject = JSONSchema.of(original.toJSONObject());
        assertTrue(fromObject.validate(1).isSuccess());
        assertFalse(fromObject.validate(1.5D).isSuccess());
    }

    @Test
    public void combinationKeywordsSurviveInMemoryRoundTrip() {
        JSONSchema not = restoredInMemory("{\"not\":{\"type\":\"string\"}}");
        assertTrue(not.validate(123).isSuccess());
        assertFalse(not.validate("s").isSuccess());

        // the type-array shortcut of "not" has to survive as well
        JSONSchema notTypes = restoredInMemory("{\"not\":{\"type\":[\"integer\"]}}");
        assertFalse(notTypes.validate(1).isSuccess());
        assertTrue(notTypes.validate("s").isSuccess());
        assertTrue(notTypes.validate(1.5D).isSuccess());

        JSONSchema allOf = restoredInMemory("{\"allOf\":[{\"type\":\"integer\"},{\"minimum\":5}]}");
        assertTrue(allOf.validate(7).isSuccess());
        assertFalse(allOf.validate(3).isSuccess());

        JSONSchema oneOf = restoredInMemory("{\"oneOf\":[{\"type\":\"integer\"},{\"type\":\"string\"}]}");
        assertTrue(oneOf.validate(7).isSuccess());
        assertFalse(oneOf.validate(1.5D).isSuccess());

        JSONSchema enumSchema = restoredInMemory("{\"enum\":[1,2]}");
        assertTrue(enumSchema.validate(1).isSuccess());
        assertFalse(enumSchema.validate(3).isSuccess());

        JSONSchema nullType = restoredInMemory("{\"type\":\"null\"}");
        assertTrue(nullType.validate((Object) null).isSuccess());
        assertFalse(nullType.validate(1).isSuccess());
    }

    @Test
    public void booleanSubschemasSurviveRoundTrip() {
        // boolean subschemas are kept as Any / not-Any by the combination constructors
        assertTrue(restored("{\"allOf\":[true,{\"type\":\"integer\"}]}").validate(1).isSuccess());
        assertFalse(restored("{\"allOf\":[true,{\"type\":\"integer\"}]}").validate("s").isSuccess());
        assertTrue(restored("{\"anyOf\":[false,{\"type\":\"string\"}]}").validate("s").isSuccess());
        assertFalse(restored("{\"anyOf\":[false,{\"type\":\"string\"}]}").validate(1).isSuccess());
        assertFalse(restored("{\"not\":true}").validate(1).isSuccess());
        assertTrue(restored("{\"not\":false}").validate(1).isSuccess());

        assertTrue(restoredInMemory("{\"allOf\":[true,{\"type\":\"integer\"}]}").validate(1).isSuccess());
        assertFalse(restoredInMemory("{\"allOf\":[true,{\"type\":\"integer\"}]}").validate("s").isSuccess());
        assertTrue(restoredInMemory("{\"anyOf\":[false,{\"type\":\"string\"}]}").validate("s").isSuccess());
        assertFalse(restoredInMemory("{\"anyOf\":[false,{\"type\":\"string\"}]}").validate(1).isSuccess());
        assertFalse(restoredInMemory("{\"not\":true}").validate(1).isSuccess());
        assertTrue(restoredInMemory("{\"not\":false}").validate(1).isSuccess());
    }

    @Test
    public void nestedCombinationsSurviveRoundTrip() {
        String inProperties = "{\"type\":\"object\",\"properties\":{\"x\":{\"anyOf\":[{\"type\":\"integer\"},{\"type\":\"string\"}]}}}";
        assertTrue(restored(inProperties).validate(JSON.parseObject("{\"x\":1}")).isSuccess());
        assertFalse(restored(inProperties).validate(JSON.parseObject("{\"x\":1.5}")).isSuccess());
        assertTrue(restoredInMemory(inProperties).validate(JSON.parseObject("{\"x\":1}")).isSuccess());
        assertFalse(restoredInMemory(inProperties).validate(JSON.parseObject("{\"x\":1.5}")).isSuccess());

        String inAllOf = "{\"allOf\":[{\"not\":{\"type\":\"string\"}},{\"type\":\"integer\"}]}";
        assertTrue(restored(inAllOf).validate(1).isSuccess());
        assertFalse(restored(inAllOf).validate(1.5D).isSuccess());
        assertFalse(restored(inAllOf).validate("s").isSuccess());
        assertTrue(restoredInMemory(inAllOf).validate(1).isSuccess());
        assertFalse(restoredInMemory(inAllOf).validate(1.5D).isSuccess());
        assertFalse(restoredInMemory(inAllOf).validate("s").isSuccess());
    }

    @Test
    public void notTypeArrayExportsLowercaseKeywords() {
        String text = "{\"not\":{\"type\":[\"integer\",\"null\"]}}";
        JSONSchema original = JSONSchema.parseSchema(text);

        JSONArray exportedTypes = original.toJSONObject().getJSONObject("not").getJSONArray("type");
        assertEquals(2, exportedTypes.size());
        assertEquals("integer", exportedTypes.getString(0));
        assertEquals("null", exportedTypes.getString(1));

        JSONSchema fromText = JSONSchema.parseSchema(original.toString());
        JSONSchema fromObject = JSONSchema.of(original.toJSONObject());
        for (JSONSchema schema : new JSONSchema[]{original, fromText, fromObject}) {
            assertFalse(schema.validate(1).isSuccess());             // integer rejected
            assertFalse(schema.validate((Object) null).isSuccess()); // null rejected, no NPE
            assertTrue(schema.validate("s").isSuccess());            // string accepted
        }
    }

    @Test
    public void capitalizedNotTypeArrayNormalizesOnExport() {
        JSONSchema schema = JSONSchema.parseSchema("{\"not\":{\"type\":[\"Integer\",\"Null\"]}}");

        JSONArray exportedTypes = schema.toJSONObject().getJSONObject("not").getJSONArray("type");
        assertEquals(2, exportedTypes.size());
        assertEquals("integer", exportedTypes.getString(0));
        assertEquals("null", exportedTypes.getString(1));

        assertFalse(schema.validate(1).isSuccess());
        assertFalse(schema.validate((Object) null).isSuccess());
        assertTrue(schema.validate("s").isSuccess());

        JSONSchema fromText = JSONSchema.parseSchema(schema.toString());
        assertFalse(fromText.validate(1).isSuccess());
        assertFalse(fromText.validate((Object) null).isSuccess());
        assertTrue(fromText.validate("s").isSuccess());
    }
}
