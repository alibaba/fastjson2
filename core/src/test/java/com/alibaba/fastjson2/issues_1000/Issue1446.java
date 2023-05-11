package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.time.chrono.HijrahDate;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistDate;
import java.time.temporal.TemporalAccessor;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1446 {


    @Test
    public void test() {

        testTemporal(LocalDate.now(), LocalDateTime.now(),
                        LocalTime.now(), ZonedDateTime.now(),
                        OffsetDateTime.now(),Instant.now(),
                        HijrahDate.now(), JapaneseDate.now(),
                        OffsetTime.now(), MinguoDate.now(),
                        ThaiBuddhistDate.now(),YearMonth.now(),
                        MonthDay.now(),DayOfWeek.of(3));


    }


    void testTemporal(TemporalAccessor... targets){

        com.alibaba.fastjson.JSONObject j1 = new com.alibaba.fastjson.JSONObject();
        JSONObject j2 = new JSONObject();

        for (TemporalAccessor target : targets) {

            j1.put("data",target);
            j2.put("data",target);

            assertEquals(j1.getString("data"), j2.getString("data"));
        }
    }

}
