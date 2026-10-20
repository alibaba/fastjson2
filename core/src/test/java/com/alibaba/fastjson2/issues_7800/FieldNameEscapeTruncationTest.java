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
class FieldNameEscapeTruncationTest {
    @Test
    public void escapeAtEndOfFieldName() {
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"\\"));
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"ab\\"));
        assertThrows(JSONException.class, () -> JSON.parseObject("{'x\\"));
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"\\".getBytes(StandardCharsets.US_ASCII)));
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"ab\\".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    public void escapedFieldNameStillParses() {
        JSONObject object = JSON.parseObject("{\"a\\tb\":1}");
        assertEquals(1, object.getIntValue("a\tb"));
    }
}
