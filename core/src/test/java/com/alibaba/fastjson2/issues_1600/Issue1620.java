package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1620 {
    @Test
    public void test() {
        LocalDate localDate = LocalDate.of(2023, 7, 2);
        String str = "{\"value\":\"2023-07-02T16:00:00.000Z\"}";
        assertEquals(localDate, JSON.parseObject(str, Bean.class).value);
        assertEquals(localDate, JSON.parseObject(str.getBytes(), Bean.class).value);
        assertEquals(localDate, JSON.parseObject(str.toCharArray(), Bean.class).value);

        assertEquals(localDate, JSONB.parseObject(JSONObject.parse(str).toJSONBBytes(), Bean.class).value);

        LocalDateTime ldt = LocalDateTime.of(localDate, LocalTime.of(16, 0, 0));
        ZonedDateTime zdt = ZonedDateTime.of(ldt, DateUtils.OFFSET_8_ZONE_ID);
        Instant instant = zdt.toInstant();

        assertEquals(
                localDate,
                JSONB.parseObject(
                        JSONObject.of("value", ldt).toJSONBBytes(),
                        Bean.class).value
        );
        assertEquals(
                localDate,
                JSONB.parseObject(
                        JSONObject.of("value", zdt).toJSONBBytes(),
                        Bean.class).value
        );

        assertEquals(
                ldt,
                JSONB.parseObject(
                        JSONObject.of("value", zdt).toJSONBBytes(),
                        Bean1.class).value
        );

        assertEquals(
                ZonedDateTime.of(LocalDateTime.of(localDate, LocalTime.MIN), DateUtils.DEFAULT_ZONE_ID),
                JSONB.parseObject(
                        JSONObject.of("value", localDate).toJSONBBytes(),
                        Bean2.class).value
        );
        assertEquals(
                ZonedDateTime.of(ldt, DateUtils.DEFAULT_ZONE_ID),
                JSONB.parseObject(
                        JSONObject.of("value", ldt).toJSONBBytes(),
                        Bean2.class).value
        );
        assertEquals(
                zdt.toInstant(),
                JSONB.parseObject(
                        JSONObject.of("value", instant).toJSONBBytes(),
                        Bean2.class).value.toInstant()
        );
    }

    public static class Bean {
        public LocalDate value;
    }

    public static class Bean1 {
        public LocalDateTime value;
    }

    public static class Bean2 {
        public ZonedDateTime value;
    }

    public static class Bean3 {
        public Instant value;
    }
}
