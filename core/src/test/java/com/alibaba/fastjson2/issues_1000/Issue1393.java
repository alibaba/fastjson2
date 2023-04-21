package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1393 {
    @Test
    public void test1() {
        String dateStr = "2023-04-21 21:03:43.1";
        long millis = DateUtils.parseMillis(dateStr);

        JSONObject jsonObject = JSONObject.of(
                "date", dateStr,
                "ldt", dateStr,
                "zdt", dateStr,
                "instant", dateStr
        );
        String json = jsonObject.toString();
        Bean bean = JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(bean.instant);
        assertEquals(millis, bean.date.getTime());
        assertEquals(millis, bean.ldt.atZone(DateUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli());
        assertEquals(millis, bean.zdt.toInstant().toEpochMilli());
        assertEquals(millis, bean.instant.toEpochMilli());

        Bean bean1 = jsonObject.toJavaObject(Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        assertEquals(bean.ldt, bean1.ldt);
        assertEquals(bean.zdt, bean1.zdt);
        assertEquals(bean.instant, bean1.instant);
    }

    @Test
    public void test2() {
        String dateStr = "2023-04-21 21:03:43.12";
        long millis = DateUtils.parseMillis(dateStr);

        JSONObject jsonObject = JSONObject.of(
                "date", dateStr,
                "ldt", dateStr,
                "zdt", dateStr,
                "instant", dateStr
        );
        String json = jsonObject.toString();
        Bean bean = JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(bean.instant);
        assertEquals(millis, bean.date.getTime());
        assertEquals(millis, bean.ldt.atZone(DateUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli());
        assertEquals(millis, bean.zdt.toInstant().toEpochMilli());
        assertEquals(millis, bean.instant.toEpochMilli());

        Bean bean1 = jsonObject.toJavaObject(Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        assertEquals(bean.ldt, bean1.ldt);
        assertEquals(bean.zdt, bean1.zdt);
        assertEquals(bean.instant, bean1.instant);
    }

    @Test
    public void test3() {
        String dateStr = "2023-04-21 21:03:43.071";
        long millis = DateUtils.parseMillis(dateStr);

        JSONObject jsonObject = JSONObject.of(
                "date", dateStr,
                "ldt", dateStr,
                "zdt", dateStr,
                "instant", dateStr
        );
        String json = jsonObject.toString();
        Bean bean = JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(bean.instant);
        assertEquals(millis, bean.date.getTime());
        assertEquals(millis, bean.ldt.atZone(DateUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli());
        assertEquals(millis, bean.zdt.toInstant().toEpochMilli());
        assertEquals(millis, bean.instant.toEpochMilli());

        Bean bean1 = jsonObject.toJavaObject(Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        assertEquals(bean.ldt, bean1.ldt);
        assertEquals(bean.zdt, bean1.zdt);
        assertEquals(bean.instant, bean1.instant);
    }

    @Test
    public void test4() {
        String dateStr = "2023-04-21 21:03:43.1234";
        long millis = DateUtils.parseMillis(dateStr);

        JSONObject jsonObject = JSONObject.of(
                "date", dateStr,
                "ldt", dateStr,
                "zdt", dateStr,
                "instant", dateStr
        );
        String json = jsonObject.toString();
        Bean bean = JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(bean.instant);
        assertEquals(millis, bean.date.getTime());
        assertEquals(millis, bean.ldt.atZone(DateUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli());
        assertEquals(millis, bean.zdt.toInstant().toEpochMilli());
        assertEquals(millis, bean.instant.toEpochMilli());

        Bean bean1 = jsonObject.toJavaObject(Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        assertEquals(bean.ldt, bean1.ldt);
        assertEquals(bean.zdt, bean1.zdt);
        assertEquals(bean.instant, bean1.instant);
    }

    @Test
    public void test5() {
        String dateStr = "2023-04-21 21:03:43.12345";
        long millis = DateUtils.parseMillis(dateStr);

        JSONObject jsonObject = JSONObject.of(
                "date", dateStr,
                "ldt", dateStr,
                "zdt", dateStr,
                "instant", dateStr
        );
        String json = jsonObject.toString();
        Bean bean = JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(bean.instant);
        assertEquals(millis, bean.date.getTime());
        assertEquals(millis, bean.ldt.atZone(DateUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli());
        assertEquals(millis, bean.zdt.toInstant().toEpochMilli());
        assertEquals(millis, bean.instant.toEpochMilli());

        Bean bean1 = jsonObject.toJavaObject(Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        assertEquals(bean.ldt, bean1.ldt);
        assertEquals(bean.zdt, bean1.zdt);
        assertEquals(bean.instant, bean1.instant);
    }

    @Test
    public void test6() {
        String dateStr = "2023-04-21 21:03:43.123456";
        long millis = DateUtils.parseMillis(dateStr);

        JSONObject jsonObject = JSONObject.of(
                "date", dateStr,
                "ldt", dateStr,
                "zdt", dateStr,
                "instant", dateStr
        );
        String json = jsonObject.toString();
        Bean bean = JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(bean.instant);
        assertEquals(millis, bean.date.getTime());
        assertEquals(millis, bean.ldt.atZone(DateUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli());
        assertEquals(millis, bean.zdt.toInstant().toEpochMilli());
        assertEquals(millis, bean.instant.toEpochMilli());

        Bean bean1 = jsonObject.toJavaObject(Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        assertEquals(bean.ldt, bean1.ldt);
        assertEquals(bean.zdt, bean1.zdt);
        assertEquals(bean.instant, bean1.instant);
    }

    @Test
    public void test7() {
        String dateStr = "2023-04-21 21:03:43.1234567";
        long millis = DateUtils.parseMillis(dateStr);

        JSONObject jsonObject = JSONObject.of(
                "date", dateStr,
                "ldt", dateStr,
                "zdt", dateStr,
                "instant", dateStr
        );
        String json = jsonObject.toString();
        Bean bean = JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(bean.instant);
        assertEquals(millis, bean.date.getTime());
        assertEquals(millis, bean.ldt.atZone(DateUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli());
        assertEquals(millis, bean.zdt.toInstant().toEpochMilli());
        assertEquals(millis, bean.instant.toEpochMilli());

        Bean bean1 = jsonObject.toJavaObject(Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        assertEquals(bean.ldt, bean1.ldt);
        assertEquals(bean.zdt, bean1.zdt);
        assertEquals(bean.instant, bean1.instant);
    }

    @Test
    public void test8() {
        String dateStr = "2023-04-21 21:03:43.12345678";
        long millis = DateUtils.parseMillis(dateStr);

        JSONObject jsonObject = JSONObject.of(
                "date", dateStr,
                "ldt", dateStr,
                "zdt", dateStr,
                "instant", dateStr
        );
        String json = jsonObject.toString();
        Bean bean = JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(bean.instant);
        assertEquals(millis, bean.date.getTime());
        assertEquals(millis, bean.ldt.atZone(DateUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli());
        assertEquals(millis, bean.zdt.toInstant().toEpochMilli());
        assertEquals(millis, bean.instant.toEpochMilli());

        Bean bean1 = jsonObject.toJavaObject(Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        assertEquals(bean.ldt, bean1.ldt);
        assertEquals(bean.zdt, bean1.zdt);
        assertEquals(bean.instant, bean1.instant);
    }

    @Test
    public void test9() {
        String dateStr = "2023-04-21 21:03:43.123456789";
        long millis = DateUtils.parseMillis(dateStr);

        JSONObject jsonObject = JSONObject.of(
                "date", dateStr,
                "ldt", dateStr,
                "zdt", dateStr,
                "instant", dateStr
        );
        String json = jsonObject.toString();
        Bean bean = JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(bean.instant);
        assertEquals(millis, bean.date.getTime());
        assertEquals(millis, bean.ldt.atZone(DateUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli());
        assertEquals(millis, bean.zdt.toInstant().toEpochMilli());
        assertEquals(millis, bean.instant.toEpochMilli());

        Bean bean1 = jsonObject.toJavaObject(Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        assertEquals(bean.ldt, bean1.ldt);
        assertEquals(bean.zdt, bean1.zdt);
        assertEquals(bean.instant, bean1.instant);
    }

    public static class Bean {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public Date date;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public LocalDateTime ldt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public ZonedDateTime zdt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public Instant instant;
    }
}
