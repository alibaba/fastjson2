package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2_vo.LocalDate1;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalDateTest {
    @Test
    public void test_jsonb() {
        LocalDate1 vo = new LocalDate1();
        vo.setDate(LocalDate.now());
        byte[] jsonbBytes = JSONB.toBytes(vo);

        LocalDate1 v1 = JSONB.parseObject(jsonbBytes, LocalDate1.class);
        assertEquals(vo.getDate(), v1.getDate());
    }

    @Test
    public void test_jsonb_str() {
        LocalDate now = LocalDate.now();
        String str = now.toString();
        byte[] jsonbBytes = JSONB.toBytes(str);

        LocalDate localDate = JSONB.parseObject(jsonbBytes, LocalDate.class);
        assertEquals(now, localDate);
    }

    @Test
    public void test_jsonb_str_bytes() {
        LocalDate now = LocalDate.of(2022, 7, 17);
        byte[] jsonbBytes = {122, 10, 50, 48, 50, 50, 45, 48, 55, 45, 49, 55};

        LocalDate localDate = JSONB.parseObject(jsonbBytes, LocalDate.class);
        assertEquals(now, localDate);
    }

    @Test
    public void test_utf8() {
        LocalDate1 vo = new LocalDate1();
        vo.setDate(LocalDate.now());
        byte[] utf8 = JSON.toJSONBytes(vo);

        LocalDate1 v1 = JSON.parseObject(utf8, LocalDate1.class);
        assertEquals(vo.getDate(), v1.getDate());
    }

    @Test
    public void test_str() {
        LocalDate1 vo = new LocalDate1();
        vo.setDate(LocalDate.now());
        String str = JSON.toJSONString(vo);

        LocalDate1 v1 = JSON.parseObject(str, LocalDate1.class);
        assertEquals(vo.getDate(), v1.getDate());
    }

    @Test
    public void test_str_1() {
        String str = "{\"date\":\"2021年2月3日\"}";
        LocalDate1 vo = JSON.parseObject(str, LocalDate1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(2, vo.getDate().getMonthValue());
        assertEquals(3, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_2() {
        String str = "{\"date\":\"2021年12月1日\"}";
        LocalDate1 vo = JSON.parseObject(str, LocalDate1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(1, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_3() {
        String str = "{\"date\":\"2021年12月11日\"}";
        LocalDate1 vo = JSON.parseObject(str, LocalDate1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_4() {
        String str = "{\"date\":\"2021-12-11\"}";
        LocalDate1 vo = JSON.parseObject(str, LocalDate1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_4_utf8() {
        String str = "{\"date\":\"2021-12-11\"}";
        LocalDate1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDate1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_5() {
        String str = "{\"date\":\"20211211\"}";
        LocalDate1 vo = JSON.parseObject(str, LocalDate1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_5_utf8() {
        String str = "{\"date\":\"20211211\"}";
        LocalDate1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDate1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_6() {
        String str = "\r\t\b\f {\"date\":\"2021-2-1\"}";
        LocalDate1 vo = JSON.parseObject(str, LocalDate1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(2, vo.getDate().getMonthValue());
        assertEquals(1, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_6_utf16() {
        String str = "\r\t\b\f {\"date\":\"2021-2-1\"}";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_16);
        LocalDate1 vo = JSON.parseObject(strBytes, 0, strBytes.length, StandardCharsets.UTF_16, LocalDate1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(2, vo.getDate().getMonthValue());
        assertEquals(1, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_6_utf16_2() {
        String str = "\r\t\b\f {\"date\":\"2021-2-1\"}";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_16);
        LocalDate1 vo = JSON.parseObject(strBytes, 2, strBytes.length - 2, StandardCharsets.UTF_16, LocalDate1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(2, vo.getDate().getMonthValue());
        assertEquals(1, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_6_utf8() {
        String str = "\r\t\b\f {\"date\":\"2021-2-1\"}";
        LocalDate1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDate1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(2, vo.getDate().getMonthValue());
        assertEquals(1, vo.getDate().getDayOfMonth());
    }
}
