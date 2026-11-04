package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONSchemaValidException;
import com.alibaba.fastjson2.schema.JSONSchema;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
public class Issue1854 {
    @Test
    public void test() {
        JSONSchema integerMaximum = JSONSchema.of(JSONObject.of("type", "integer", "maximum", 10));
        assertInvalid("maximum not match, expect <= 10, but 11", () -> integerMaximum.assertValidate((Object) 11));
        assertInvalid("maximum not match, expect <= 10, but 11", () -> integerMaximum.assertValidate(11L));
        assertInvalid("maximum not match, expect <= 10, but 11", () -> integerMaximum.assertValidate(Long.valueOf(11L)));
        assertInvalid("maximum not match, expect <= 10, but 11", () -> integerMaximum.assertValidate(Integer.valueOf(11)));

        JSONSchema numberMaximum = JSONSchema.of(JSONObject.of("type", "number", "maximum", 10));
        assertInvalid("maximum not match, expect <= 10, but 11", () -> numberMaximum.assertValidate((Object) 11));
        assertInvalid("maximum not match, expect <= 10, but 11", () -> numberMaximum.assertValidate(11L));
        assertInvalid("maximum not match, expect <= 10, but 11.0", () -> numberMaximum.assertValidate(11d));

        JSONSchema integerExclusiveMaximum = JSONSchema.of(JSONObject.of("type", "integer", "exclusiveMaximum", true, "maximum", 10));
        assertInvalid("exclusiveMaximum not match, expect < 10, but 11", () -> integerExclusiveMaximum.assertValidate((Object) 11));
        assertInvalid("exclusiveMaximum not match, expect < 10, but 11", () -> integerExclusiveMaximum.assertValidate(11L));
        assertInvalid("exclusiveMaximum not match, expect < 10, but 11", () -> integerExclusiveMaximum.assertValidate(Long.valueOf(11L)));
        assertInvalid("exclusiveMaximum not match, expect < 10, but 11", () -> integerExclusiveMaximum.assertValidate(Integer.valueOf(11)));

        JSONSchema numberExclusiveMaximum = JSONSchema.of(JSONObject.of("type", "number", "exclusiveMaximum", true, "maximum", 10));
        assertInvalid("exclusiveMaximum not match, expect < 10, but 11", () -> numberExclusiveMaximum.assertValidate((Object) 11));
        assertInvalid("exclusiveMaximum not match, expect < 10, but 11", () -> numberExclusiveMaximum.assertValidate(11L));
        assertInvalid("exclusiveMaximum not match, expect < 10, but 11.0", () -> numberExclusiveMaximum.assertValidate(11d));

        JSONSchema integerMinimum = JSONSchema.of(JSONObject.of("type", "integer", "minimum", 10));
        assertInvalid("minimum not match, expect >= 10, but 9", () -> integerMinimum.assertValidate((Object) 9));
        assertInvalid("minimum not match, expect >= 10, but 9", () -> integerMinimum.assertValidate(9L));
        assertInvalid("minimum not match, expect >= 10, but 9", () -> integerMinimum.assertValidate(Long.valueOf(9L)));
        assertInvalid("minimum not match, expect >= 10, but 9", () -> integerMinimum.assertValidate(Integer.valueOf(9)));

        JSONSchema numberMinimum = JSONSchema.of(JSONObject.of("type", "number", "minimum", 10));
        assertInvalid("minimum not match, expect >= 10, but 9", () -> numberMinimum.assertValidate((Object) 9));
        assertInvalid("minimum not match, expect >= 10, but 9", () -> numberMinimum.assertValidate(9L));
        assertInvalid("minimum not match, expect >= 10, but 9.0", () -> numberMinimum.assertValidate(9d));

        JSONSchema integerExclusiveMinimum = JSONSchema.of(JSONObject.of("type", "integer", "exclusiveMinimum", true, "minimum", 10));
        assertInvalid("exclusiveMinimum not match, expect > 10, but 9", () -> integerExclusiveMinimum.assertValidate((Object) 9));
        assertInvalid("exclusiveMinimum not match, expect > 10, but 9", () -> integerExclusiveMinimum.assertValidate(9L));
        assertInvalid("exclusiveMinimum not match, expect > 10, but 9", () -> integerExclusiveMinimum.assertValidate(Long.valueOf(9L)));
        assertInvalid("exclusiveMinimum not match, expect > 10, but 9", () -> integerExclusiveMinimum.assertValidate(Integer.valueOf(9)));

        JSONSchema numberExclusiveMinimum = JSONSchema.of(JSONObject.of("type", "number", "exclusiveMinimum", true, "minimum", 10));
        assertInvalid("exclusiveMinimum not match, expect > 10, but 9", () -> numberExclusiveMinimum.assertValidate((Object) 9));
        assertInvalid("exclusiveMinimum not match, expect > 10, but 9", () -> numberExclusiveMinimum.assertValidate(9L));
        assertInvalid("exclusiveMinimum not match, expect > 10, but 9.0", () -> numberExclusiveMinimum.assertValidate(9d));
    }

    private static void assertInvalid(String expectedMessage, Executable validation) {
        JSONSchemaValidException e = assertThrows(JSONSchemaValidException.class, validation);
        assertEquals(expectedMessage, e.getMessage());
    }
}
