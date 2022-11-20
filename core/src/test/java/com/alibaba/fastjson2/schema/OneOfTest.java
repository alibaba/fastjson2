package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OneOfTest {
    @Test
    public void test() {
        assertThrows(
                JSONException.class,
                () -> JSONSchema.of(
                        JSONObject.of(
                                "oneOf",
                                JSONArray.of()
                        )
                )
        );
    }

    @Test
    public void test1() {
        OneOf schema = (OneOf) JSONSchema.of(
                JSONObject.of(
                        "oneOf",
                        JSONArray.of(
                                JSONObject.of("type", "string"),
                                JSONObject.of("type", "integer")
                        )
                )
        );
        assertEquals(JSONSchema.Type.OneOf, schema.getType());
    }
}
