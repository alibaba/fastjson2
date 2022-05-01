package com.alibaba.fastjson2.v1issues.issue_1600;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.sql.Time;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1644 {
    @Test
    public void test_for_issue() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("time", 1324138987429L);

        Time time = jsonObject.getObject("time", Time.class);
        assertEquals(1324138987429L, time.getTime());
    }
}
