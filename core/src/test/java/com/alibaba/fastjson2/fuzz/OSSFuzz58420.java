package com.alibaba.fastjson2.fuzz;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class OSSFuzz58420 {
    @Test
    public void test() {
        assertThrows(JSONException.class, () -> JSON.parse("newDate('@65537\u0001\u000B-65535tty=#':"));
    }
}
