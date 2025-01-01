package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue3210 {
    @Data
    public static class Bean {
        private Date time;
    }

    @Test
    public void test() {
        String s = "{\"time\":\"2024-12-04T20:43:15.000000999\"}";
        Bean bean = JSONObject.parseObject(s, Bean.class);
        assertNotNull(bean.time);
    }
}
