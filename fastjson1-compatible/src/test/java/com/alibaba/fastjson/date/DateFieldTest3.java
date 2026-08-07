package com.alibaba.fastjson.date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateFieldTest3 {
    @BeforeEach
    protected void setUp() {
        JSON.defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        JSON.defaultLocale = Locale.CHINA;
    }

    @Test
    public void test_codec() {
        V0 v = new V0();
        v.setValue(new Date());

        String text = JSON.toJSONStringWithDateFormat(v, "yyyy-MM-dd");

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", JSON.defaultLocale);
        format.setTimeZone(JSON.defaultTimeZone);
        Assertions.assertEquals("{\"value\":" + JSON.toJSONString(format.format(v.getValue())) + "}", text);
    }

    @Test
    public void test_codec_no_asm() {
        V0 v = new V0();
        v.setValue(new Date());

        String text = JSON.toJSONStringWithDateFormat(v, "yyyy-MM-dd", SerializerFeature.WriteMapNullValue);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", JSON.defaultLocale);
        format.setTimeZone(JSON.defaultTimeZone);
        Assertions.assertEquals("{\"value\":" + JSON.toJSONString(format.format(v.getValue())) + "}", text);
    }

    @Test
    public void test_codec_asm() {
        V0 v = new V0();
        v.setValue(new Date());

        String text = JSON.toJSONStringWithDateFormat(v, "yyyy-MM-dd", SerializerFeature.WriteMapNullValue);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", JSON.defaultLocale);
        format.setTimeZone(JSON.defaultTimeZone);
        Assertions.assertEquals("{\"value\":" + JSON.toJSONString(format.format(v.getValue())) + "}", text);
    }

    @Test
    public void test_codec_null_asm() {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(true);

        String text = JSON.toJSONString(v, mapping, SerializerFeature.WriteMapNullValue);
//        mapping.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
        Assertions.assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        Assertions.assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null_no_asm() {
        V0 v = new V0();

//        SerializeConfig mapping = new SerializeConfig();
//        mapping.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
//        mapping.setAsmEnable(false);

        String text = JSON.toJSONStringWithDateFormat(v, "yyyy-MM-dd", SerializerFeature.WriteMapNullValue);
        Assertions.assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        Assertions.assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null_1() {
        V0 v = new V0();

//        SerializeConfig mapping = new SerializeConfig();
//        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullNumberAsZero);
        Assertions.assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        Assertions.assertEquals(null, v1.getValue());
    }

    public static class V0 {
        private Date value;

        public Date getValue() {
            return value;
        }

        public void setValue(Date value) {
            this.value = value;
        }
    }
}
