package com.alibaba.fastjson.issue_3000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Calendar;

public class Issue3093 {
    @Test
    public void test_for_issue() throws Exception {
        Timestamp ts = new Timestamp(Calendar.getInstance().getTimeInMillis());
        System.out.println(ts.toString());
        String json = JSON.toJSONString(ts, SerializerFeature.UseISO8601DateFormat);
        System.out.println(json);
    }
}
