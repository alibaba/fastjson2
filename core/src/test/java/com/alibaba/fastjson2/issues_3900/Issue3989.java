package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3989 {
    @Test
    public void test() {
        String inputJson;
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("issues/issue3989.json");
            inputJson = IOUtils.toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        JSONObject jsonObjectUTF16 = JSON.parseObject(inputJson.toCharArray());
        JSONObject jsonObjectUTF8 = JSON.parseObject(inputJson.getBytes(StandardCharsets.UTF_8));

        assertEquals(1, jsonObjectUTF16.getJSONObject("crosssell").getInteger("enable"));
        assertEquals(1, jsonObjectUTF8.getJSONObject("crosssell").getInteger("enable"));
    }
}
