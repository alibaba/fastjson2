package com.alibaba.fastjson.issue_4200;

import com.alibaba.fastjson.JSONArray;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class Issue4247 {
    @Test
    public void test() {
        JSONArray array = new JSONArray();
        assertEquals("[]", array.toJSONString());
    }
}
