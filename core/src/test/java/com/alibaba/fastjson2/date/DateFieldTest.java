package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateFieldTest {
    @Test
    public void test_codec() {
        long millis = 1654686106601L;
        V0 v = new V0();
        v.setValue(new Date(millis));

        String text = JSON.toJSONString(v, "millis");
        assertEquals("{\"value\":1654686106601}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null() {
        V0 v = new V0();

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteMapNullValue);
        assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null_asm() {
        V0 v = new V0();

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteMapNullValue);
        assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null_1() {
        V0 v = new V0();

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullNumberAsZero);
        assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(null, v1.getValue());
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
