package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1811 {
    @Test
    public void test() {
        String json = "{\"remark\":\"1. ¡Múltiples\\n 2. six\" }";
        Request req = JSON.parseObject(json, Request.class);
        assertEquals("1. ¡Múltiples\n 2. six", req.remark);
        String str1 = JSON.toJSONString(req);
        Request req2 = JSON.parseObject(str1, Request.class);
        assertEquals(req.remark, req2.remark);
    }

    public static class Request {
        public String remark;
    }
}
