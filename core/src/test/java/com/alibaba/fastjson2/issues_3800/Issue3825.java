package com.alibaba.fastjson2.issues_3800;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3825 {
    @Test
    public void jsonPathRead() {
        String path = "$.data[?(@.key)]";
        String json = "{\n" +
                "                    \"data\": [\n" +
                "                      {\n" +
                "                        \"key\": \"k1\",\n" +
                "                        \"value\": \"v1\"\n" +
                "                      },\n" +
                "                      {\n" +
                "                        \"key\": \"k2\",\n" +
                "                        \"value\": \"v2\"\n" +
                "                      },\n" +
                "                      {\n" +
                "                        \"key\": null,\n" +
                "                        \"value\": \"v3\"\n" +
                "                      }\n" +
                "                    ]\n" +
                "                  }";
        Object eval = com.alibaba.fastjson.JSONPath.eval(json, path);
        Object eval2 = com.alibaba.fastjson2.JSONPath.eval(json, path);
        assertEquals(eval, eval2);
    }
}
