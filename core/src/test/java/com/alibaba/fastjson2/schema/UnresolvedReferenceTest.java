package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UnresolvedReferenceTest {
    @Test
    public void unresolvedPlaceholderCannotValidate() {
        UnresolvedReference reference = new UnresolvedReference("missing", new LinkedHashMap<>());
        assertThrows(JSONException.class, () -> reference.isValid("value"));
    }

    @Test
    public void missingResolveTargetFails() {
        Map<String, JSONSchema> schemas = new LinkedHashMap<>();
        UnresolvedReference reference = new UnresolvedReference("missing", schemas);
        UnresolvedReference.PropertyResolveTask task =
                new UnresolvedReference.PropertyResolveTask(schemas, "value", reference);
        assertThrows(JSONException.class, task::resolve);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void completedConstructionReleasesTasks(boolean arrayRoot) {
        JSONObject defs = JSONObject.of(
                "alias", JSONObject.of("$ref", "#/$defs/value"),
                "value", JSONObject.of("type", "string"));
        JSONSchema schema = JSONSchema.of(JSONObject.of("type", arrayRoot ? "array" : "object", "$defs", defs));
        assertNull(schema.resolveTasks);
    }
}
