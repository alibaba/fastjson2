package com.alibaba.json.bvt.issue_1000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 22/03/2017.
 */
public class Issue1095 {
    @Test
    public void test_for_issue() throws Exception {
        String text = "{\"Grade\": 1, \"UpdateTime\": \"2017-03-22T11:41:17\"}";
        JSONObject jsonObject = JSON.parseObject(text, Feature.AllowISO8601DateFormat);
        assertEquals(Date.class, jsonObject.get("UpdateTime").getClass());
    }
}
