package com.alibaba.fastjson2.time;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2_vo.Date1;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTest {
    @Test
    public void testDate() throws Exception {
        JSONWriter jw = JSONWriter.of();
        jw.getContext().setZoneId(ZoneId.of("UTC+0"));

        Date1 date = new Date1();
        date.setDate(new java.util.Date(0));
        jw.writeAny(date);
        assertEquals("{\"date\":\"1970-01-01 00:00:00\"}", jw.toString());
    }

    @Test
    public void testDateNull() {
        Date[] dates = new Date[]{null};
        assertEquals("[null]", JSON.toJSONString(dates));
    }

    @Test
    public void testDateJSONB() throws Exception {
        Date1 date = new Date1();
        date.setDate(new java.util.Date(1642003200000L));

        byte[] bytes = JSONB.toBytes(date);
        JSONB.dump(bytes);

        Date1 bean2 = JSONB.parseObject(bytes, Date1.class);
        assertEquals(date.getDate(), bean2.getDate());
    }

    @Test
    public void testCalendar() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);
        JSONWriter jw = JSONWriter.of();
        jw.getContext().setZoneId(ZoneId.of("UTC+0"));

        jw.writeAny(calendar);
        assertEquals("\"1970-01-01 00:00:00\"", jw.toString());
    }

    @Test
    public void testZoneId() throws Exception {
        ZoneId zoneId = ZoneId.DEFAULT_ZONE_ID;
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(zoneId);
        assertEquals("\"" + zoneId + "\"", jw.toString());
    }

    @Test
    public void testZoneTimeZone() throws Exception {
        TimeZone timeZone = TimeZone.getDefault();
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(timeZone);
        assertEquals("\"" + timeZone.getID() + "\"", jw.toString());
    }
}
