package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2064 {
    @Test
    public void test() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("time", "2023-12-01 00:00:00");
        Date date = jsonObject.getDate("time");
        assertEquals("\"2023-12-01 00:00:00\"", JSON.toJSONString(date));
    }
}
