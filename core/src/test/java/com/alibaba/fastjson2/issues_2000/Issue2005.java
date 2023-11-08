package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2005 {
    @Test
    public void test() {
        String dateTime = "2023-11-08 00:00";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date now = null;
        try {
            now = sdf.parse(dateTime);
        } catch (ParseException e) {
            // can not happen
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        Instant instant = now.toInstant();

        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

        OffsetDateTime offsetDateTime = instant.atOffset(OffsetDateTime.now().getOffset());

        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        LocalDate localDate = localDateTime.toLocalDate();

        Item item = new Item("ssss",
                now,
                instant,
                calendar,
                localDate,
                localDateTime,
                zonedDateTime,
                offsetDateTime);

        String json = JSON.toJSONString(item);
        JSONObject jsonObject = JSON.parseObject(json);
        assertEquals(dateTime, jsonObject.get("date"));
        assertEquals(dateTime, jsonObject.get("instant"));
        assertEquals(dateTime, jsonObject.get("calendar"));
        assertEquals(dateTime, jsonObject.get("localDate"));
        assertEquals(dateTime, jsonObject.get("localDateTime"));
        assertEquals(dateTime, jsonObject.get("zonedDateTime"));
        assertEquals(dateTime, jsonObject.get("offsetDateTime"));
    }

    @Data
    @AllArgsConstructor
    public static class Item {
        private String jobNumber;
        @JSONField(format = "yyyy-MM-dd HH:mm")
        private Date date;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private Instant instant;
        @JSONField(format = "yyyy-MM-dd HH:mm")
        private Calendar calendar;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDate localDate;
        @JSONField(format = "yyyy-MM-dd HH:mm")
        private LocalDateTime localDateTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private ZonedDateTime zonedDateTime;
        @JSONField(format = "yyyy-MM-dd HH:mm")
        private OffsetDateTime offsetDateTime;
    }
}
