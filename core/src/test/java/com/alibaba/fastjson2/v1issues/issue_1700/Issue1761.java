package com.alibaba.fastjson2.v1issues.issue_1700;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1761 {
    @Test
    public void test_for_issue() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("null", "");
        double d = jsonObject.getDoubleValue("null");
        assertEquals(d, 0.0D);
    }
}
