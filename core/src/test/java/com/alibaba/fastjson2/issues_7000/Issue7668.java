package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class Issue7668 {
    @Test
    public void testExceedsMaxDigits() {
        int n = 10_001;
        StringBuilder sb = new StringBuilder("{\"x\":");
        for (int i = 0; i < n; i++) {
            sb.append('9');
        }
        sb.append('}');
        String json = sb.toString();

        assertThrows(JSONException.class, () -> JSON.parseObject(json));
        assertThrows(JSONException.class, () -> JSON.parseObject(json.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testWithinMaxDigits() {
        int n = 100;
        StringBuilder sb = new StringBuilder("{\"x\":");
        for (int i = 0; i < n; i++) {
            sb.append('9');
        }
        sb.append('}');
        String json = sb.toString();

        Object val = JSON.parseObject(json).get("x");
        assertInstanceOf(BigInteger.class, val);

        Object val2 = JSON.parseObject(json.getBytes(StandardCharsets.UTF_8)).get("x");
        assertInstanceOf(BigInteger.class, val2);
    }

    @Test
    public void testErrorMessage() {
        int n = 20_000;
        StringBuilder sb = new StringBuilder("{\"x\":");
        for (int i = 0; i < n; i++) {
            sb.append('9');
        }
        sb.append('}');
        String json = sb.toString();

        JSONException ex = assertThrows(JSONException.class, () -> JSON.parseObject(json));
        assertTrue(ex.getMessage().contains("Number literal too long"));
    }

    @Test
    public void testNegativeNumber() {
        int n = 10_001;
        StringBuilder sb = new StringBuilder("{\"x\":-");
        for (int i = 0; i < n; i++) {
            sb.append('9');
        }
        sb.append('}');
        String json = sb.toString();

        assertThrows(JSONException.class, () -> JSON.parseObject(json));
        assertThrows(JSONException.class, () -> JSON.parseObject(json.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testDecimalExceedsMaxDigits() {
        int n = 10_001;
        StringBuilder sb = new StringBuilder("{\"x\":0.");
        for (int i = 0; i < n; i++) {
            sb.append('9');
        }
        sb.append('}');
        String json = sb.toString();

        assertThrows(JSONException.class, () -> JSON.parseObject(json));
        assertThrows(JSONException.class, () -> JSON.parseObject(json.getBytes(StandardCharsets.UTF_8)));
    }
}
