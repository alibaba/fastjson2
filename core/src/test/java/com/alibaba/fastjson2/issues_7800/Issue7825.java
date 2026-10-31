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
class Issue7825 {
    @Test
    public void truncatedMultiLineComment() {
        String cn = new String(new char[]{0x4e2d});
        assertThrows(JSONException.class, () -> JSON.parse("{/**"));
        assertThrows(JSONException.class, () -> JSON.parse("{/*"));
        assertThrows(JSONException.class, () -> JSON.parse("{/**".getBytes(StandardCharsets.UTF_8)));
        assertThrows(JSONException.class, () -> JSON.parse(("{/*" + cn + "/**").getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void validCommentStillParses() {
        JSONObject object = JSON.parseObject("{/*c*/\"a\":1}");
        assertEquals(1, object.getIntValue("a"));
        assertEquals(1, JSON.parseObject("{\"a\":1//c\n}").getIntValue("a"));
    }
}
