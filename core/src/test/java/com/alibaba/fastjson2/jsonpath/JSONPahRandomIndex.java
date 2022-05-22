package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JSONPahRandomIndex {
    @Test
    public void testRandomIndex() {
        JSONArray array = JSONArray.of("1", "2", "3");
        JSONPath jsonPath = JSONPath.of("$[randomIndex()]");
        for (int i = 0; i < 100; ++i) {
            assertNotNull(
                    jsonPath.eval(array)
            );
        }
    }

    @Test
    public void testEvalRandomIndexField() {
        JSONPath path = JSONPath.of("$.data[randomIndex()].name");
        Object value = path.eval("{\"data\": [{\"id\": 1, \"name\": \"a\"}, {\"id\": 2, \"name\": \"b\"}]}");
        assertNotNull(value);
    }

    @Test
    public void testExtractRandomIndex() {
        for (int i = 0; i < 50; i++) {
            JSONPath path = JSONPath.of("$.data[randomIndex()]");
            JSONReader reader = JSONReader.of("{\"data\": [{\"id\": 1, \"name\": \"a\"}, {\"id\": 2, \"name\": \"b\"}]}");
            Object value = path.extract(reader);
            System.out.println(value);
            assertNotNull(value);
        }
    }

    @Test
    public void testExtractRandomIndexField() {
        for (int i = 0; i < 50; i++) {
            JSONPath path = JSONPath.of("$.data[randomIndex()].name");
            JSONReader reader = JSONReader.of(
                    "{\"data\": [{\"id\": 1, \"name\": \"a\"}, {\"id\": 2, \"name\": \"b\"}]}");
            Object value = path.extract(reader);
            System.out.println(value);
            assertNotNull(value);
        }
    }
}
