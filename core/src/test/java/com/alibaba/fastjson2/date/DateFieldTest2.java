package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateFieldTest2 {
    public TimeZone defaultTimeZone;
    public Locale defaultLocale;

    @BeforeEach
    protected void setUp() throws Exception {
        defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        defaultLocale = Locale.CHINA;
    }

    @Test
    public void test_codec() throws Exception {
        V0 v = new V0();
        v.setValue(new Date());

        String text = JSON.toJSONString(v);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", defaultLocale);
        format.setTimeZone(defaultTimeZone);
        assertEquals("{\"value\":" + JSON.toJSONString(format.format(v.getValue())) + "}", text);
    }

    @Test
    public void test_codec_null_asm() throws Exception {
        V0 v = new V0();

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteMapNullValue);
        assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null_1() throws Exception {
        V0 v = new V0();

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullNumberAsZero);
        assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(null, v1.getValue());
    }

    public static class V0 {
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
