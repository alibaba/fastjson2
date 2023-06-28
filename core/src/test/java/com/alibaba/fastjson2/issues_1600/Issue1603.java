package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class Issue1603 {
    @Test
    public void test() {
        String text = "{\"deviceId\":\"16584\",\"srcAddress\":\"109.55.156.215\",\"ssssss\":null}";
        JSONObject jsonObject = JSON.parseObject(text);
        byte[] jsonBytes = JSON.toJSONBytes(jsonObject);
        String s = new String(jsonBytes);
        assertEquals("{\"deviceId\":\"16584\",\"srcAddress\":\"109.55.156.215\"}",s);
    }
}
