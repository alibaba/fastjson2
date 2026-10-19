package com.alibaba.fastjson2.issues_7800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
class Issue7807 {
    @Test
    public void truncatedExponent() {
        assertThrows(JSONException.class, () -> JSON.parse("1e"));
        assertThrows(JSONException.class, () -> JSON.parse("1E"));
        assertThrows(JSONException.class, () -> JSON.parse("1e-"));
        assertThrows(JSONException.class, () -> JSON.parse("1e+"));
        assertThrows(JSONException.class, () -> JSON.parse("-2.2e"));
        assertThrows(JSONException.class, () -> JSON.parse("[1e"));
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\":1e"));
    }

    @Test
    public void truncatedExponentUtf16() {
        String cn = new String(new char[]{0x4e2d});
        assertThrows(JSONException.class, () -> JSON.parse("[\"" + cn + "\",1e"));
    }

    @Test
    public void truncatedExponentUtf8Bytes() {
        String cn = new String(new char[]{0x4e2d});
        assertThrows(JSONException.class, () -> JSON.parse("1e".getBytes(StandardCharsets.UTF_8)));
        assertThrows(JSONException.class, () -> JSON.parse("1e-".getBytes(StandardCharsets.UTF_8)));
        assertThrows(JSONException.class, () -> JSON.parse(("[\"" + cn + "\",1e").getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void validExponentStillParses() {
        assertEquals(100000.0, ((Number) JSON.parse("1e5")).doubleValue(), 1e-9);
        assertEquals(0.0015, ((Number) JSON.parse("1.5e-3")).doubleValue(), 1e-12);
        assertEquals(-2.0E10, ((Number) JSON.parse("-2E+10")).doubleValue(), 1e-3);
    }

    @Test
    public void quotedTruncatedExponentKeepsCoercion() {
        assertEquals(1.0, JSON.parseObject("\"1e\"", Double.class), 0.0);
        assertEquals(1.0, JSON.parseObject("\"1e+\"", Double.class), 0.0);
        assertEquals(100000.0, JSON.parseObject("\"1e5\"", Double.class), 1e-9);
    }
}
