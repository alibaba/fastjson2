package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue1965 {
    @Test
    public void test() {
        assertNull(
                JSONPath.eval(
                        JSONObject.of("data", JSONArray.of()), "$.data[0][0]"));

        String temp = "{\n" +
                "  \"code\": \"1003\", \n" +
                "  \"data\": [], \n" +
                "  \"message\": \"code: 1003 ,以【你好】开头的句子的长度不符合要求, 长度限制：3~500，实际长度：2\"\n" +
                "}\n";
        assertNull(
                JSONPath.eval(temp, "$.data[0][0]"));
    }
}
