package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OffsetTimeTest {
    @Test
    public void test() {
        OffsetTime odt = OffsetTime.now();
        String str = JSON.toJSONString(odt);
        OffsetTime odt1 = JSON.parseObject(str.getBytes(), OffsetTime.class);
        assertEquals(odt, odt1);

        OffsetTime odt2 = JSON.parseObject(str.toCharArray(), OffsetTime.class);
        assertEquals(odt, odt2);

        OffsetTime odt3 = JSONB.parseObject(JSONB.toBytes(odt), OffsetTime.class);
        assertEquals(odt, odt3);
    }

    @Test
    public void test1() {
        LocalTime localTime = LocalTime.now();
        OffsetTime odt = OffsetTime.of(localTime, ZoneOffset.ofHours(0));
        String str = JSON.toJSONString(odt);
        OffsetTime odt1 = JSON.parseObject(str.getBytes(), OffsetTime.class);
        assertEquals(odt, odt1);

        OffsetTime odt2 = JSON.parseObject(str.toCharArray(), OffsetTime.class);
        assertEquals(odt, odt2);

        OffsetTime odt3 = JSONB.parseObject(JSONB.toBytes(odt), OffsetTime.class);
        assertEquals(odt, odt3);
    }

    @Test
    public void test2() {
        LocalTime localTime = LocalTime.of(12, 13, 14);
        OffsetTime odt = OffsetTime.of(localTime, ZoneOffset.ofHours(0));
        String str = JSON.toJSONString(odt);
        OffsetTime odt1 = JSON.parseObject(str.getBytes(), OffsetTime.class);
        assertEquals(odt, odt1);

        OffsetTime odt2 = JSON.parseObject(str.toCharArray(), OffsetTime.class);
        assertEquals(odt, odt2);

        OffsetTime odt3 = JSONB.parseObject(JSONB.toBytes(odt), OffsetTime.class);
        assertEquals(odt, odt3);
    }

    @Test
    public void test3() {
        LocalTime localTime = LocalTime.of(12, 13, 14);
        OffsetTime odt = OffsetTime.of(localTime, ZoneOffset.ofHours(8));
        String str = JSON.toJSONString(odt);
        OffsetTime odt1 = JSON.parseObject(str.getBytes(), OffsetTime.class);
        assertEquals(odt, odt1);

        OffsetTime odt2 = JSON.parseObject(str.toCharArray(), OffsetTime.class);
        assertEquals(odt, odt2);

        OffsetTime odt3 = JSONB.parseObject(JSONB.toBytes(odt), OffsetTime.class);
        assertEquals(odt, odt3);
    }

    @Test
    public void test4() {
        LocalTime localTime = LocalTime.of(12, 13, 14);
        OffsetTime odt = OffsetTime.of(localTime, ZoneOffset.ofHours(-8));
        String str = JSON.toJSONString(odt);
        OffsetTime odt1 = JSON.parseObject(str.getBytes(), OffsetTime.class);
        assertEquals(odt, odt1);

        OffsetTime odt2 = JSON.parseObject(str.toCharArray(), OffsetTime.class);
        assertEquals(odt, odt2);

        OffsetTime odt3 = JSONB.parseObject(JSONB.toBytes(odt), OffsetTime.class);
        assertEquals(odt, odt3);
    }

    @Test
    public void test5() {
        LocalTime localTime = LocalTime.of(12, 13, 14);
        OffsetTime odt = OffsetTime.of(localTime, ZoneOffset.ofHours(0));
        String str = "\"12:13:14\"";
        OffsetTime odt1 = JSON.parseObject(str.getBytes(), OffsetTime.class);
        assertEquals(odt, odt1);

        OffsetTime odt2 = JSON.parseObject(str.toCharArray(), OffsetTime.class);
        assertEquals(odt, odt2);

        OffsetTime odt3 = JSONB.parseObject(JSONB.toBytes(odt), OffsetTime.class);
        assertEquals(odt, odt3);
    }

    @Test
    public void test6() {
        LocalTime localTime = LocalTime.of(12, 13, 14);
        OffsetTime odt = OffsetTime.of(localTime, ZoneOffset.ofHours(0));
        String str = "\"12:13:14Z\"";
        OffsetTime odt1 = JSON.parseObject(str.getBytes(), OffsetTime.class);
        assertEquals(odt, odt1);

        OffsetTime odt2 = JSON.parseObject(str.toCharArray(), OffsetTime.class);
        assertEquals(odt, odt2);

        OffsetTime odt3 = JSONB.parseObject(JSONB.toBytes(odt), OffsetTime.class);
        assertEquals(odt, odt3);
    }

    @Test
    public void test7() {
        LocalTime localTime = LocalTime.of(0, 28, 3, 319930000);
        OffsetTime odt = OffsetTime.of(localTime, ZoneOffset.ofHours(8));
        String str = "\"00:28:03.319930+08:00\"";
        OffsetTime odt1 = JSON.parseObject(str.getBytes(), OffsetTime.class);
        assertEquals(odt, odt1);

        OffsetTime odt2 = JSON.parseObject(str.toCharArray(), OffsetTime.class);
        assertEquals(odt, odt2);

        OffsetTime odt3 = JSONB.parseObject(JSONB.toBytes(odt), OffsetTime.class);
        assertEquals(odt, odt3);
    }

    @Test
    public void test8() {
        LocalTime localTime = LocalTime.of(0, 28, 3, 319930123);
        OffsetTime odt = OffsetTime.of(localTime, ZoneOffset.ofHours(8));
        String str = "\"00:28:03.319930123+08:00\"";
        OffsetTime odt1 = JSON.parseObject(str.getBytes(), OffsetTime.class);
        assertEquals(odt, odt1);

        OffsetTime odt2 = JSON.parseObject(str.toCharArray(), OffsetTime.class);
        assertEquals(odt, odt2);

        OffsetTime odt3 = JSONB.parseObject(JSONB.toBytes(odt), OffsetTime.class);
        assertEquals(odt, odt3);
    }
}
