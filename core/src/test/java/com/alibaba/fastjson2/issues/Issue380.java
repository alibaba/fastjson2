package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue380 {
    @Test
    public void test2() {
        ZonedDateTime zdt = LocalDateTime.of(2017, 6, 28, 11, 12, 13)
                .atZone(
                        ZoneId.of("Asia/Shanghai")
                );
        Bean2 bean = new Bean2();
        bean.date = new Date(zdt.toInstant().toEpochMilli());

        assertEquals("{\"date\":\"2017-06-28\"}", JSON.toJSONString(bean));
        assertEquals("{\"date\":\"2017-06-28\"}", JSON.toJSONString(bean, "yyyy-MM-dd HH:mm:ss"));
    }

    public static class Bean2 {
        @JSONField(format = "yyyy-MM-dd")
        public java.util.Date date;
    }
}
