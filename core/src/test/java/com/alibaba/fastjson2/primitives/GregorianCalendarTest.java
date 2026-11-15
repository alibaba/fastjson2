package com.alibaba.fastjson2.primitives;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@Tag("primitives")
public class GregorianCalendarTest {
    private static final long MILLIS = 1717243200123L; // 2024-06-01T12:00:00.123Z

    @Test
    public void test() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(MILLIS);

        // the object writer handles any Calendar subclass
        String json = JSON.toJSONString(calendar);

        // ... so the object reader must too, not only the exact Calendar class
        Calendar calendarParsed = JSON.parseObject(json, Calendar.class);
        assertEquals(MILLIS, calendarParsed.getTimeInMillis());

        GregorianCalendar parsed = JSON.parseObject(json, GregorianCalendar.class);
        assertInstanceOf(GregorianCalendar.class, parsed);
        assertEquals(MILLIS, parsed.getTimeInMillis());

        GregorianCalendar parsedJSONB = JSONB.parseObject(JSONB.toBytes(calendar), GregorianCalendar.class);
        assertInstanceOf(GregorianCalendar.class, parsedJSONB);
        assertEquals(MILLIS, parsedJSONB.getTimeInMillis());

        Bean bean = new Bean();
        bean.calendar = calendar;
        Bean beanParsed = JSON.parseObject(JSON.toJSONString(bean), Bean.class);
        assertInstanceOf(GregorianCalendar.class, beanParsed.calendar);
        assertEquals(MILLIS, beanParsed.calendar.getTimeInMillis());
    }

    public static class Bean {
        public GregorianCalendar calendar;
    }
}
