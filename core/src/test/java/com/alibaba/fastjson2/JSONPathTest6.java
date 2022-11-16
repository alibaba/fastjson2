package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPathTest6 {
    @Test
    public void testInteger() {
        JSONPath jsonPath = JSONPath.of("id", Long.class);

        JSONObject object = JSONObject.of("id", 1001, "name", "DataWorks");
        assertEquals(1001L, jsonPath.eval(object));

        String json = object.toString();
        byte[] jsonBytes = json.getBytes();

        assertEquals(1001, (Long) jsonPath.extract(JSONReader.of(json)));
        assertEquals(1001, (Long) jsonPath.extract(json));
        assertEquals(1001, (Long) jsonPath.extract(jsonBytes));
    }

    @Test
    public void testLong() {
        JSONPath jsonPath = JSONPath.of("id", Long.class);

        JSONObject object = JSONObject.of("id", 1001, "name", "DataWorks");
        assertEquals(1001L, jsonPath.eval(object));

        String json = object.toString();
        byte[] jsonBytes = json.getBytes();

        assertEquals(1001, (Long) jsonPath.extract(JSONReader.of(json)));
        assertEquals(1001, (Long) jsonPath.extract(json));
        assertEquals(1001, (Long) jsonPath.extract(jsonBytes));
    }

    @Test
    public void testBigInteger() {
        JSONPath jsonPath = JSONPath.of("id", BigInteger.class);

        JSONObject object = JSONObject.of("id", 1001, "name", "DataWorks");
        BigInteger id = object.getBigInteger("id");

        assertEquals(id, jsonPath.eval(object));

        String json = object.toString();
        byte[] jsonBytes = json.getBytes();

        assertEquals(id, jsonPath.extract(JSONReader.of(json)));
        assertEquals(id, jsonPath.extract(json));
        assertEquals(id, jsonPath.extract(jsonBytes));
    }
}
