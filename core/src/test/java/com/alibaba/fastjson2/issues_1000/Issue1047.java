package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSON.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue1047 {
    @Test
    public void test() {
        assertThrows(JSONException.class, () -> to(Byte[].class, "[1"));
        assertThrows(JSONException.class, () -> to(byte[].class, "[1"));
        assertThrows(JSONException.class, () -> to(Short[].class, "[1"));
        assertThrows(JSONException.class, () -> to(short[].class, "[1"));
        assertThrows(JSONException.class, () -> to(Integer[].class, "[1"));
        assertThrows(JSONException.class, () -> to(int[].class, "[1"));
        assertThrows(JSONException.class, () -> to(Long[].class, "[1"));
        assertThrows(JSONException.class, () -> to(long[].class, "[1"));
        assertThrows(JSONException.class, () -> to(Float[].class, "[1"));
        assertThrows(JSONException.class, () -> to(float[].class, "[1"));
        assertThrows(JSONException.class, () -> to(Double[].class, "[1"));
        assertThrows(JSONException.class, () -> to(double[].class, "[1"));
        assertThrows(JSONException.class, () -> to(char[].class, "[1"));
    }
}
