package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue2197 {
    @Test
    public void test() {
        Exception error = null;
        try {
            JSON.parse("Hello World!");
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("offset 1"));
        assertTrue(error.getMessage().contains("line 1"));
        assertTrue(error.getMessage().contains("column 1"));
        assertTrue(error.getMessage().contains("character H"));
    }
}
