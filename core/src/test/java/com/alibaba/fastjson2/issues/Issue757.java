package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue757 {
    @Test
    public void test() {
        String str = "{\"visit_time\":1662950912,\"visit_user\":{\"userid\":\"51753998\",\"username\":\"\",\"usernick\":\"\",\"usersex\":\"M\"},\"visit_oauth\":{\"access_token\":\"8d82afa4189cdd1a623b1a45412ca3f93e1a4373\",\"token_expires\":\"1662999033\"}}\0\0";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        char[] chars = str.toCharArray();

        assertEquals(1662950912, JSON.parseObject(str).get("visit_time"));
        assertEquals(1662950912, JSON.parseObject(chars).get("visit_time"));
        assertEquals(1662950912, JSON.parseObject(bytes).get("visit_time"));
        assertEquals(1662950912, JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.UTF_8).get("visit_time"));
        assertEquals(1662950912, JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.US_ASCII).get("visit_time"));
        assertEquals(1662950912, JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1).get("visit_time"));

        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str)) {
            jsonReader.readObject();
            assertTrue(jsonReader.isEnd(), jsonReader.getClass().getName());
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str)) {
            JSONObject object = new JSONObject();
            jsonReader.readObject(object);
            assertTrue(jsonReader.isEnd(), jsonReader.getClass().getName());
        }
    }
}
