package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2328 {
    @Test
    public void test() throws Exception {
        LocalDateTime ldt = LocalDateTime.of(2018, 2, 3, 12, 13, 14);
        Bean bean = new Bean();
        bean.createdAt = ZonedDateTime.of(ldt, ZoneId.of("Asia/Shanghai"));
        String str = JSON.toJSONString(bean);
        assertTrue(str.equals("{\"createdAt\":\"星期六 二月 03 12:13:14 +0800 2018\"}")
                || str.equals("{\"createdAt\":\"周六 2月 03 12:13:14 +0800 2018\"}"));

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.createdAt, bean1.createdAt);
    }

    public static class Bean {
        @JSONField(format = "E MMM dd HH:mm:ss Z yyyy", locale = "zh_CN")
        public ZonedDateTime createdAt;
    }

    @Test
    public void test1() throws Exception {
        LocalDateTime ldt = LocalDateTime.of(2018, 2, 3, 12, 13, 14);
        Bean1 bean = new Bean1();
        bean.createdAt = ldt;
        String str = JSON.toJSONString(bean);
        assertTrue(str.equals("{\"createdAt\":\"星期六 二月 03 12:13:14 +0800 2018\"}")
                || str.equals("{\"createdAt\":\"周六 2月 03 12:13:14 +0800 2018\"}"));

        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.createdAt, bean1.createdAt);
    }

    public static class Bean1 {
        @JSONField(format = "E MMM dd HH:mm:ss Z yyyy", locale = "zh_CN")
        public LocalDateTime createdAt;
    }

    @Test
    public void test2() throws Exception {
        LocalDateTime ldt = LocalDateTime.of(2018, 2, 3, 12, 13, 14);
        Bean2 bean = new Bean2();
        bean.createdAt = OffsetDateTime.of(ldt, ZoneOffset.ofHours(8));
        String str = JSON.toJSONString(bean);
        assertTrue(str.equals("{\"createdAt\":\"星期六 二月 03 12:13:14 +0800 2018\"}")
                || str.equals("{\"createdAt\":\"周六 2月 03 12:13:14 +0800 2018\"}"));

        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(bean.createdAt, bean1.createdAt);
    }

    public static class Bean2 {
        @JSONField(format = "E MMM dd HH:mm:ss Z yyyy", locale = "zh_CN")
        public OffsetDateTime createdAt;
    }
}
