package com.alibaba.fastjson.issue_3600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONValidator;
import org.junit.jupiter.api.Test;

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
        assertTrue(JSONValidator.from(json).validate());
        assertTrue(JSON.isValid(json));
    }
}
