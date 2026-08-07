package com.alibaba.fastjson.date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateFieldTest8 {
    @BeforeEach
    protected void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        JSON.defaultLocale = Locale.CHINA;
    }

    @Test
    public void test_0() throws Exception {
        Entity object = new Entity();
        object.setValue(new Date());
        String text = JSON.toJSONStringWithDateFormat(object, "yyyy");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", JSON.defaultLocale);
        format.setTimeZone(JSON.defaultTimeZone);
        assertEquals("{\"value\":\"" + format.format(object.getValue()) + "\"}",
                text);
    }

    @Test
    public void test_1() throws Exception {
        Entity object = new Entity();
        object.setValue(new Date());
        String text = JSON.toJSONString(object);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", JSON.defaultLocale);
        format.setTimeZone(JSON.defaultTimeZone);
        assertEquals("{\"value\":\"" + format.format(object.getValue()) + "\"}",
                text);
    }

    public static class Entity {
        @JSONField(format = "yyyy-MM-dd")
        private Date value;

        public Date getValue() {
            return value;
        }

        public void setValue(Date value) {
            this.value = value;
        }
    }
}
