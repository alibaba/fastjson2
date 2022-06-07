package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalTimeTest {
    @Test
    public void test0() {
        Bean bean = new Bean();
        bean.time = LocalTime.of(12, 13, 14);
        String str = JSON.toJSONString(bean, "yyyy-MM-dd HH:mm:ss");
        assertEquals("{\"time\":\"1970-01-01 12:13:14\"}", str);
    }

    public static class Bean {
        public LocalTime time;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.time = LocalTime.of(12, 13, 14);
        String str = JSON.toJSONString(bean, "yyyy-MM-dd HH:mm:ss");
        assertEquals("{\"time\":\"1970-01-01 12:13:14.000\"}", str);
    }

    public static class Bean1 {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
        public LocalTime time;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.time = LocalTime.of(12, 13, 14);
        String str = JSON.toJSONString(bean, "yyyy-MM-dd HH:mm:ss");
        assertEquals("{\"time\":\"1970-01-01 12:13:14\"}", str);
    }

    public static class Bean2 {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public LocalTime time;
    }
}
