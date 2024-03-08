package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONReaderUTF8VectorTest {
    @Test
    public void test() {
        JSONReader.Context ctx = JSONFactory.createReadContext();
        String str = "abcdef1234567890中国©®£\uD83D\uDE0D\uD83D\uDC81\uD83D\uDC4C\uD83C\uDF8D\uD83D\uDE0D";
        String json = JSON.toJSONString(str);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        JSONReaderUTF8Vector jsonReader = new JSONReaderUTF8Vector(ctx, json, bytes, 0, bytes.length);
        String parsed = jsonReader.readString();
        assertEquals(str, parsed);
    }

    @Test
    public void testJSON() {
        for (char i = 0; i < 512; i++) {
            String s1 = Character.toString(i);
            String json = JSON.toJSONString(JSONObject.of(s1, s1));
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            JSONReader.Context ctx = JSONFactory.createReadContext();
            JSONReaderUTF8Vector jsonReader = new JSONReaderUTF8Vector(ctx, s1, bytes, 0, bytes.length);
            JSONObject object = (JSONObject) jsonReader.readObject();
            Object v1 = object.get(s1);
            assertEquals(s1, v1);
        }

        for (char i = 0; i < 512; i++) {
            for (char j = 0; j < 512; j++) {
                String s2 = new String(new char[]{i, j});
                String json = JSON.toJSONString(JSONObject.of(s2, s2));
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                JSONReader.Context ctx = JSONFactory.createReadContext();
                JSONReaderUTF8Vector jsonReader = new JSONReaderUTF8Vector(ctx, s2, bytes, 0, bytes.length);
                JSONObject object = (JSONObject) jsonReader.readObject();
                Object v1 = object.get(s2);
                assertEquals(s2, v1);
            }
        }
    }
}
