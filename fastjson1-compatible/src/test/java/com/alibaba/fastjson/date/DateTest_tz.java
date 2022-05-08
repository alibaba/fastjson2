package com.alibaba.fastjson.date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTest_tz {
    @BeforeEach
    protected void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        JSON.defaultLocale = Locale.CHINA;
    }

    @Test
    public void test_codec() throws Exception {
        JSONReader reader = new JSONReader(new StringReader("{\"value\":\"2016-04-29\"}"));
        reader.setLocale(Locale.CHINA);
        reader.setTimzeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        Model model = reader.readObject(Model.class);
        Assertions.assertNotNull(model.value);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date date = format.parse("2016-04-29");
        Assertions.assertEquals(date.getTime(), model.value.getTime());

        Assertions.assertEquals(TimeZone.getTimeZone("Asia/Shanghai"), reader.getTimeZone());
        Assertions.assertEquals(Locale.CHINA, reader.getLocal());

        reader.close();
    }

    public static class Model {
        public Date value;
    }
}
