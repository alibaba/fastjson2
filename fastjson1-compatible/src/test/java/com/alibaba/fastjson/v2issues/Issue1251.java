package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue1251 {
    @Test
    public void test() {
        String str = "|[fd listen socket: /tmp/dev_dc1_pubvsnutlog2v1.2.23.sock]\n" +
                "        |";
        assertFalse(JSON.isValidArray(str));
        assertFalse(JSONValidator.from(str).validate());
    }
}
