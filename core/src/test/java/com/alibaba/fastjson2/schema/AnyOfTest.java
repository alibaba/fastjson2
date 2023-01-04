package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AnyOfTest {
    @Test
    public void test() {
        assertThrows(
                JSONException.class,
                () -> JSONSchema.of(
                        JSONObject.of(
                                "anyOf",
                                JSONArray.of()
                        )
                )
        );
    }

    @Test
    public void test1() {
        AnyOf schema = (AnyOf) JSONSchema.of(
                JSONObject.of(
                        "anyOf",
                        JSONArray.of(
                                JSONObject.of("type", "string"),
                                JSONObject.of("type", "integer")
                        )
                )
        );
        assertEquals(JSONSchema.Type.AnyOf, schema.getType());
    }
}
