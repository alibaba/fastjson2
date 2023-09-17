package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.stream.JSONStreamReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1869 {
    @Test
    public void test() throws Exception {
        String str = "{" +
                "    \"requestId\": \"8961167a73fa40e38feda862cd57b311\"," +
                "    \"success\": true," +
                "    \"code\": 200," +
                "    \"msg\": \"成功\"" +
                "}";

        try (InputStream fis = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))) {
            JSONStreamReader reader = JSONStreamReader.of(fis);
            Object object;
            while ((object = reader.readLineObject()) != null) {
                JSONObject jsonObject = (JSONObject) object;
                assertEquals(4, jsonObject.size());
            }
        }
    }
}
