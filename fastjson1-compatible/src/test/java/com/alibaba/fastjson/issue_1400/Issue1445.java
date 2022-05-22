package com.alibaba.fastjson.issue_1400;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

public class Issue1445 {
    @Test
    public void test_for_issue() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("data", new JSONObject());
        obj.getJSONObject("data").put("data", new JSONObject());
        obj.getJSONObject("data").getJSONObject("data").put("map", new JSONObject());
        obj.getJSONObject("data").getJSONObject("data").getJSONObject("map").put("21160001", "abc");

        String json = JSON.toJSONString(obj);
//        assertEquals("abc", JSONPath.read(json,"data.data.map.21160001"));
    }
}
