package com.alibaba.fastjson2.issues_3800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue3883 {
    @Test
    public void jsonReaderUTF8ArrayOOBTest() {
        String str = ".";
        assertThrows(JSONException.class, () -> JSON.parse(str.toCharArray())); // UTF16
        assertThrows(JSONException.class, () -> JSON.parse(str.getBytes(StandardCharsets.UTF_8))); // UTF8
        assertThrows(JSONException.class, () -> JSON.parse(str)); // JDK 8 UTF16, JDK 9+ UTF8
    }
}
