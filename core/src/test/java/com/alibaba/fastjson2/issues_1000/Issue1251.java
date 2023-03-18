package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue1251 {
    @Test
    public void test() {
        String str = "|[fd listen socket: /tmp/dev_dc1_pubvsnutlog2v1.2.23.sock]\n" +
                "        |";
        assertFalse(JSON.isValidArray(str));
    }
}
