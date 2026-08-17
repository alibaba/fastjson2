package com.alibaba.fastjson2.issues_3800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * #3845 BC date (negative year) deserialization error
 */
public class Issue3845 {
    @Test
    public void testNegativeYearRoundTrip() {
        // Year 0 minus 8 hours = year -1, Dec 31, 16:00:00
        LocalDateTime time = LocalDateTime.of(0, 1, 1, 0, 0, 0, 0).minusHours(8);
        JSONObject bean = new JSONObject().fluentPut("time", time);
        String s = bean.toJSONString();
        LocalDateTime parsed = JSON.parseObject(s).getLocalDateTime("time");
        assertNotNull(parsed);
        assertEquals(time, parsed);
    }

    @Test
    public void testNegativeYearDirectParse() {
        // Parse a negative year datetime string directly
        String json = "{\"time\":\"-0001-12-31 16:00:00\"}";
        LocalDateTime parsed = JSON.parseObject(json).getLocalDateTime("time");
        assertNotNull(parsed);
        assertEquals(-1, parsed.getYear());
        assertEquals(12, parsed.getMonthValue());
        assertEquals(31, parsed.getDayOfMonth());
        assertEquals(16, parsed.getHour());
        assertEquals(0, parsed.getMinute());
        assertEquals(0, parsed.getSecond());
    }

    @Test
    public void testNegativeYearWithT() {
        // Parse with T separator instead of space
        String json = "{\"time\":\"-0001-12-31T16:00:00\"}";
        LocalDateTime parsed = JSON.parseObject(json).getLocalDateTime("time");
        assertNotNull(parsed);
        assertEquals(-1, parsed.getYear());
        assertEquals(12, parsed.getMonthValue());
        assertEquals(31, parsed.getDayOfMonth());
    }

    @Test
    public void testNegativeYearWithSlash() {
        // Parse with / separator
        String json = "{\"time\":\"-0001/12/31 16:00:00\"}";
        LocalDateTime parsed = JSON.parseObject(json).getLocalDateTime("time");
        assertNotNull(parsed);
        assertEquals(-1, parsed.getYear());
    }

    @Test
    public void testNegativeYearLarger() {
        // Year -100 (101 BC)
        LocalDateTime time = LocalDateTime.of(-100, 6, 15, 12, 30, 45);
        JSONObject bean = new JSONObject().fluentPut("time", time);
        String s = bean.toJSONString();
        LocalDateTime parsed = JSON.parseObject(s).getLocalDateTime("time");
        assertNotNull(parsed);
        assertEquals(time, parsed);
    }

    @Test
    public void testPositiveYearStillWorks() {
        // Ensure normal dates are not affected
        LocalDateTime time = LocalDateTime.of(2026, 3, 25, 10, 30, 0);
        JSONObject bean = new JSONObject().fluentPut("time", time);
        String s = bean.toJSONString();
        LocalDateTime parsed = JSON.parseObject(s).getLocalDateTime("time");
        assertNotNull(parsed);
        assertEquals(time, parsed);
    }
}
