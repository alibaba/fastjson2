package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class Issue647 {
    @Test
    public void test() {
        URL url = Issue647.class.getClassLoader().getResource("issues/issue647.json");
        JSONObject jsonObject = JSON.parseObject(url);
        assertNotNull(jsonObject);
    }

    @Test
    public void test1() {
        String str = "{\"item\":[{\"id\":101}]";

        Exception error = null;
        try {
            char[] chars = str.toCharArray();
            JSON.parseObject(chars, 0, chars.length, Bean.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("fieldName item"));

        error = null;
        try {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSON.parseObject(bytes, 0, bytes.length, Bean.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("fieldName item"));

        error = null;
        try {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII);
            jsonReader.read(Bean.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("fieldName item"));
    }

    public static class Bean {
        public Item item;
    }

    public static class Item {
        public int id;
    }
}
