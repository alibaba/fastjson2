package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertAll;

public class DateFormatTestField_Local {
    @Test
    void localeDateTest() {
        final String date = "{\"today\": \"2022 March 10\"}";
        assertAll(() -> JSON.parseObject(date, Today.class));
    }

    @Test
    void localeDateTest_CN() {
        final String date = "{\"today\": \"2022 五月 10\"}";

        Today today;
        try (JSONReader reader = JSONReader.of(date)) {
            reader.getContext().setLocale(Locale.CHINESE);
            today = reader.read(Today.class);
        }
    }

    private static class Today {
        @JSONField(format = "yyyy MMMM dd")
        public Date today;
    }

    @Test
    void zhLocaleDateTest() {
        final String date = "{\"today\": \"2022 五月 10\"}";
        assertAll(() -> JSON.parseObject(date, TodayCN.class));
    }

    private static class TodayCN {
        @JSONField(format = "yyyy MMMM dd", locale = "zh_CN")
        public Date today;
    }
}
