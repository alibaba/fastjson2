package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

public class Issue1790 {
    @Test
    public void test() {
        String json = "{\"op\":\"yyy\",\"test\":\"{\\\"erw\\\":{\\\"qqq\\\":12313}}\"}";
        Bean a = JSON.parseObject(json, Bean.class);
        System.out.println(a);
    }

    @Data
    public class Bean {
        private String op;
        private JSONObject test;
    }
}
