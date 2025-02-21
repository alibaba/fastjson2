package com.alibaba.fastjson2.issues_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3347 {
    @Test
    public void test() {
        String json2 = "{\"*/*\":{\"schema\":{\"$ref\":\"Error-ModelName{namespace='javax.servlet.http', name='HttpServletResponse'}\"}}}";
        String expected = "Error-ModelName{namespace='javax.servlet.http', name='HttpServletResponse'}";
        {
            JSONObject jsonObject4 = JSON.parseObject(json2);
            assertEquals(expected, jsonObject4.getJSONObject("*/*").getJSONObject("schema").getString("$ref"));
        }
        {
            JSONObject jsonObject4 = JSON.parseObject(json2.getBytes(StandardCharsets.UTF_8));
            assertEquals(expected, jsonObject4.getJSONObject("*/*").getJSONObject("schema").getString("$ref"));
        }
        {
            JSONObject jsonObject4 = JSON.parseObject(json2.toCharArray());
            assertEquals(expected, jsonObject4.getJSONObject("*/*").getJSONObject("schema").getString("$ref"));
        }
        {
            JSONObject jsonObject4 = JSON.parseObject(json2, JSONReader.Feature.DisableReferenceDetect);
            assertEquals(expected, jsonObject4.getJSONObject("*/*").getJSONObject("schema").getString("$ref"));
        }
    }
}
