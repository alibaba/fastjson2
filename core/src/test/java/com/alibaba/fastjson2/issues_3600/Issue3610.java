package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue3610 {
    @Test
    public void test() {
        String str = "{\"screenWidth\":1920,\"isSupportSmallWindow\"";
        assertThrows(JSONException.class, () -> JSON.parseObject(str.getBytes(StandardCharsets.UTF_8)));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.toCharArray()));
        assertThrows(JSONException.class, () -> JSON.parseObject(str));
    }

    @Test
    public void testUTF8() {
        String str = "{\"中文\":1920,\"isSupportSmallWindow\"";
        assertThrows(JSONException.class, () -> JSON.parseObject(str.getBytes(StandardCharsets.UTF_8)));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.toCharArray()));
        assertThrows(JSONException.class, () -> JSON.parseObject(str));
    }
}
