package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue502 {
    @Test
    public void test() {
        String result = "{\"taskId\":\"xxx\"}";
        ThirdCommonResult thirdCommonResult = JSONObject.parseObject(result, ThirdCommonResult.class);
        assertEquals("xxx", thirdCommonResult.getTaskId());
    }

    @Data
    public static class ThirdCommonResult {
        private String msg;

        private String flag;

        private String taskId;

        private JSONArray data;
    }
}
