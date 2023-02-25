package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONSchemaTest1 {
    @Test
    public void test0() {
        URL url = JSONSchemaTest.class.getClassLoader().getResource("schema/schema_02.json");
        JSONObject object = JSON.parseObject(url, JSONObject.class);
        ArraySchema schema = (ArraySchema) JSONSchema.of(object);
        assertFalse(schema.isValid("aa"));
        assertFalse(schema.isValid(-1));
        assertFalse(schema.isValid(-1L));

        assertTrue(schema.isValid(JSONArray.of()));
        assertTrue(schema.isValid(JSONArray.of(1)));

        assertFalse(schema.isValid(JSONArray.of("a")));
        assertFalse(schema.isValid(JSONArray.of(-1)));
        assertFalse(schema.isValid(JSONArray.of(-1L)));
    }
}
