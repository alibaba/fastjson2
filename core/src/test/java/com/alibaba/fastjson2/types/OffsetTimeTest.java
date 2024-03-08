package com.alibaba.fastjson2.types;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class OffsetTimeTest {
    @Test
    public void test() {
        assertEquals(
                OffsetTime.of(LocalTime.of(12, 13, 14), ZoneOffset.UTC),
                JSON.parseObject("\"12:13:14Z\"", OffsetTime.class));
        assertNull(
                JSON.parseObject("null", OffsetTime.class));

        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneOffset.UTC);
        assertEquals(
                OffsetTime.of(LocalTime.of(0, 0, 1), ZoneOffset.UTC),
                JSON.parseObject("1000", OffsetTime.class, context));

        assertSame(
                OffsetTime.class,
                JSONFactory.getDefaultObjectReaderProvider()
                        .getObjectReader(OffsetTime.class)
                        .getObjectClass());
    }

    @Test
    public void test1() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneOffset.UTC);
        Bean1 bean = JSON.parseObject("{\"time\":1}", Bean1.class, context);
        assertEquals(
                OffsetTime.of(LocalTime.of(0, 0, 1), ZoneOffset.UTC),
                bean.time);
    }

    public static class Bean1 {
        private final OffsetTime time;

        public Bean1(@JSONField(format = "unixtime")OffsetTime time) {
            this.time = time;
        }
    }

    @Test
    public void test2() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneOffset.UTC);
        context.setDateFormat("unixtime");
        Bean2 bean = JSON.parseObject("{\"time\":1}", Bean2.class, context);
        assertEquals(
                OffsetTime.of(LocalTime.of(0, 0, 1), ZoneOffset.UTC),
                bean.time);
    }

    public static class Bean2 {
        private final OffsetTime time;

        public Bean2(OffsetTime time) {
            this.time = time;
        }
    }

    @Test
    public void test3() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneOffset.UTC);
        Bean3 bean = JSON.parseObject("{\"time\":\"1000\"}", Bean3.class, context);
        assertEquals(
                OffsetTime.of(LocalTime.of(0, 0, 1), ZoneOffset.UTC),
                bean.time);
    }

    public static class Bean3 {
        private final OffsetTime time;

        public Bean3(@JSONField(format = "millis")OffsetTime time) {
            this.time = time;
        }
    }

    @Test
    public void test4() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneOffset.UTC);
        Bean4 bean = JSON.parseObject("{\"time\":\"121314\"}", Bean4.class, context);
        assertEquals(
                OffsetTime.of(LocalTime.of(12, 13, 14), ZoneOffset.UTC),
                bean.time);
    }

    public static class Bean4 {
        private final OffsetTime time;

        public Bean4(@JSONField(format = "HHmmss")OffsetTime time) {
            this.time = time;
        }
    }

    @Test
    public void test5() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneOffset.UTC);
        Bean5 bean = JSON.parseObject("{\"time\":\"20010203121314\"}", Bean5.class, context);
        assertEquals(
                OffsetTime.of(LocalTime.of(12, 13, 14), ZoneOffset.UTC),
                bean.time);
    }

    public static class Bean5 {
        private final OffsetTime time;

        public Bean5(@JSONField(format = "yyyyMMddHHmmss")OffsetTime time) {
            this.time = time;
        }
    }

    @Test
    public void test6() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneOffset.UTC);
        Bean6 bean = JSON.parseObject("{\"time\":\"20010203\"}", Bean6.class, context);
        assertEquals(
                OffsetTime.of(LocalTime.of(0, 0, 0), ZoneOffset.UTC),
                bean.time);
    }

    public static class Bean6 {
        private final OffsetTime time;

        public Bean6(@JSONField(format = "yyyyMMdd")OffsetTime time) {
            this.time = time;
        }
    }
}
