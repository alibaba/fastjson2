package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1605 {
    @Test
    public void test() {
        String s = "{\"dateTime\":\"2023-06-27T16:53:23.830347521Z\"}";
        Bean bean = JSON.parseObject(s, Bean.class);
        String s1 = JSON.toJSONString(bean);
        assertEquals("{\"dateTime\":\"2023-06-27T16:53:23.830Z\"}", s1);
    }

    public static class Bean {
        public DateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(DateTime dateTime) {
            this.dateTime = dateTime;
        }

        public DateTime dateTime;
    }

    public DateTime build(ZonedDateTime zdt) {
        return new DateTime(
                zdt.getYear(),
                zdt.getMonthValue(),
                zdt.getDayOfMonth(),

                zdt.getHour(),
                zdt.getMinute(),
                zdt.getSecond(),
                zdt.getNano() / 1_000_000,
                DateTimeZone.forID(zdt.getZone().getId())
        );
    }

    public ZonedDateTime build1(DateTime dt) {
        return ZonedDateTime.of(
                dt.getYear(),
                dt.getMonthOfYear(),
                dt.getDayOfMonth(),
                dt.getHourOfDay(),
                dt.getMinuteOfHour(),
                dt.getSecondOfMinute(),
                dt.getMillisOfSecond(),
                ZoneId.of(dt.getZone().getID())
        );
    }
}
