package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2128 {

    @Test
    public void testWithUTF8() {
        String str = "{\"amt\":-22.1400000000000005684341886080801486968994140625}";
        assertEquals(str, JSON.toJSONString(JSONReader.of(str.getBytes(StandardCharsets.UTF_8)).readObject()));
    }

    @Test
    public void testWithUTF16() {
        String str = "{\"amt\":-22.1400000000000005684341886080801486968994140625}";
        assertEquals(str, JSON.toJSONString(JSONObject.parseObject(str, Map.class)));
    }
}
