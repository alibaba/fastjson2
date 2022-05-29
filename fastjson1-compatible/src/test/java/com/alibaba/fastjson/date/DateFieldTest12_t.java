package com.alibaba.fastjson.date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateFieldTest12_t {
    @BeforeEach
    protected void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        JSON.defaultLocale = Locale.CHINA;
    }

    @Test
    public void test_1() throws Exception {
        Entity object = new Entity();
        object.setValue(new Date());
        String text = JSON.toJSONString(object);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", JSON.defaultLocale);
        format.setTimeZone(JSON.defaultTimeZone);
        Assertions.assertEquals("{\"value\":\"" + format.format(object.getValue()) + "\"}", text);

        Entity object2 = JSON.parseObject(text, Entity.class);
    }

    public static class Entity {
        @JSONField(format = "yyyy-MM-ddTHH:mm:ssZ")
        private Date value;

        public Date getValue() {
            return value;
        }

        public void setValue(Date value) {
            this.value = value;
        }
    }
}
