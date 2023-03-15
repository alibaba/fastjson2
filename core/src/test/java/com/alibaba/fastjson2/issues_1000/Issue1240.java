package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1240 {
    @Test
    public void test() {
        String str = "{\"date\":\"2023-03-10 11:55:42\",\"errorCode\":\"202004\",\"errorMessage\":\"人员不存在或获取失败\",\"obj\":{\"deptCode\":\"755H\",\"scheduleCode\":\"03D\",\"scheduleEndTime\":1330,\"scheduleName\":\"755H03D10311330\",\"scheduleStartTime\":1031,\"scheduleTime\":\"10:31-13:30\",\"scheduleType\":2,\"status\":0,\"unitCode\":\"755H013\"},\"success\":false}";

        Result<List> result = (Result<List>) JSON.parseObject(str, new TypeReference<Result<List>>() {});
        assertEquals(1, result.obj.size());
        JSONObject object = (JSONObject) result.obj.get(0);
        assertNotNull(object);
        assertEquals("755H", object.get("deptCode"));
    }

    public static class Result<T> {
        public Date date;
        public String errorCode;
        public String errorMessage;
        public T obj;
        public boolean success;
    }
}
