package com.alibaba.fastjson.issue_2800;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2894 {
    @Test
    public void test_for_issue() throws Exception {
        String json = "{\"timestamp\":\"2019-09-19 08:49:52.350000000\"}";
        Pojo pojo = JSONObject.parseObject(json, Pojo.class);
        int nanos = pojo.timestamp.getNanos();
        assertEquals(nanos, 350000000);
        assertEquals("{\"timestamp\":\"2019-09-19 08:49:52.350000000\"}", JSON.toJSONString(pojo));
    }

    public static class Pojo {
        @JSONField(name = "timestamp", format = "yyyy-MM-dd HH:mm:ss.SSSSSSSSS")
        public Timestamp timestamp;
    }
}
