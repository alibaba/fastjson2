package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue380 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.date = LocalDate.of(2017, 6, 28);

        assertEquals("{\"date\":\"2017-06-28 00:00:00\"}", JSON.toJSONString(bean));
        assertEquals("{\"date\":\"2017-06-28 00:00:00\"}", JSON.toJSONString(bean, "yyyy-MM-dd"));
    }

    public static class Bean {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public LocalDate date;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.date = LocalDateTime.of(2017, 6, 28, 11, 12, 13);

        assertEquals("{\"date\":\"2017-06-28\"}", JSON.toJSONString(bean));
        assertEquals("{\"date\":\"2017-06-28\"}", JSON.toJSONString(bean, "yyyy-MM-dd"));
    }

    public static class Bean1 {
        @JSONField(format = "yyyy-MM-dd")
        public LocalDateTime date;
    }

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
