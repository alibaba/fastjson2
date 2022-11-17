package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPathTypedMultiTest2 {
    String jsonStr = "{\n"
            + "  \"a\": \"t\",\n"
            + "  \"v\": [\n"
            + "    {\n"
            + "      \"name\": \"a.b\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"name\": \"c\"\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    @Test
    public void test() {
        String[] paths = {"$.v", "$.v[*]"};
        Class<?>[] types = {String.class, String[].class};
        JSONPath jsonPath = JSONPath.of(paths, types);
        Object[] values = (Object[]) jsonPath.extract(jsonStr);
        assertEquals("[{\"name\":\"a.b\"},{\"name\":\"c\"}]", values[0]);
        assertArrayEquals(
                new String[]{
                        "{\"name\":\"a.b\"}",
                        "{\"name\":\"c\"}"
                },
                (String[]) values[1]
        );
    }

    @Test
    public void test1() {
        String[] paths = {"$.v", "$.v[*]"};
        Class<?>[] types = {String.class, String.class};
        JSONPath jsonPath = JSONPath.of(paths, types);
        Object[] values = (Object[]) jsonPath.extract(jsonStr);
        assertEquals("[{\"name\":\"a.b\"},{\"name\":\"c\"}]", values[0]);
        assertEquals(
                "[{\"name\":\"a.b\"},{\"name\":\"c\"}]",
                values[1]
        );
    }
}
