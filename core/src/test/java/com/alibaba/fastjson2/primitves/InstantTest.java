package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2_vo.Instant1;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstantTest {
    static ZoneId zoneId = ZoneId.of("UTC");
    static ZonedDateTime[] dateTimes = new ZonedDateTime[]{
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 121_000_000, ZoneId.of("UTC")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 140__000_000, ZoneId.of("UTC+8")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 200__000_000, ZoneId.of("UTC-8")),
            ZonedDateTime.of(2021, 10, 20, 16, 22, 15, 0, ZoneId.of("Asia/Shanghai"))
    };

    @Test
    public void test_jsonb() {
        for (ZonedDateTime dateTime : dateTimes) {
            Instant1 vo = new Instant1();
            vo.setV0000(dateTime.toInstant());
            byte[] jsonbBytes = JSONB.toBytes(vo);

            Instant1 v1 = JSONB.parseObject(jsonbBytes, Instant1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_utf8() {
        for (ZonedDateTime dateTime : dateTimes) {
            Instant1 vo = new Instant1();
            vo.setV0000(dateTime.toInstant());
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Instant1 v1 = JSON.parseObject(utf8Bytes, Instant1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_str() {
        for (ZonedDateTime dateTime : dateTimes) {
            Instant1 vo = new Instant1();
            vo.setV0000(dateTime.toInstant());
            String str = JSON.toJSONString(vo);

            Instant1 v1 = JSON.parseObject(str, Instant1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_ascii() {
        for (ZonedDateTime dateTime : dateTimes) {
            Instant1 vo = new Instant1();
            vo.setV0000(dateTime.toInstant());
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Instant1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Instant1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

}
