package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONReaderUTF16VectorTest {
    @Test
    public void testJSON() {
        for (char i = 0; i < 512; i++) {
            String s1 = Character.toString(i);
            String json = JSON.toJSONString(JSONObject.of(s1, s1));
            char[] chars = json.toCharArray();
            JSONReader.Context ctx = JSONFactory.createReadContext();
            JSONReaderUTF16Vector jsonReader = new JSONReaderUTF16Vector(ctx, json, chars, 0, chars.length);
            JSONObject object = (JSONObject) jsonReader.readObject();
            Object v1 = object.get(s1);
            assertEquals(s1, v1, Integer.toString(i));
        }

        for (char i = 0; i < 512; i++) {
            for (char j = 0; j < 512; j++) {
                String s2 = new String(new char[]{i, j});
                String json = JSON.toJSONString(JSONObject.of(s2, s2));
                char[] chars = json.toCharArray();
                JSONReader.Context ctx = JSONFactory.createReadContext();
                JSONReaderUTF16Vector jsonReader = new JSONReaderUTF16Vector(ctx, json, chars, 0, chars.length);
                JSONObject object = (JSONObject) jsonReader.readObject();
                Object v1 = object.get(s2);
                assertEquals(s2, v1);
            }
        }
    }
}
