package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
public class Issue2522 {
    @Test
    public void test() {
        String str = "{[1,2]}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        assertThrows(
                JSONException.class,
                () -> JSON.parse(str));
        assertThrows(
                JSONException.class,
                () -> JSON.parse(str.toCharArray()));
        assertThrows(
                JSONException.class,
                () -> JSON.parse(utf8));
    }
}
