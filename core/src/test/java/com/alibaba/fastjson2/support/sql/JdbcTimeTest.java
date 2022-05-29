package com.alibaba.fastjson2.support.sql;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcTimeTest {
    private TimeZone defaultTimeZone;

    @BeforeEach
    public void before() {
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @AfterEach
    public void after() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    public void test_time() {
        A a = JSON.parseObject("{\"value\":\"12:13:14\"}", A.class);
        A a1 = JSON.parseObject("{\"value\":\"12:13:14\"}", A.class);
        assertEquals("12:13:14", a.value.toString());
        assertEquals("12:13:14", a1.value.toString());

        assertEquals("{\"value\":\"12:13:14\"}", JSON.toJSONString(a));
        assertEquals("{\"value\":null}", JSON.toJSONString(new A(), JSONWriter.Feature.WriteNulls));

        byte[] bytes = JSONB.toBytes(a);
        A a2 = JSONB.parseObject(bytes, A.class);
        assertEquals(a.value, a2.value);
    }

    @Test
    public void test_timestamp() {
        long millis = System.currentTimeMillis();

        B b = new B();
        b.value = new Timestamp(millis);
        String str = JSON.toJSONString(b);
        B b1 = JSON.parseObject(str, B.class);
        assertEquals(b.value, b1.value);

        String str1 = "{\"value\":" + millis + "}";
        B b2 = JSON.parseObject(str1, B.class);
        assertEquals(b.value, b2.value);
    }

    @Test
    public void test_timestamp_nano() {
        LocalDateTime now = LocalDateTime.now();
        Timestamp ts = Timestamp.valueOf(now);

        {
            byte[] bytes = JSONB.toBytes(ts);
            Timestamp ts_jsonb = JSONB.parseObject(bytes, Timestamp.class);
            assertEquals(ts, ts_jsonb);
        }

        B b = new B();
        b.value = ts;
        String str = JSON.toJSONString(b);
        B b1 = JSON.parseObject(str, B.class);
        assertEquals(b.value, b1.value);

        String str2 = JSON.toJSONString(ts);
        Timestamp ts1 = JSON.parseObject(str2, Timestamp.class);
        assertEquals(now, ts1.toLocalDateTime());
        assertEquals(now.getNano(), ts1.toLocalDateTime().getNano());

        String str3 = JSON.toJSONString(ts);
        Timestamp ts2 = JSON.parseObject(str3, Timestamp.class);
        assertEquals(now, ts2.toLocalDateTime());
        assertEquals(now.getNano(), ts2.toLocalDateTime().getNano());
    }

    @Test
    public void test_timestamp_nano_1() {
        LocalDateTime now = LocalDateTime.now();
        Timestamp ts = Timestamp.valueOf(now);
        ts.setNanos(10);

        {
            byte[] bytes = JSONB.toBytes(ts);
            Timestamp ts_jsonb = JSONB.parseObject(bytes, Timestamp.class);
            assertEquals(ts, ts_jsonb);
        }

        B b = new B();
        b.value = ts;
        String str = JSON.toJSONString(b);
        B b1 = JSON.parseObject(str, B.class);
        assertEquals(b.value, b1.value);

        String str2 = JSON.toJSONString(ts);
        Timestamp ts1 = JSON.parseObject(str2, Timestamp.class);
        assertEquals(ts, ts1);
        assertEquals(ts.getNanos(), ts1.toLocalDateTime().getNano());

        String str3 = JSON.toJSONString(ts);
        Timestamp ts2 = JSON.parseObject(str3, Timestamp.class);
        assertEquals(ts, ts2);
        assertEquals(ts.getNanos(), ts2.toLocalDateTime().getNano());

        C1 c1 = new C1();
        c1.value = ts;
        JSON.toJSONString(c1);

        JSON.toJSONString(b, "iso8601");
    }

    @Test
    public void test_timestamp_1() {
        String str = "{\"value\":\"2012-12-01\"}";
        C c = JSON.parseObject(str, C.class);
        assertEquals(2012, c.value.toLocalDateTime().getYear());
        assertEquals(12, c.value.toLocalDateTime().getMonthValue());
    }

    @Test
    public void test_date() {
        long millis = 1653665873000L;

        D d = new D();
        d.value = new java.sql.Date(millis);
        String str = JSON.toJSONString(d);
        D d1 = JSON.parseObject(str, D.class);
        assertEquals(d.value.toString(), d1.value.toString(), str);

        byte[] bytes = JSONB.toBytes(d);
        D d2 = JSONB.parseObject(bytes, D.class);
        assertEquals(d.value.toString(), d2.value.toString());
    }

    @Test
    public void test_date_1() {
        String str = "{\"value\":\"2012-12-01\"}";
        E e = JSON.parseObject(str, E.class);
        assertEquals(2012, e.value.toLocalDate().getYear());
        assertEquals(12, e.value.toLocalDate().getMonthValue());
    }

    public static class A {
        @JSONField(format = "HH:mm:ss")
        public Time value;
    }

    public static class B {
        public Timestamp value;
    }

    public static class C {
        @JSONField(format = "yyyy-MM-dd")
        public Timestamp value;
    }

    public static class C1 {
        @JSONField(format = "iso8601")
        public Timestamp value;
    }

    public static class D {
        public java.sql.Date value;
    }

    public static class E {
        @JSONField(format = "yyyy-MM-dd")
        public java.sql.Date value;
    }
}
