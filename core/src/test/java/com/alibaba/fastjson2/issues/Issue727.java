package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import lombok.Data;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue727 {
    @org.junit.jupiter.api.Test
    public void test() {
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject("{\"dateTime\":\"20220-09-01 10:41:00\"}", Test.class)
        );
    }

    @Data
    public static class Test{
        private LocalDateTime dateTime;
    }
}
