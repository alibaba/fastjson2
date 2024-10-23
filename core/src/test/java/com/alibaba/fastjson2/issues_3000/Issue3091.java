package com.alibaba.fastjson2.issues_3000;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue3091 {
    @Test
    public void test() {
        String json = "{\"time\":0}";
        JSONObject jsonObject = JSONObject.parseObject(json);
        Time2 bean = jsonObject.toJavaObject(Time2.class);
        assertNotNull(bean.time);
        assertEquals(1970, bean.time.getYear());
    }

    @Data
    public static class Time2 {
        private LocalDateTime time;
    }
}
