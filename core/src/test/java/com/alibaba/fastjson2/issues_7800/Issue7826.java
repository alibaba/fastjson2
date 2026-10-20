package com.alibaba.fastjson2.issues_7800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
class Issue7826 {
    @Test
    public void trailingWhitespaceAfterFieldName() {
        String cn = new String(new char[]{0x4e2d});
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\" "));
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\"\t"));
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\" ".getBytes(StandardCharsets.UTF_8)));
        assertThrows(JSONException.class, () -> JSON.parse(("{\"" + cn + "\" ").getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void trailingWhitespaceAfterColon() {
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\": "));
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\":\t"));
    }

    @Test
    public void whitespaceAroundColonStillParses() {
        JSONObject object = JSON.parseObject("{\"a\" \n :\t 1}");
        assertEquals(1, object.getIntValue("a"));
    }
}
