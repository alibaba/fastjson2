package com.alibaba.fastjson2.v1issues.issue_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Issue1588 {
    @Test
    public void test_for_issue() throws Exception {
        String dateString = "2017-11-17 00:00:00";
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test", date);
        System.out.println(JSON.toJSONString(jsonObject, "iso8601"));
        System.out.println(JSON.toJSONString(jsonObject, "yyyy-MM-dd'T'HH:mm:ssXXX"));
    }
}
