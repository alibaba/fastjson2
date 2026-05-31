package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("jsonpath")
public class JSONPathTest6 {
    JSONObject object = JSONObject.of("id", 1001, "name", "DataWorks");
    String json = object.toString();
    byte[] jsonBytes = json.getBytes();
    byte[] jsonbBytes = object.toJSONBBytes();

    @Test
    public void testInteger() {
        JSONPath jsonPath = JSONPath.of("id", Integer.class);

        Integer id = 1001;
        assertEquals(id, jsonPath.eval(object));
        assertEquals(id, jsonPath.extract(JSONReader.of(json)));
        assertEquals(id, jsonPath.extract(JSONReader.ofJSONB(jsonbBytes)));
        assertEquals(id, jsonPath.extract(json));
        assertEquals(id, jsonPath.extract(jsonBytes));
    }

    @Test
    public void testLong() {
        JSONPath jsonPath = JSONPath.of("id", Long.class);

        Long id = 1001L;
        assertEquals(id, jsonPath.eval(object));
        assertEquals(id, jsonPath.extract(JSONReader.of(json)));
        assertEquals(id, jsonPath.extract(JSONReader.ofJSONB(jsonbBytes)));
        assertEquals(id, jsonPath.extract(json));
        assertEquals(id, jsonPath.extract(jsonBytes));
    }

    @Test
    public void testBigInteger() {
        JSONPath jsonPath = JSONPath.of("id", BigInteger.class);

        BigInteger id = object.getBigInteger("id");
        assertEquals(id, jsonPath.eval(object));
        assertEquals(id, jsonPath.extract(JSONReader.of(json)));
        assertEquals(id, jsonPath.extract(JSONReader.ofJSONB(jsonbBytes)));
        assertEquals(id, jsonPath.extract(json));
        assertEquals(id, jsonPath.extract(jsonBytes));
    }

    @Test
    public void testBigDecimal() {
        JSONPath jsonPath = JSONPath.of("id", BigDecimal.class);

        BigDecimal id = object.getBigDecimal("id");
        assertEquals(id, jsonPath.eval(object));
        assertEquals(id, jsonPath.extract(JSONReader.of(json)));
        assertEquals(id, jsonPath.extract(JSONReader.ofJSONB(jsonbBytes)));
        assertEquals(id, jsonPath.extract(json));
        assertEquals(id, jsonPath.extract(jsonBytes));
    }

    @Test
    public void testString() {
        JSONPath jsonPath = JSONPath.of("id", String.class);

        String id = "1001";
        assertEquals(id, jsonPath.eval(object));
        assertEquals(id, jsonPath.extract(JSONReader.of(json)));
        assertEquals(id, jsonPath.extract(JSONReader.ofJSONB(jsonbBytes)));
        assertEquals(id, jsonPath.extract(json));
        assertEquals(id, jsonPath.extract(jsonBytes));
        assertEquals(id, jsonPath.extract(jsonBytes, 0, jsonBytes.length, StandardCharsets.US_ASCII));
    }
}
