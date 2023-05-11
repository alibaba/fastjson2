package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.time.chrono.HijrahDate;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistDate;
import java.time.temporal.Temporal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1446 {


    @Test
    public void test() {


        testTemporal(LocalDate.now(), LocalDateTime.now(),
                        LocalTime.now(), ZonedDateTime.now(),
                        OffsetDateTime.now(),Instant.now(),
                        HijrahDate.now(), JapaneseDate.now(),
                        OffsetTime.now(), MinguoDate.now(),
                        ThaiBuddhistDate.now(),YearMonth.now());


    }


    void testTemporal(Temporal... targets){

        JSONObject jsonObject = new JSONObject();

        for (Temporal temporal : targets) {

            jsonObject.put("data",temporal);

            assertEquals(temporal.toString(), jsonObject.getString("data"));
        }
    }

}
