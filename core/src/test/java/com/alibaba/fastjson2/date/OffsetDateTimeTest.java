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
        OffsetDateTime odt1 = JSON.parseObject(str, OffsetDateTime.class);
        assertEquals(odt, odt1);

        OffsetDateTime odt2 = JSONB.parseObject(JSONB.toBytes(odt), OffsetDateTime.class);
        assertEquals(odt, odt2);
    }

    @Test
    public void test1() {
        OffsetDateTime odt = OffsetDateTime.of(2022, 5, 3, 15, 26, 5, 0, ZoneOffset.UTC);
        assertEquals("\"2022-05-03T15:26:05Z\"", JSON.toJSONString(odt));
        assertEquals("\"2022-05-03T15:26:05Z\"", new String(JSON.toJSONBytes(odt)));
    }
}
