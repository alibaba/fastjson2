package com.alibaba.fastjson2.internal.processor.primitives;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTypeTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.v01 = new java.util.Date();
        bean.v02 = java.util.Calendar.getInstance();
        bean.v03 = new java.sql.Date(1687104000000L);
        bean.v04 = new java.sql.Time(9729000L);
        bean.v05 = new java.sql.Timestamp(System.currentTimeMillis());
        bean.v06 = java.time.LocalDate.now();
        bean.v07 = java.time.LocalTime.of(19, 38, 12);
        bean.v08 = java.time.LocalDateTime.now();
        bean.v09 = java.time.ZonedDateTime.now();
        bean.v10 = java.time.OffsetDateTime.now();
//        bean.v11 = java.time.OffsetTime.now();

        String str = JSON.toJSONString(bean);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.v01, bean1.v01);
        assertEquals(bean.v02, bean1.v02);
        assertEquals(bean.v03.getTime(), bean1.v03.getTime());
        assertEquals(bean.v04.getTime(), bean1.v04.getTime());
        assertEquals(bean.v05, bean1.v05);
        assertEquals(bean.v06, bean1.v06);
        assertEquals(bean.v07, bean1.v07);
        assertEquals(bean.v07, bean1.v07);
        assertEquals(bean.v08, bean1.v08);
        assertEquals(bean.v09, bean1.v09);
        assertEquals(bean.v10, bean1.v10);
//        assertEquals(bean.v11, bean1.v11);
    }

    @JSONCompiled
    public static class Bean {
        public java.util.Date v01;
        public java.util.Calendar v02;
        public java.sql.Date v03;
        public java.sql.Time v04;
        public java.sql.Timestamp v05;
        public java.time.LocalDate v06;
        public java.time.LocalTime v07;
        public java.time.LocalDateTime v08;
        public java.time.ZonedDateTime v09;
        public java.time.OffsetDateTime v10;
        public java.time.OffsetTime v11;
    }
}
