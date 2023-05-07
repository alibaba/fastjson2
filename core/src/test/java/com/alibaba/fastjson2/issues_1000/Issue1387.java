package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1387 {
    @Test
    public void test() {
        String json = "{\"date\":\"2021\\nNOV\"}";
        Bean params = JSON.parseObject(json, Bean.class);
        assertEquals(json, JSON.toJSONString(params));
        assertEquals(json, new String(JSON.toJSONBytes(params)));
    }

    public static class Bean {
        private String date;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }
}
