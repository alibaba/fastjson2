package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1507 {
    @Test
    public void test() {
        String json = "{\n" +
                "    \"deep1\": [{\n" +
                "        \"flightId\": \"MH8633\",\n" +
                "        \"column\": \"C\",\n" +
                "        \"row\": \"19\"\n" +
                "    }]\n" +
                "}";
        Object result = JSONPath.extract(json, "$.deep1['row','column']");
        assertEquals("[[\"19\",\"C\"]]", JSON.toJSONString(result));
    }

    @Test
    public void test1() {
        String json = "{\n" +
                "    \"deep1\": {\n" +
                "        \"flightId\": \"MH8633\",\n" +
                "        \"column\": \"C\",\n" +
                "        \"row\": \"19\"\n" +
                "    }\n" +
                "}";
        Object result = JSONPath.extract(json, "$.deep1['row','column']");
        assertEquals("[\"19\",\"C\"]", JSON.toJSONString(result));
    }
}
