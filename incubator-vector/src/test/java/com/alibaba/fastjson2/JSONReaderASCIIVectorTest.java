package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONReaderASCIIVectorTest {
    @Test
    public void testJSON() {
        for (char i = 0; i < 256; i++) {
            String s1 = Character.toString(i);
            String json = JSON.toJSONString(JSONObject.of(s1, s1));
            byte[] bytes = json.getBytes(StandardCharsets.ISO_8859_1);
            JSONReader.Context ctx = JSONFactory.createReadContext();
            JSONReaderASCIIVector jsonReader = new JSONReaderASCIIVector(ctx, json, bytes, 0, bytes.length);
            JSONObject object = (JSONObject) jsonReader.readObject();
            Object v1 = object.get(s1);
            assertEquals(s1, v1, Integer.toString(i));
        }

        for (char i = 0; i < 256; i++) {
            for (char j = 0; j < 256; j++) {
                String s2 = new String(new char[]{i, j});
                String json = JSON.toJSONString(JSONObject.of(s2, s2));
                byte[] bytes = json.getBytes(StandardCharsets.ISO_8859_1);
                JSONReader.Context ctx = JSONFactory.createReadContext();
                JSONReaderASCIIVector jsonReader = new JSONReaderASCIIVector(ctx, json, bytes, 0, bytes.length);
                JSONObject object = (JSONObject) jsonReader.readObject();
                Object v1 = object.get(s2);
                assertEquals(s2, v1);
            }
        }
    }
}
