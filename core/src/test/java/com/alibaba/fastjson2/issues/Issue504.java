package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue504 {
    @Test
    public void test() {
        assertNull(JSON.parseObject("{\"time\":\"\"}").to(Bean.class).time);
        assertNull(JSON.parseObject("{\"time\":\"null\"}").to(Bean.class).time);
        assertNull(JSON.parseObject("{\"time\":null}").to(Bean.class).time);
    }
    public static class Bean {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public Date time;
    }
}
