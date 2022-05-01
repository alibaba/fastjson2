package com.alibaba.fastjson2.v1issues.issue_3600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue3671 {
    @Test
    public void test_for_issue() throws Exception {
        String json = "[{\n" +
                "    \"filters\": [],\n" +
                "    \"id\": \"baidu_route2\",\n" +
                "    \"order\": 0,\n" +
                "    \"predicates\": [{\n" +
                "        \"args\": {\n" +
                "            \"pattern\": \"/baidu/**\"\n" +
                "        },\n" +
                "        \"name\": \"Path\"\n" +
                "    }],\n" +
                "    \"uri\": \"https://www.baidu.com\"\n" +
                "}]\n";
        byte[] utf8 = json.getBytes(StandardCharsets.UTF_8);

        assertTrue(JSON.isValid(json));
        assertTrue(JSON.isValid(utf8));
        assertTrue(JSON.isValid(utf8, 0, utf8.length, StandardCharsets.UTF_8));
        assertTrue(JSON.isValid(utf8, 0, utf8.length, StandardCharsets.US_ASCII));
    }
}
