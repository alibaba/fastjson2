package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2817 {
    @Test
    public void test() {
        long millis = System.currentTimeMillis();
        Bean bean = JSON.parseObject("{\"time\":" + millis + "}", Bean.class);

        assertEquals(
                Instant.ofEpochMilli(millis).atZone(DateUtils.DEFAULT_ZONE_ID).toLocalDateTime(),
                bean.time);
    }

    public static class Bean {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public LocalDateTime time;
    }
}
