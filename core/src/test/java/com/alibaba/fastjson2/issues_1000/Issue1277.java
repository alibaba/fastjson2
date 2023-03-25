package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1277 {
    @Test
    public void test() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "{\"paternalSurname\": \"Rodríguez\"}");
        String json = JSON.toJSONString(params);
        System.out.println(json);
        JSONObject jsonObject = JSON.parseObject(json);
// output: {"name":"{\"paternalSurname\": \"Rodr￭guez\"}"}
// Rodríguez 变成了 Rodr￭guez
        assertEquals(params.get("name"), jsonObject.get("name"));
    }
}
