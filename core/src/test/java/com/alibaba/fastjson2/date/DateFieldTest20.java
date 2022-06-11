package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateFieldTest20 {
    @Test
    public void test_0() {
        Bean bean = new Bean();
        bean.value = new Date(1654686106602L);

        String str = JSON.toJSONString(bean, "yyyyMMdd HH:mm:ss");
        assertEquals("{\"value\":\"20220608 19:01:46\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "yyyyMMdd HH:mm:ss");
        assertEquals(1654686106000L, bean1.value.getTime());
    }

    @Test
    public void test_yyyyMMddhhmmss19() {
        Bean bean = new Bean();
        bean.value = new Date(1654686106602L);

        String str = JSON.toJSONString(bean, "yyyy-MM-dd HH:mm:ss");
        assertEquals("{\"value\":\"2022-06-08 19:01:46\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "yyyy-MM-dd HH:mm:ss");
        assertEquals(1654686106000L, bean1.value.getTime());
    }

    @Test
    public void test_yyyyMMddhhmmss19_1() {
        Bean bean = new Bean();
        bean.value = new Date(1654686106602L);

        String str = JSON.toJSONString(bean, "yyyy-MM-ddTHH:mm:ss");
        assertEquals("{\"value\":\"2022-06-08T19:01:46\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "yyyy-MM-ddTHH:mm:ss");
        assertEquals(1654686106000L, bean1.value.getTime());
    }

    @Test
    public void test_yyyyMMddhhmmss19_2() {
        Bean bean = new Bean();
        bean.value = new Date(1654686106602L);

        String str = JSON.toJSONString(bean, "yyyy-MM-dd'T'HH:mm:ss");
        assertEquals("{\"value\":\"2022-06-08T19:01:46\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "yyyy-MM-dd'T'HH:mm:ss");
        assertEquals(1654686106000L, bean1.value.getTime());
    }

    @Test
    public void test_millis() {
        Bean bean = new Bean();
        bean.value = new Date(1654686106602L);

        String str = JSON.toJSONString(bean, "millis");
        assertEquals("{\"value\":1654686106602}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "millis");
        assertEquals(1654686106602L, bean1.value.getTime());
    }

    @Test
    public void test_millis_str() {
        String str = "{\"value\":\"1654686106602\"}";

        Bean bean1 = JSON.parseObject(str, Bean.class, "millis");
        assertEquals(1654686106602L, bean1.value.getTime());
    }

    @Test
    public void test_unixtime_str() {
        String str = "{\"value\":\"1654686106\"}";

        Bean bean1 = JSON.parseObject(str, Bean.class, "unixtime");
        assertEquals(1654686106000L, bean1.value.getTime());
    }

    @Test
    public void test_iso8601() {
        Bean bean = new Bean();
        bean.value = new Date(1654686106602L);

        String str = JSON.toJSONString(bean, "iso8601");
        assertEquals("{\"value\":\"2022-06-08T19:01:46.602+08:00\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "iso8601");
        assertEquals(1654686106602L, bean1.value.getTime());
    }

    @Test
    public void test_yyyyMMdd() {
        Bean bean = new Bean();
        bean.value = new Date(1654686106602L);

        String str = JSON.toJSONString(bean, "yyyyMMdd");
        assertEquals("{\"value\":\"20220608\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "yyyyMMdd");
        assertEquals(1654617600000L, bean1.value.getTime());
    }

    @Test
    public void test_yyyy_MM_dd() {
        Bean bean = new Bean();
        bean.value = new Date(1654686106602L);

        String str = JSON.toJSONString(bean, "yyyy-MM-dd");
        assertEquals("{\"value\":\"2022-06-08\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "yyyy-MM-dd");
        assertEquals(1654617600000L, bean1.value.getTime());
    }

    @Test
    public void test_2() {
        Bean bean = new Bean();
        bean.value = new Date(1654686106602L);

        String str = JSON.toJSONString(bean, "millis");
        assertEquals("{\"value\":1654686106602}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "millis");
        assertEquals(1654686106602L, bean1.value.getTime());
    }

    @Test
    public void test_3() {
        Bean bean = new Bean();
        bean.value = new Date(1654686106602L);

        String str = JSON.toJSONString(bean, "unixtime");
        assertEquals("{\"value\":1654686106}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class, "unixtime");
        assertEquals(1654686106, bean1.value.getTime());
    }

    public static class Bean {
        public Date value;
    }

    @Test
    public void test_20() {
        Bean20 bean = new Bean20();
        bean.value = new Date(1654686106602L);

        String str = JSON.toJSONString(bean);
        assertEquals("{\"value\":\"20220608 19:01:46\"}", str);

        Bean20 bean1 = JSON.parseObject(str, Bean20.class);
        assertEquals(1654686106000L, bean1.value.getTime());
    }

    public static class Bean20 {
        @JSONField(format = "yyyyMMdd HH:mm:ss")
        public Date value;
    }
}
