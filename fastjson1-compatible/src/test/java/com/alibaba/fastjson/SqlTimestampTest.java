package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SqlTimestampTest {
    @BeforeEach
    public void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getDefault();
        JSON.defaultLocale = Locale.getDefault();
    }

    @Test
    public void test_date() throws Exception {
        Timestamp ts = new Timestamp(
                97,
                2,
                17,
                15,
                53,
                01,
                12345678
        );

        assertEquals("1997-03-17 15:53:01.012345678", ts.toString());

        String json = JSON.toJSONString(ts, SerializerFeature.UseISO8601DateFormat);
        Timestamp ts2 = JSON.parseObject(json, Timestamp.class);
        assertEquals('"' + ts.toString() + '"', '"' + ts2.toString() + '"');
    }

    @Test
    public void test_date_1() throws Exception {
        // 2020-04-11 03:10:19.516
        Timestamp ts = new Timestamp(
                97,
                2,
                17,
                15,
                53,
                01,
                516000000
        );

        String json = JSON.toJSONString(ts, SerializerFeature.UseISO8601DateFormat);
        Timestamp ts2 = JSON.parseObject(json, Timestamp.class);
        String json2 = JSON.toJSONString(ts2, SerializerFeature.UseISO8601DateFormat);
        assertEquals('"' + ts.toString() + '"', '"' + ts2.toString() + '"');
    }

    // 1997-03-17 15:53:01.01
    @Test
    public void test_date_2() throws Exception {
        // 2020-04-11 03:10:19.516
        Timestamp ts = new Timestamp(
                97,
                3,
                17,
                15,
                53,
                01,
                10000000
        );

        String json = JSON.toJSONString(ts, SerializerFeature.UseISO8601DateFormat);
        Timestamp ts2 = JSON.parseObject(json, Timestamp.class);
        String json2 = JSON.toJSONString(ts2, SerializerFeature.UseISO8601DateFormat);
        assertEquals('"' + ts.toString() + '"', '"' + ts2.toString() + '"');
    }

    @Test
    public void test_date_999999999() throws Exception {
        // 2020-04-11 03:10:19.516
        Timestamp ts = new Timestamp(
                97,
                2,
                17,
                15,
                53,
                01,
                999999999
        );

        String json = JSON.toJSONString(ts, SerializerFeature.UseISO8601DateFormat);
        Timestamp ts2 = JSON.parseObject(json, Timestamp.class);
        assertEquals('"' + ts.toString() + '"', '"' + ts2.toString() + '"');
    }

    @Test
    public void test_date_x1() throws Exception {
        // 2020-04-11 03:10:19.516
        Timestamp ts = new Timestamp(
                97,
                2,
                17,
                15,
                53,
                01,
                5
        );

        String json = JSON.toJSONString(ts, SerializerFeature.UseISO8601DateFormat);
        Timestamp ts2 = JSON.parseObject(json, Timestamp.class);
        assertEquals('"' + ts.toString() + '"', '"' + ts2.toString() + '"');
    }

    @Test
    public void test_date_x2() throws Exception {
        // 2020-04-11 03:10:19.516
        Timestamp ts = new Timestamp(
                97,
                2,
                17,
                15,
                53,
                01,
                50
        );

        String json = JSON.toJSONString(ts, SerializerFeature.UseISO8601DateFormat);
        Timestamp ts2 = JSON.parseObject(json, Timestamp.class);
        assertEquals('"' + ts.toString() + '"', '"' + ts2.toString() + '"');
    }

    @Test
    public void test_date_x() throws Exception {
        // 2020-04-11 03:10:19.516
        int nanos = 1;
        for (int i = 0; i < 8; i++) {
            nanos = nanos * 10;
            Timestamp ts = new Timestamp(
                    97,
                    2,
                    17,
                    15,
                    53,
                    01,
                    nanos
            );

            String json = JSON.toJSONString(ts, SerializerFeature.UseISO8601DateFormat);
            Timestamp ts2 = JSON.parseObject(json, Timestamp.class);
            assertEquals('"' + ts.toString() + '"', '"' + ts2.toString() + '"');
        }
    }

    @Test
    public void test_date_xx() throws Exception {
        // 2020-04-11 03:10:19.516
        int nanos = 0;
        for (int i = 0; i < 9; i++) {
            nanos = nanos * 10 + (i + 1);
            Timestamp ts = new Timestamp(
                    97,
                    2,
                    17,
                    15,
                    53,
                    01,
                    nanos
            );

            String json = JSON.toJSONString(ts, SerializerFeature.UseISO8601DateFormat);
            Timestamp ts2 = JSON.parseObject(json, Timestamp.class);
            assertEquals('"' + ts.toString() + '"', '"' + ts2.toString() + '"');
        }
    }
}
