package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalDateTest {
    @Test
    public void test0() {
        Bean bean = new Bean();
        bean.date = LocalDate.of(2017, 9, 11);
        String str = JSON.toJSONString(bean, "yyyy-MM-dd HH:mm:ss");
        assertEquals("{\"date\":\"2017-09-11 00:00:00\"}", str);
    }

    public static class Bean {
        public LocalDate date;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.date = LocalDate.of(2017, 9, 11);
        String str = JSON.toJSONString(bean, "yyyy-MM-dd HH:mm:ss");
        assertEquals("{\"date\":\"2017-09-11 00:00:00.000\"}", str);
    }

    public static class Bean1 {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
        public LocalDate date;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.date = LocalDate.of(2017, 9, 11);
        String str = JSON.toJSONString(bean, "yyyy-MM-dd HH:mm:ss");
        assertEquals("{\"date\":\"2017-09-11 00:00:00\"}", str);
    }

    public static class Bean2 {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public LocalDate date;
    }
}
