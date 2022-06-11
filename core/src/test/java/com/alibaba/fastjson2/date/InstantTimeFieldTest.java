package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstantTimeFieldTest {
    static final ZoneId zoneId = ZoneId.of("Asia/Shanghai");

    @Test
    public void test_0() {
        Bean bean = new Bean();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean, "yyyyMMdd HH:mm:ss");
        assertEquals("{\"value\":\"20220608 19:01:46\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "yyyyMMdd HH:mm:ss");
        assertEquals(1654686106000L, millis(bean1.value));
    }

    @Test
    public void test_yyyyMMddhhmmss19() {
        Bean bean = new Bean();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean, "yyyy-MM-dd HH:mm:ss");
        assertEquals("{\"value\":\"2022-06-08 19:01:46\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "yyyy-MM-dd HH:mm:ss");
        assertEquals(1654686106000L, millis(bean1.value));
    }

    @Test
    public void test_yyyyMMddhhmmss19_1() {
        Bean bean = new Bean();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean, "yyyy-MM-ddTHH:mm:ss");
        assertEquals("{\"value\":\"2022-06-08T19:01:46\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "yyyy-MM-ddTHH:mm:ss");
        assertEquals(1654686106000L, millis(bean1.value));
    }

    @Test
    public void test_yyyyMMddhhmmss19_2() {
        Bean bean = new Bean();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean, "yyyy-MM-dd'T'HH:mm:ss");
        assertEquals("{\"value\":\"2022-06-08T19:01:46\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "yyyy-MM-dd'T'HH:mm:ss");
        assertEquals(1654686106000L, millis(bean1.value));
    }

    @Test
    public void test_millis() {
        Bean bean = new Bean();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean, "millis");
        assertEquals("{\"value\":1654686106602}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "millis");
        assertEquals(1654686106602L, millis(bean1.value));
    }

    @Test
    public void test_millis_str() {
        String str = "{\"value\":\"1654686106602\"}";

        Bean bean1 = JSON.parseObject(str, Bean.class, "millis");
        assertEquals(1654686106602L, millis(bean1.value));
    }

    @Test
    public void test_unixtime() {
        String str = "{\"value\":1654686106}";

        Bean bean1 = JSON.parseObject(str, Bean.class, "unixtime");
        assertEquals(1654686106000L, millis(bean1.value));
    }

    @Test
    public void test_unixtime_str() {
        String str = "{\"value\":\"1654686106\"}";

        Bean bean1 = JSON.parseObject(str, Bean.class, "unixtime");
        assertEquals(1654686106000L, millis(bean1.value));
    }

    @Test
    public void test_iso8601() {
        Bean bean = new Bean();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean, "iso8601");
        assertEquals("{\"value\":\"2022-06-08T19:01:46.602+08:00\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "iso8601");
        assertEquals(1654686106602L, millis(bean1.value));
    }

    @Test
    public void test_yyyyMMdd() {
        Bean bean = new Bean();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean, "yyyyMMdd");
        assertEquals("{\"value\":\"20220608\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "yyyyMMdd");
        assertEquals(1654617600000L, millis(bean1.value));
    }

    @Test
    public void test_yyyy_MM_dd() {
        Bean bean = new Bean();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean, "yyyy-MM-dd");
        assertEquals("{\"value\":\"2022-06-08\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "yyyy-MM-dd");
        assertEquals(1654617600000L, millis(bean1.value));
    }

    @Test
    public void test_2() {
        Bean bean = new Bean();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean, "millis");
        assertEquals("{\"value\":1654686106602}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "millis");
        assertEquals(1654686106602L, millis(bean1.value));
    }

    @Test
    public void test_unixtime_1() {
        Bean bean = new Bean();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean, "unixtime");
        assertEquals("{\"value\":1654686106}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "unixtime");
        assertEquals(1654686106000L, millis(bean1.value));
    }

    public static class Bean {
        public Instant value;
    }

    public long millis(Instant instant) {
        return instant.toEpochMilli();
    }

    static Instant zdt(long millis) {
        return Instant.ofEpochMilli(millis);
    }

    @Test
    public void test_20() {
        Bean20 bean = new Bean20();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean);
        assertEquals("{\"value\":\"20220608 19:01:46\"}", str);

        Bean20 bean1 = JSON.parseObject(str, Bean20.class);
        assertEquals(1654686106000L, millis(bean1.value));
    }

    public static class Bean20 {
        @JSONField(format = "yyyyMMdd HH:mm:ss")
        public Instant value;
    }

    @Test
    public void test_21() {
        Bean21 bean = new Bean21();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean);
        assertEquals("{\"value\":\"20220608\"}", str);

        Bean21 bean1 = JSON.parseObject(str, Bean21.class);
        ZonedDateTime zdt = bean.value.atZone(zoneId);
        ZonedDateTime zdt1 = bean1.value.atZone(zoneId);
        assertEquals(zdt.getYear(), zdt1.getYear());
        assertEquals(zdt.getMonthValue(), zdt1.getMonthValue());
        assertEquals(zdt.getDayOfMonth(), zdt1.getDayOfMonth());
    }

    public static class Bean21 {
        @JSONField(format = "yyyyMMdd")
        public Instant value;
    }

    @Test
    public void test_22() {
        Bean22 bean = new Bean22();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean);
        assertEquals("{\"value\":1654686106602}", str);

        Bean22 bean1 = JSON.parseObject(str, Bean22.class);
        assertEquals(1654686106602L, millis(bean1.value));

        Bean22 bean2 = JSON.parseObject("{\"value\":\"1654686106602\"}", Bean22.class);
        assertEquals(1654686106602L, millis(bean2.value));
    }

    public static class Bean22 {
        @JSONField(format = "millis")
        public Instant value;
    }

    @Test
    public void test_23() {
        Bean23 bean = new Bean23();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean);
        assertEquals("{\"value\":1654686106}", str);

        Bean23 bean1 = JSON.parseObject(str, Bean23.class);
        assertEquals(1654686106000L, millis(bean1.value));

        Bean23 bean2 = JSON.parseObject("{\"value\":\"1654686106\"}", Bean23.class);
        assertEquals(1654686106000L, millis(bean2.value));
    }

    public static class Bean23 {
        @JSONField(format = "unixtime")
        public Instant value;
    }

    @Test
    public void test_24() {
        Bean24 bean = new Bean24();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean);
        assertEquals("{\"value\":\"2022-06-08T19:01:46.602+08:00\"}", str);

        Bean24 bean1 = JSON.parseObject(str, Bean24.class);
        assertEquals(1654686106602L, millis(bean1.value));
    }

    public static class Bean24 {
        @JSONField(format = "iso8601")
        public Instant value;
    }

    @Test
    public void test_25() {
        Bean25 bean = new Bean25();
        bean.value = zdt(1654686106602L);

        String str = JSON.toJSONString(bean);
        assertEquals("{\"value\":\"19:01:46\"}", str);

        Bean25 bean1 = JSON.parseObject(str, Bean25.class);
        ZonedDateTime zdt = bean.value.atZone(zoneId);
        ZonedDateTime zdt1 = bean1.value.atZone(zoneId);

        assertEquals(zdt.getHour(), zdt1.getHour());
        assertEquals(zdt.getMinute(), zdt1.getMinute());
        assertEquals(zdt.getSecond(), zdt1.getSecond());
    }

    public static class Bean25 {
        @JSONField(format = "HH:mm:ss")
        public Instant value;
    }
}
