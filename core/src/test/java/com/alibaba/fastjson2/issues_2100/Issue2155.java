package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue2155 {
    @Test
    public void test() {
        assertThrows(
                JSONException.class,
                () -> JSONObject.parse("{1,2}"));
    }
}
