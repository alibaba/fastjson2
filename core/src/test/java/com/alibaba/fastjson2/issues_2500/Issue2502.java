package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2502 {
    @Test
    public void test() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("endData", 1743004800000L);
        jsonObject.put("machineId", "6b69b21c178a8254dedfe031");
        jsonObject.put("maxDeviceNum", 1000);
        jsonObject.put("maxUserNum", 1000);
        jsonObject.put("message", "LIcense授权成功！");
        jsonObject.put("sModes", "11111111");
        jsonObject.put("startDate", 1713456000000L);
        jsonObject.put("sucess", true);

        String expected = "{\"endData\":1743004800000,\"machineId\":\"6b69b21c178a8254dedfe031\",\"maxDeviceNum\":1000,\"maxUserNum\":1000,\"message\":\"LIcense授权成功！\",\"sModes\":\"11111111\",\"startDate\":1713456000000,\"sucess\":true}";
        assertEquals(expected, jsonObject.toString());
        assertEquals(expected, JSON.toJSONString(jsonObject));
    }
}
