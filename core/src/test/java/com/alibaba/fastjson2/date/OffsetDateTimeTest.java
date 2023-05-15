package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OffsetDateTimeTest {
    @Test
    public void test() {
        OffsetDateTime odt = OffsetDateTime.now();
        String str = JSON.toJSONString(odt);
        OffsetDateTime odt1 = JSON.parseObject(str.getBytes(), OffsetDateTime.class);
        assertEquals(odt, odt1);

        OffsetDateTime odt2 = JSON.parseObject(str.toCharArray(), OffsetDateTime.class);
        assertEquals(odt, odt2);

        OffsetDateTime odt3 = JSONB.parseObject(JSONB.toBytes(odt), OffsetDateTime.class);
        assertEquals(odt, odt3);
    }

    @Test
    public void test_s0() {
        OffsetDateTime odt = OffsetDateTime.of(2022, 5, 3, 15, 26, 5, 0, ZoneOffset.UTC);
        String str = JSON.toJSONString(odt);

        assertEquals("\"2022-05-03T15:26:05Z\"", str);
        assertEquals("\"2022-05-03T15:26:05Z\"", new String(JSON.toJSONBytes(odt)));

        OffsetDateTime odt1 = JSON.parseObject(str.getBytes(), OffsetDateTime.class);
        assertEquals(odt, odt1);

        OffsetDateTime odt2 = JSON.parseObject(str.toCharArray(), OffsetDateTime.class);
        assertEquals(odt, odt2);
    }

    @Test
    public void test_s1() {
        OffsetDateTime odt = OffsetDateTime.of(2022, 5, 3, 15, 26, 5, 100_000_000, ZoneOffset.UTC);
        String str = "\"2022-05-03T15:26:05.1Z\"";

        OffsetDateTime odt1 = JSON.parseObject(str.getBytes(), OffsetDateTime.class);
        assertEquals(odt, odt1);

        OffsetDateTime odt2 = JSON.parseObject(str.toCharArray(), OffsetDateTime.class);
        assertEquals(odt, odt2);
    }

    @Test
    public void test_s2() {
        OffsetDateTime odt = OffsetDateTime.of(2022, 5, 3, 15, 26, 5, 120_000_000, ZoneOffset.UTC);
        String str = "\"2022-05-03T15:26:05.12Z\"";

        OffsetDateTime odt1 = JSON.parseObject(str.getBytes(), OffsetDateTime.class);
        assertEquals(odt, odt1);

        OffsetDateTime odt2 = JSON.parseObject(str.toCharArray(), OffsetDateTime.class);
        assertEquals(odt, odt2);
    }

    @Test
    public void test_s3() {
        OffsetDateTime odt = OffsetDateTime.of(2022, 5, 3, 15, 26, 5, 100_000_000, ZoneOffset.UTC);
        String str = JSON.toJSONString(odt);

        assertEquals("\"2022-05-03T15:26:05.100Z\"", str);
        assertEquals("\"2022-05-03T15:26:05.100Z\"", new String(JSON.toJSONBytes(odt)));

        OffsetDateTime odt1 = JSON.parseObject(str.getBytes(), OffsetDateTime.class);
        assertEquals(odt, odt1);

        OffsetDateTime odt2 = JSON.parseObject(str.toCharArray(), OffsetDateTime.class);
        assertEquals(odt, odt2);
    }

    @Test
    public void test_s4() {
        OffsetDateTime odt = OffsetDateTime.of(2022, 5, 3, 15, 26, 5, 100_000, ZoneOffset.UTC);
        String str = "\"2022-05-03T15:26:05.0001Z\"";

        OffsetDateTime odt1 = JSON.parseObject(str.getBytes(), OffsetDateTime.class);
        assertEquals(odt, odt1);

        OffsetDateTime odt2 = JSON.parseObject(str.toCharArray(), OffsetDateTime.class);
        assertEquals(odt, odt2);
    }

    @Test
    public void test_s5() {
        OffsetDateTime odt = OffsetDateTime.of(2022, 5, 3, 15, 26, 5, 120_000, ZoneOffset.UTC);
        String str = "\"2022-05-03T15:26:05.00012Z\"";

        OffsetDateTime odt1 = JSON.parseObject(str.getBytes(), OffsetDateTime.class);
        assertEquals(odt, odt1);

        OffsetDateTime odt2 = JSON.parseObject(str.toCharArray(), OffsetDateTime.class);
        assertEquals(odt, odt2);
    }

    @Test
    public void test_s6() {
        OffsetDateTime odt = OffsetDateTime.of(2022, 5, 3, 15, 26, 5, 100_000, ZoneOffset.UTC);
        String str = JSON.toJSONString(odt);

        assertEquals("\"2022-05-03T15:26:05.000100Z\"", str);
        assertEquals("\"2022-05-03T15:26:05.000100Z\"", new String(JSON.toJSONBytes(odt)));

        OffsetDateTime odt1 = JSON.parseObject(str.getBytes(), OffsetDateTime.class);
        assertEquals(odt, odt1);

        OffsetDateTime odt2 = JSON.parseObject(str.toCharArray(), OffsetDateTime.class);
        assertEquals(odt, odt2);
    }

    @Test
    public void test_s7() {
        OffsetDateTime odt = OffsetDateTime.of(2022, 5, 3, 15, 26, 5, 100, ZoneOffset.UTC);
        String str = "\"2022-05-03T15:26:05.0000001Z\"";

        OffsetDateTime odt1 = JSON.parseObject(str.getBytes(), OffsetDateTime.class);
        assertEquals(odt, odt1);

        OffsetDateTime odt2 = JSON.parseObject(str.toCharArray(), OffsetDateTime.class);
        assertEquals(odt, odt2);
    }

    @Test
    public void test_s8() {
        OffsetDateTime odt = OffsetDateTime.of(2022, 5, 3, 15, 26, 5, 120, ZoneOffset.UTC);
        String str = "\"2022-05-03T15:26:05.00000012Z\"";

        OffsetDateTime odt1 = JSON.parseObject(str.getBytes(), OffsetDateTime.class);
        assertEquals(odt, odt1);

        OffsetDateTime odt2 = JSON.parseObject(str.toCharArray(), OffsetDateTime.class);
        assertEquals(odt, odt2);
    }

    @Test
    public void test_s9() {
        OffsetDateTime odt = OffsetDateTime.of(2022, 5, 3, 15, 26, 5, 100, ZoneOffset.UTC);
        String str = JSON.toJSONString(odt);

        assertEquals("\"2022-05-03T15:26:05.000000100Z\"", str);
        assertEquals("\"2022-05-03T15:26:05.000000100Z\"", new String(JSON.toJSONBytes(odt)));

        OffsetDateTime odt1 = JSON.parseObject(str.getBytes(), OffsetDateTime.class);
        assertEquals(odt, odt1);

        OffsetDateTime odt2 = JSON.parseObject(str.toCharArray(), OffsetDateTime.class);
        assertEquals(odt, odt2);
    }
}
