package com.alibaba.fastjson.issue_2100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2130 {
    @Test
    public void test_for_issue() throws Exception {
        String str = "{\"score\":0.000099369485}";
        JSONObject object = JSON.parseObject(str);
        assertEquals("{\"score\":0.000099369485}", object.toJSONString());
    }
}
