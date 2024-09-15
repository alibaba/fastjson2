package com.alibaba.fastjson2.issues_2900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.schema.JSONSchema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2931 {
    @Test
    public void test() {
        JSONObject object = JSON.parseObject("{\"type\":\"object\",\"properties\":{\"longitude\":{\"type\":[\"string\", \"number\"]},\"latitude\":{\"type\":\"number\",\"minimum\":-90,\"maximum\":90}},\"required\":[\"longitude\",\"latitude\"]}");
        System.out.println(object);
        JSONSchema schema = JSONSchema.of(object);
        assertTrue(schema.isValid(
                JSONObject.of("longitude", "123.45", "latitude", 21.31)
        ));
        assertFalse(schema.isValid(
                JSONObject.of("longitude", false, "latitude", 21.31)
        ));
    }
}
