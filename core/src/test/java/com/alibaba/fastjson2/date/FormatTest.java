package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormatTest {
    @Test
    public void test() {
        LocalDate date = LocalDate.of(2017, 12, 13);
        LocalDateTime dateTime = LocalDateTime.of(2017, 1, 2, 12, 13, 14);

        Bean bean = new Bean();
        bean.date = date;
        bean.dateTime = dateTime;

        JSON.mixIn(LocalDateTime.class, LocalDateTimeMixin.class);
        assertEquals("{\"date\":\"2017-12-13\",\"dateTime\":\"2017-01-02 12:13:14\"}", JSON.toJSONString(bean));

        // clear cache
        JSON.mixIn(LocalDateTime.class, null);
        JSON.mixIn(Bean.class, null);

        assertEquals("{\"date\":\"2017-12-13\",\"dateTime\":\"2017-01-02T12:13:14\"}", JSON.toJSONString(bean));

        JSON.mixIn(LocalDateTime.class, null);
        JSON.mixIn(Bean.class, null);

        JSON.mixIn(LocalDate.class, LocalDateMixin.class);
        assertEquals("\"2017-12-13 00:00:00\"", JSON.toJSONString(date));
        JSON.mixIn(LocalDate.class, null);
        assertEquals("\"2017-12-13\"", JSON.toJSONString(date));
    }

    @JSONType(format = "yyyy-MM-dd HH:mm:ss")
    public static class LocalDateMixin {
    }

    @JSONType(format = "yyyy-MM-dd HH:mm:ss")
    public static class LocalDateTimeMixin {
    }

    public static class Bean {
        public LocalDateTime dateTime;
        public LocalDate date;
    }
}
