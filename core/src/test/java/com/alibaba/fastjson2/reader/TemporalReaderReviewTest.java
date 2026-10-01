package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.MonthDay;
import java.time.OffsetTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TemporalReaderReviewTest {
    @Test
    public void numericMonthDayRejectsOverflowBeforeNarrowing() {
        long value = (1L << 32) * 100 + 101;
        assertThrows(JSONException.class, () -> JSON.parseObject(Long.toString(value), MonthDay.class));
        assertThrows(JSONException.class, () -> JSONB.parseObject(JSONB.toBytes(value), MonthDay.class));
        assertEquals(MonthDay.of(12, 31), JSON.parseObject("1231", MonthDay.class));
        assertEquals(MonthDay.of(1, 1), JSONB.parseObject(JSONB.toBytes(101), MonthDay.class));
    }

    @Test
    public void offsetTimeSupportsIso8601Format() {
        OffsetTime expected = OffsetTime.parse("12:34:56+05:30");
        OffsetBean bean = JSON.parseObject("{\"time\":\"12:34:56+05:30\"}", OffsetBean.class);
        assertEquals(expected, bean.time);
    }

    public static class OffsetBean {
        @JSONField(format = "iso8601")
        public OffsetTime time;
    }
}
