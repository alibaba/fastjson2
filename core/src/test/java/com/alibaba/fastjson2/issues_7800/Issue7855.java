package com.alibaba.fastjson2.issues_7800;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.schema.ArraySchema;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.schema.ObjectSchema;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("regression")
public class Issue7855 {
    @ParameterizedTest
    @CsvSource({"$defs,true", "$defs,false", "definitions,true", "definitions,false"})
    public void objectRoot(String keyword, boolean forward) {
        ObjectSchema schema = (ObjectSchema) JSONSchema.of(
                JSONObject.of("type", "object", keyword, definitions(keyword, forward),
                        "properties", JSONObject.of("data", ref(keyword, "attachments"),
                                "item", ref(keyword, "attachment"))));

        ArraySchema attachments = (ArraySchema) schema.getProperty("data");
        assertSame(schema.getProperty("item"), attachments.getItemSchema());
        assertTrue(schema.isValid(JSONObject.of("data", JSONArray.of(JSONObject.of("id", "a")))));
        assertTrue(schema.isValid(JSONObject.of("data", new JSONArray())));
        assertFalse(schema.isValid(JSONObject.of("data", JSONArray.of(new JSONObject()))));
        assertFalse(schema.isValid(JSONObject.of("data", JSONArray.of(JSONObject.of("id", 1)))));
        assertFalse(schema.isValid(JSONObject.of("data", JSONArray.of("not an attachment"))));
    }

    @ParameterizedTest
    @CsvSource({"$defs,true", "$defs,false", "definitions,true", "definitions,false"})
    public void arrayRoot(String keyword, boolean forward) {
        JSONSchema schema = JSONSchema.of(
                JSONObject.of("type", "array", keyword, definitions(keyword, forward),
                        "items", ref(keyword, "attachments")));

        assertTrue(schema.isValid(JSONArray.of(JSONArray.of(JSONObject.of("id", "a")))));
        assertFalse(schema.isValid(JSONArray.of(JSONArray.of(new JSONObject()))));
        assertFalse(schema.isValid(JSONArray.of(JSONArray.of(JSONObject.of("id", 1)))));
    }

    @ParameterizedTest
    @CsvSource({"$defs,true", "$defs,false", "definitions,true", "definitions,false"})
    public void prefixItems(String keyword, boolean forward) {
        JSONObject defs = definitions(keyword, forward);
        JSONObject attachments = defs.getJSONObject("attachments");
        attachments.remove("items");
        attachments.put("prefixItems", JSONArray.of(ref(keyword, "attachment")));
        JSONSchema schema = JSONSchema.of(JSONObject.of("type", "array", keyword, defs,
                "items", ref(keyword, "attachments")));

        assertTrue(schema.isValid(JSONArray.of(JSONArray.of(JSONObject.of("id", "a")))));
        assertFalse(schema.isValid(JSONArray.of(JSONArray.of(new JSONObject()))));
        assertFalse(schema.isValid(JSONArray.of(JSONArray.of(JSONObject.of("id", 1)))));
    }

    @ParameterizedTest
    @CsvSource({"$defs,true,true", "$defs,false,true", "definitions,true,true", "definitions,false,true",
            "$defs,true,false", "$defs,false,false", "definitions,true,false", "definitions,false,false"})
    public void recursiveDefinitions(String keyword, boolean forward, boolean arrayRoot) {
        JSONObject defs = definitions(keyword, forward);
        defs.getJSONObject("attachment").getJSONObject("properties")
                .put("children", ref(keyword, "attachments"));
        JSONObject input = arrayRoot
                ? JSONObject.of("type", "array", keyword, defs, "items", ref(keyword, "attachment"))
                : JSONObject.of("type", "object", keyword, defs,
                        "properties", JSONObject.of("data", ref(keyword, "attachments")));
        JSONSchema schema = JSONSchema.of(input);

        JSONObject child = JSONObject.of("id", "child");
        JSONObject parent = JSONObject.of("id", "parent", "children", JSONArray.of(child));
        Object value = arrayRoot ? JSONArray.of(parent) : JSONObject.of("data", JSONArray.of(parent));
        assertTrue(schema.isValid(value));
        child.put("id", 1);
        assertFalse(schema.isValid(value));
        child.remove("id");
        assertFalse(schema.isValid(value));
    }

    @Test
    public void separateDefinitionNamespaces() {
        JSONObject definitions = JSONObject.of(
                "values", JSONObject.of("type", "array", "items", ref("definitions", "value")),
                "value", JSONObject.of("type", "integer"));
        JSONObject defs = JSONObject.of(
                "values", JSONObject.of("type", "array", "items", ref("$defs", "value")),
                "value", JSONObject.of("type", "string"));
        JSONSchema schema = JSONSchema.of(JSONObject.of("type", "object",
                "definitions", definitions, "$defs", defs,
                "properties", JSONObject.of("legacy", ref("definitions", "values"),
                        "modern", ref("$defs", "values"))));

        assertTrue(schema.isValid(JSONObject.of("legacy", JSONArray.of(1), "modern", JSONArray.of("a"))));
        assertFalse(schema.isValid(JSONObject.of("legacy", JSONArray.of("a"), "modern", JSONArray.of("a"))));
        assertFalse(schema.isValid(JSONObject.of("legacy", JSONArray.of(1), "modern", JSONArray.of(1))));

        JSONObject missing = JSONObject.of("type", "object", "definitions", new JSONObject(),
                "properties", JSONObject.of("data", ref("definitions", "missing")));
        assertThrows(RuntimeException.class,
                () -> JSONSchema.of(missing).assertValidate(JSONObject.of("data", 1)));
    }

    private static JSONObject definitions(String keyword, boolean forward) {
        JSONObject attachment = JSONObject.of("type", "object",
                "properties", JSONObject.of("id", JSONObject.of("type", "string")),
                "required", JSONArray.of("id"));
        JSONObject attachments = JSONObject.of("type", "array", "items", ref(keyword, "attachment"));
        return forward
                ? JSONObject.of("attachments", attachments, "attachment", attachment)
                : JSONObject.of("attachment", attachment, "attachments", attachments);
    }

    private static JSONObject ref(String keyword, String name) {
        return JSONObject.of("$ref", "#/" + keyword + "/" + name);
    }
}
