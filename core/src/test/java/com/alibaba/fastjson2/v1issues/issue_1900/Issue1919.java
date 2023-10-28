package com.alibaba.fastjson2.v1issues.issue_1900;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

public class Issue1919 {
    @Test
    public void test() {
        StringBuilder buf = new StringBuilder("{\"precedeId\":1.");
        for (int i = 0; i < 2500; i++) {
            buf.append('0');
        }
        String a = buf.append("}").toString();
        JSON.parseObject(a);
    }
}
