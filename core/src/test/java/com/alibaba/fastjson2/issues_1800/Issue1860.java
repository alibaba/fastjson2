package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue1860 {
    @Test
    public void test() {
        assertThrows(JSONException.class, () -> JSON.parse("+"));
        assertThrows(JSONException.class, () -> JSON.parse("+".getBytes()));

        assertThrows(JSONException.class, () -> JSON.parse("-"));
        assertThrows(JSONException.class, () -> JSON.parse("-".getBytes()));
    }
}
