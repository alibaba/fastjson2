package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.JSONReader.Feature;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AllowArbitraryCommasTest {
    @Test
    public void test_UTF16() {
        String json = "[,,1, 2,, 3,, ,, 4,,]";
        JSONArray array = JSON.parseArray(json, Feature.AllowArbitraryCommas);
        assertEquals(4, array.size());
        assertEquals(1, array.get(0));
        assertEquals(2, array.get(1));
        assertEquals(3, array.get(2));
        assertEquals(4, array.get(3));

        String json2 = "{, , \"id\": 123,, \"name\": \"fastjson\",, \"valid\": true,,}";
        JSONObject map = JSON.parseObject(json2, Feature.AllowArbitraryCommas);
        assertEquals(3, map.size());
        assertEquals(123, map.get("id"));
        assertEquals("fastjson", map.get("name"));
        assertEquals(true, map.get("valid"));

        String json3 = "[,, ,,]";
        JSONArray array3 = JSON.parseArray(json3, Feature.AllowArbitraryCommas);
        assertTrue(array3.isEmpty());

        String json4 = "{,, ,,}";
        JSONObject map4 = JSON.parseObject(json4, Feature.AllowArbitraryCommas);
        assertTrue(map4.isEmpty());

        String json5 = "[, // 注释 \n ,1]";
        try (JSONReader reader = JSONReader.of(json5)) {
            assertThrows(JSONException.class, reader::readArray);
        }

        String json6 = "{, // comment \n , \"a\":1}";
        try (JSONReader reader = JSONReader.of(json6)) {
            assertThrows(JSONException.class, reader::readObject);
        }

        String json7 = "{" +
                ", , \"list\": [,, 1,, 2,, ]," +
                ",, \"obj\": {,, \"x\": 10,, }," +
                ",,}";
        JSONObject jsonObject = JSON.parseObject(json7, Feature.AllowArbitraryCommas);
        List list = (List) jsonObject.get("list");
        assertEquals(2, list.size());
        assertEquals(1, list.get(0));
        Map obj = (Map) jsonObject.get("obj");
        assertEquals(10, obj.get("x"));

        String json8 = "{, , \"id\": 123, , \"name\": \"fastjson\",, \"valid\": true, ,}";
        User user = JSON.parseObject(json8, User.class, Feature.AllowArbitraryCommas);
        assertEquals(123, user.getId());
        assertEquals("fastjson", user.getName());
        assertEquals(true, user.isValid());
    }

    @Test
    public void test_UTF8() {
        JSONReader.Context context = new JSONReader.Context(Feature.AllowArbitraryCommas);

        String json = "[, , 1, 2,, 3,,]";
        try (JSONReader reader = JSONReader.of(json.getBytes(StandardCharsets.UTF_8), context)) {
            List<Integer> list = reader.readArray(Integer.class);
            assertEquals(3, list.size());
            assertEquals(1, list.get(0));
            assertEquals(2, list.get(1));
            assertEquals(3, list.get(2));
        }

        String json2 = "{,, \"id\": 123,, \"name\": \"fastjson\", , }";
        try (JSONReader reader = JSONReader.of(json2.getBytes(StandardCharsets.UTF_8), context)) {
            Map<String, Object> map = reader.readObject();
            assertEquals(123, map.get("id"));
            assertEquals("fastjson", map.get("name"));
        }

        String json3 = "{,, \"id\": 1001, , \"name\": \"UserUTF8\",, \"valid\": true,,}";
        try (JSONReader reader = JSONReader.of(json3.getBytes(StandardCharsets.UTF_8), context)) {
            User user = reader.read(User.class);
            assertEquals(1001, user.getId());
            assertEquals("UserUTF8", user.getName());
            assertTrue(user.isValid());
        }
    }

    @Test
    public void test_global() {
        try {
            JSON.config(Feature.AllowArbitraryCommas, true);

            String json = "[,,1, 2,, 3,,,, 4,,]";
            JSONArray array = JSON.parseArray(json);
            assertEquals(4, array.size());
            assertEquals(1, array.get(0));
            assertEquals(2, array.get(1));
            assertEquals(3, array.get(2));
            assertEquals(4, array.get(3));

            String json2 = "{,, \"id\": 123,, \"name\": \"fastjson\",, \"valid\": true,,}";
            JSONObject map = JSON.parseObject(json2);
            assertEquals(3, map.size());
            assertEquals(123, map.get("id"));
            assertEquals("fastjson", map.get("name"));
            assertEquals(true, map.get("valid"));
        } finally {
            JSON.config(Feature.AllowArbitraryCommas, false);
        }
    }

    @Data
    public static class User {
        private int id;
        private String name;
        private boolean valid;
    }
}
