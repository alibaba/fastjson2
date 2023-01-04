package com.alibaba.fastjson2.v1issues.issue_4200;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue4221 {
    @Test
    public void test() {
        JSONObject rootObject = new JSONObject();

        String sizePath = "size";
        String lengthPath = "length";

        assertFalse(JSONPath.contains(rootObject, sizePath));
        assertFalse(JSONPath.contains(rootObject, lengthPath));
        assertFalse(JSONPath.contains(rootObject, "item"));
    }
}
