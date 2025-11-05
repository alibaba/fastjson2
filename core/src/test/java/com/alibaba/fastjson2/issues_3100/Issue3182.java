package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3182 {
    @Test
    public void test() {
        try {
            JSON.config(JSONReader.Feature.SupportSmartMatch, true);

            String text = "{\"appid\":\"com.xxx.xxx\"}";
            Map<String, Object> jsonObject = JSON.parseObject(text);

            JSONObject obj = new JSONObject();
            obj.put("v1", jsonObject);

            Bean bean3 = obj.getObject("v1", Bean.class);
//            Bean bean3 = obj.getObject("v1", Bean.class, JSONReader.Feature.SupportSmartMatch);
            assertEquals("com.xxx.xxx", bean3.appID);
        } finally {
            JSON.config(JSONReader.Feature.SupportSmartMatch, false);
        }
    }

    @Data
    private static class Bean {
        private String appID;
    }
}
