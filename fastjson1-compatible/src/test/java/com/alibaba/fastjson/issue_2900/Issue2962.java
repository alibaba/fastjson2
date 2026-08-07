package com.alibaba.fastjson.issue_2900;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.*;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2962 {
    private TimeZone original;

    @BeforeEach
    public void setUp() {
        original = TimeZone.getDefault();
    }

    @AfterEach
    public void tearDown() {
        TimeZone.setDefault(original);
        JSON.defaultTimeZone = original;
    }

    @Test
    public void test_dates_different_timeZones() {
        String[] availableIDs = TimeZone.getAvailableIDs();
        for (int i = 0; i < availableIDs.length; i++) {
            String id = availableIDs[i];

            TimeZone timeZone = TimeZone.getTimeZone(id);
            TimeZone.setDefault(timeZone);
            JSON.defaultTimeZone = timeZone;

            Calendar cal = Calendar.getInstance();
            Date now = cal.getTime();

            VO vo = new VO();
            vo.date = now;

            String json = JSON.toJSONString(vo);
            VO result = JSON.parseObject(json, VO.class);
            assertEquals(vo.date, result.date);

            // with iso-format
            json = JSON.toJSONString(vo, SerializerFeature.UseISO8601DateFormat);
//            System.out.println(i + "\t" + id + " " + json);
            result = JSON.parseObject(json, VO.class);
            assertEquals(vo.date.getTime(), result.date.getTime(), "timeZone " + id);
            assertEquals(JSON.toJSONString(vo.date), JSON.toJSONString(result.date), "timeZone " + id);
        }
    }

    public static class VO {
        public Date date;
    }
}
