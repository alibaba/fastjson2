package com.alibaba.fastjson2.v1issues.issue_3900;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3922 {
    @Test
    public void test_for_issue3922() {
        String jsonString = "{\"this0\":{\"$ref\":\"1\"}}";
        JSONObject jsonObject = JSONObject.parseObject(jsonString, JSONObject.class);
        JSONObject innerObject = (JSONObject) jsonObject.get("this0");
        assertEquals("{\"$ref\":\"1\"}", innerObject.toString());
    }
}
