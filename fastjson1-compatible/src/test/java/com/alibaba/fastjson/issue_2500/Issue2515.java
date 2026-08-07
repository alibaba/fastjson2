package com.alibaba.fastjson.issue_2500;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2515 {
    @Test
    public void test_for_issue() throws Exception {
        String json = "{\n" +
                "    \"a\":\"{\\\"b\\\":\\\"cd\\\"}\"\n" +
                "}";

        JSONObject obj = JSON.parseObject(json);

        assertEquals("cd", JSONPath.eval(obj, "$.a.b"));
        assertEquals(10, JSONPath
                .eval(obj, "$.a.length()"));
    }
}
