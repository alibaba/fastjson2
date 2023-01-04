package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue502 {
    @Test
    public void test() {
        String s = "{  \"msg\":\"success\",\"flag\":\"ok\",\"taskId\":\"13fee3000dddc\"," +
                "\"data\":[{\"rq\":\"2022-06-28 12:03:52\",\"lng\":\"172.342\"," +
                "\"lat\":\"31.4532\",\"name\":\"ceshi\"}]}";
        ThirdCommonResult thirdCommonResult = JSONObject.parseObject(s, ThirdCommonResult.class);
        assertEquals("13fee3000dddc", thirdCommonResult.getTaskId());
    }

    @Data
    public static class ThirdCommonResult {
        private String msg;

        private String flag;

        private String taskId;

        private JSONArray data;
    }
}
