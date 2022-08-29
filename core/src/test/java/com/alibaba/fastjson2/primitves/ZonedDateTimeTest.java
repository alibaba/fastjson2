package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2_vo.ZonedDateTime1;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZonedDateTimeTest {
    static ZoneId zoneId = ZoneOffset.UTC;
    static ZonedDateTime[] dateTimes = new ZonedDateTime[]{
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 0, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 10, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 100, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_0, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_00, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_000, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_000_0, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_000_00, ZoneOffset.UTC),

            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 9, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 99, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999_9, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999_99, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999_999, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999_999_9, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999_999_99, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999_999_999, ZoneOffset.UTC),

            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 0, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 10, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 100, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_0, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_00, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_000, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_000_0, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_000_00, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 9, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 99, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999_9, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999_99, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999_999, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999_999_9, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999_999_99, ZoneId.of("Asia/Shanghai")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 999_999_999, ZoneId.of("Asia/Shanghai")),

            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 121_000_000, ZoneOffset.UTC),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 140__000_000, ZoneId.of("UTC+8")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 200__000_000, ZoneId.of("UTC-8")),

            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 0, ZoneId.of("-07:00")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1, ZoneId.of("-07:00")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 10, ZoneId.of("-07:00")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 100, ZoneId.of("-07:00")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000, ZoneId.of("-07:00")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_0, ZoneId.of("-07:00")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_00, ZoneId.of("-07:00")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_000, ZoneId.of("-07:00")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_000_0, ZoneId.of("-07:00")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 1000_000_00, ZoneId.of("-07:00"))
    };

    @Test
    public void test_jsonb() {
        for (ZonedDateTime dateTime : dateTimes) {
            ZonedDateTime1 vo = new ZonedDateTime1();
            vo.setV0000(dateTime);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            ZonedDateTime1 v1 = JSONB.parseObject(jsonbBytes, ZonedDateTime1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_utf8() {
        for (ZonedDateTime dateTime : dateTimes) {
            ZonedDateTime1 vo = new ZonedDateTime1();
            vo.setV0000(dateTime);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            ZonedDateTime1 v1 = JSON.parseObject(utf8Bytes, ZonedDateTime1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_str() {
        for (int i = 0; i < dateTimes.length; i++) {
            ZonedDateTime dateTime = dateTimes[i];

            ZonedDateTime1 vo = new ZonedDateTime1();
            vo.setV0000(dateTime);
            String str = JSON.toJSONString(vo);

            ZonedDateTime1 v1 = JSON.parseObject(str, ZonedDateTime1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_ascii() {
        for (ZonedDateTime dateTime : dateTimes) {
            ZonedDateTime1 vo = new ZonedDateTime1();
            vo.setV0000(dateTime);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            ZonedDateTime1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, ZonedDateTime1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }
}
