package com.alibaba.fastjson.issue_1900;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1996 {
    @Test
    public void test_for_issue() throws Exception {
        StringBuilder sb = new StringBuilder();

        char start = '\uD800';
        char end = '\uDFFF';

        for (char i = start; i <= end; i++) {
            if (Character.isLowSurrogate(i)) {
                sb.append(i);
            }
        }
        String s = sb.toString();

        // ok
        String json1 = JSON.toJSONString(s);
        byte[] bytes = json1.getBytes("utf-8");

        byte[] bytes2 = JSON.toJSONBytes(s);
        assertEquals(new String(bytes), new String(bytes2));

        assertEquals(bytes.length, bytes2.length);
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(bytes[i], bytes[i]);
        }
    }
}
