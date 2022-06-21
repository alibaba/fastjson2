package com.alibaba.fastjson.date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSONFactory;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CalendarTest {
    @Test
    public void test_null() {
        String text = "{\"calendar\":null}";

        VO vo = JSON.parseObject(text, VO.class);
        assertNull(vo.getCalendar());
    }

    @Test
    public void test_codec() {
        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        VO vo = new VO();
        vo.setCalendar(calendar);
        String text = JSON.toJSONString(vo);

        VO vo2 = JSON.parseObject(text, VO.class);
        assertEquals(vo.getCalendar().getTimeInMillis(), vo2.getCalendar().getTimeInMillis());
    }

    @Test
    public void test_codec_iso88591() {
        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        VO vo = new VO();
        vo.setCalendar(calendar);
        String text = JSON.toJSONString(vo, SerializerFeature.UseISO8601DateFormat);
        System.out.println(text);
        VO vo2 = JSON.parseObject(text, VO.class);
        assertEquals(vo.getCalendar().getTimeInMillis(), vo2.getCalendar().getTimeInMillis());
    }

    @Test
    public void test_codec_iso88591_2() {
        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        VO vo = new VO();
        vo.setCalendar(calendar);
        String text = JSON.toJSONString(vo, SerializerFeature.UseISO8601DateFormat);
        System.out.println(text);

        VO vo2 = JSON.parseObject(text, VO.class);
        assertEquals(vo.getCalendar().getTimeInMillis(), vo2.getCalendar().getTimeInMillis());
    }

    @Test
    public void getObjectClass() {
        assertEquals(
                Calendar.class,
                JSONFactory
                        .getDefaultObjectReaderProvider()
                        .getObjectReader(Calendar.class)
                        .getObjectClass()
        );
    }

    public static class VO {
        private Calendar calendar;

        public Calendar getCalendar() {
            return calendar;
        }

        public void setCalendar(Calendar calendar) {
            this.calendar = calendar;
        }
    }
}
