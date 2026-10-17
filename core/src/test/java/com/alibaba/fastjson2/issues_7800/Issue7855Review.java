package com.alibaba.fastjson2.issues_7800;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.schema.JSONSchema;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("regression")
public class Issue7855Review {
    @ParameterizedTest
    @CsvSource({"$defs,false", "$defs,true", "definitions,false", "definitions,true"})
    public void additionalProperties(String keyword, boolean arrayRoot) {
        JSONObject container = JSONObject.of("type", "object",
                "additionalProperties", ref(keyword, "value"));
        JSONSchema schema = schema(keyword, arrayRoot, JSONObject.of(
                "container", container, "value", JSONObject.of("type", "string")));

        assertTrue(schema.isValid(value(arrayRoot, JSONObject.of("extra", "ok"))));
        assertFalse(schema.isValid(value(arrayRoot, JSONObject.of("extra", 1))));
    }

    @ParameterizedTest
    @CsvSource({"$defs,false", "$defs,true", "definitions,false", "definitions,true"})
    public void patternProperties(String keyword, boolean arrayRoot) {
        JSONObject container = JSONObject.of("type", "object",
                "patternProperties", JSONObject.of("^value", ref(keyword, "value")));
        JSONSchema schema = schema(keyword, arrayRoot, JSONObject.of(
                "container", container, "value", JSONObject.of("type", "string")));

        assertTrue(schema.isValid(value(arrayRoot, JSONObject.of("value1", "ok"))));
        assertFalse(schema.isValid(value(arrayRoot, JSONObject.of("value1", 1))));
    }

    @ParameterizedTest
    @CsvSource({"$defs,false,allOf", "$defs,true,allOf", "definitions,false,allOf", "definitions,true,allOf",
            "$defs,false,anyOf", "$defs,true,anyOf", "definitions,false,anyOf", "definitions,true,anyOf",
            "$defs,false,oneOf", "$defs,true,oneOf", "definitions,false,oneOf", "definitions,true,oneOf"})
    public void combinators(String keyword, boolean arrayRoot, String combinator) {
        JSONArray options = JSONArray.of(ref(keyword, "value"));
        if (!"allOf".equals(combinator)) {
            options.add(JSONObject.of("type", "integer"));
        }
        JSONSchema schema = schema(keyword, arrayRoot, JSONObject.of(
                "container", JSONObject.of(combinator, options),
                "value", JSONObject.of("type", "string")));

        assertTrue(schema.isValid(value(arrayRoot, "ok")));
        assertFalse(schema.isValid(value(arrayRoot, true)));
        if (!"allOf".equals(combinator)) {
            assertTrue(schema.isValid(value(arrayRoot, 1)));
        }
    }

    @ParameterizedTest
    @CsvSource({"$defs,false", "$defs,true", "definitions,false", "definitions,true"})
    public void additionalItems(String keyword, boolean arrayRoot) {
        JSONObject container = JSONObject.of("type", "array",
                "prefixItems", JSONArray.of(JSONObject.of("type", "integer")),
                "additionalItems", ref(keyword, "value"));
        JSONSchema schema = schema(keyword, arrayRoot, JSONObject.of(
                "container", container, "value", JSONObject.of("type", "string")));

        assertTrue(schema.isValid(value(arrayRoot, JSONArray.of(1, "ok"))));
        assertFalse(schema.isValid(value(arrayRoot, JSONArray.of(1, 2))));
        assertFalse(schema.isValid(value(arrayRoot, JSONArray.of("wrong", "ok"))));
    }

    @ParameterizedTest
    @CsvSource({"$defs,false", "$defs,true", "definitions,false", "definitions,true"})
    public void secondPrefixItem(String keyword, boolean arrayRoot) {
        JSONObject container = JSONObject.of("type", "array",
                "prefixItems", JSONArray.of(JSONObject.of("type", "string"), ref(keyword, "value")));
        JSONSchema schema = schema(keyword, arrayRoot, JSONObject.of(
                "container", container, "value", JSONObject.of("type", "integer")));

        assertTrue(schema.isValid(value(arrayRoot, JSONArray.of("ok", 1))));
        assertFalse(schema.isValid(value(arrayRoot, JSONArray.of("ok", "wrong"))));
        assertFalse(schema.isValid(value(arrayRoot, JSONArray.of(1, 1))));
    }

    @ParameterizedTest
    @CsvSource({"$defs,false", "$defs,true", "definitions,false", "definitions,true"})
    public void aliasChain(String keyword, boolean arrayRoot) {
        JSONSchema schema = schema(keyword, arrayRoot, JSONObject.of(
                "container", ref(keyword, "middle"),
                "middle", ref(keyword, "value"),
                "value", JSONObject.of("type", "string")));

        assertTrue(schema.isValid(value(arrayRoot, "ok")));
        assertFalse(schema.isValid(value(arrayRoot, 1)));
    }

    @ParameterizedTest
    @CsvSource({"$defs,false", "$defs,true", "definitions,false", "definitions,true"})
    public void danglingAlias(String keyword, boolean arrayRoot) {
        assertThrows(JSONException.class, () -> schema(keyword, arrayRoot, JSONObject.of(
                "container", ref(keyword, "missing"))));
    }

    @ParameterizedTest
    @CsvSource({"$defs,false", "$defs,true", "definitions,false", "definitions,true"})
    public void circularAlias(String keyword, boolean arrayRoot) {
        assertThrows(JSONException.class, () -> schema(keyword, arrayRoot, JSONObject.of(
                "container", ref(keyword, "middle"), "middle", ref(keyword, "container"))));
    }

    @ParameterizedTest
    @CsvSource({"$defs,false", "$defs,true", "definitions,false", "definitions,true"})
    public void selfAlias(String keyword, boolean arrayRoot) {
        assertThrows(JSONException.class, () -> schema(keyword, arrayRoot, JSONObject.of(
                "container", ref(keyword, "container"))));
    }

    @ParameterizedTest
    @CsvSource({"$defs,false", "$defs,true", "definitions,false", "definitions,true"})
    public void missingDefinition(String keyword, boolean arrayRoot) {
        assertThrows(JSONException.class, () -> schema(keyword, arrayRoot, new JSONObject()));
    }

    @ParameterizedTest
    @CsvSource({"$defs,false", "$defs,true", "definitions,false", "definitions,true"})
    public void crossNamespaceAliasChain(String keyword, boolean arrayRoot) {
        String other = "$defs".equals(keyword) ? "definitions" : "$defs";
        JSONObject input = input(keyword, arrayRoot, JSONObject.of(
                "container", ref(other, "middle"), "value", JSONObject.of("type", "string")));
        input.put(other, JSONObject.of("middle", ref(keyword, "value")));
        JSONSchema schema = JSONSchema.of(input);

        assertTrue(schema.isValid(value(arrayRoot, "ok")));
        assertFalse(schema.isValid(value(arrayRoot, 1)));
    }

    @ParameterizedTest
    @CsvSource({"$defs,false,allOf", "$defs,true,allOf", "definitions,false,allOf", "definitions,true,allOf",
            "$defs,false,anyOf", "$defs,true,anyOf", "definitions,false,anyOf", "definitions,true,anyOf",
            "$defs,false,oneOf", "$defs,true,oneOf", "definitions,false,oneOf", "definitions,true,oneOf"})
    public void typedRootCombinators(String keyword, boolean arrayRoot, String combinator) {
        JSONObject constraint = arrayRoot
                ? JSONObject.of("type", "array", "minItems", 1)
                : JSONObject.of("type", "object", "required", JSONArray.of("data"));
        JSONSchema schema = JSONSchema.of(JSONObject.of("type", arrayRoot ? "array" : "object",
                keyword, JSONObject.of("constraint", constraint),
                combinator, JSONArray.of(ref(keyword, "constraint"))));

        assertTrue(schema.isValid(value(arrayRoot, 1)));
        assertFalse(schema.isValid(arrayRoot ? new JSONArray() : new JSONObject()));
    }

    @ParameterizedTest
    @CsvSource({"$defs,false", "$defs,true", "definitions,false", "definitions,true"})
    public void escapedForwardReferenceNames(String keyword, boolean arrayRoot) {
        String[][] names = {
                {"tilde~field", "tilde~0field"},
                {"slash/field", "slash~1field"},
                {"percent%field", "percent%25field"},
                {"literal~1field", "literal~01field"},
                {"space field", "space%20field"},
                {"plus+field", "plus+field"},
                {"\u540d", "%E5%90%8D"}
        };
        for (String[] name : names) {
            JSONSchema schema = schema(keyword, arrayRoot, JSONObject.of(
                    "container", ref(keyword, name[1]), name[0], JSONObject.of("type", "string")));
            assertTrue(schema.isValid(value(arrayRoot, "ok")), name[0]);
            assertFalse(schema.isValid(value(arrayRoot, 1)), name[0]);
        }
    }

    private static JSONSchema schema(String keyword, boolean arrayRoot, JSONObject definitions) {
        return JSONSchema.of(input(keyword, arrayRoot, definitions));
    }

    private static JSONObject input(String keyword, boolean arrayRoot, JSONObject definitions) {
        return arrayRoot
                ? JSONObject.of("type", "array", keyword, definitions, "items", ref(keyword, "container"))
                : JSONObject.of("type", "object", keyword, definitions,
                        "properties", JSONObject.of("data", ref(keyword, "container")));
    }

    private static Object value(boolean arrayRoot, Object item) {
        return arrayRoot ? JSONArray.of(item) : JSONObject.of("data", item);
    }

    private static JSONObject ref(String keyword, String name) {
        return JSONObject.of("$ref", "#/" + keyword + "/" + name);
    }
}
