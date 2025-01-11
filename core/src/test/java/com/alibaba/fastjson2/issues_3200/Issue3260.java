package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue3260 {
    @Test
    public void test() {
        String str = "{\"dns\":[\"<>\"/'\"],\"operation\":\"init\"}";
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str));
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str.toCharArray()));
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str.getBytes(StandardCharsets.UTF_8)));
    }
}
