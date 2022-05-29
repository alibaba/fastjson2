package com.alibaba.fastjson2.v1issues.date;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateFieldTest5 {
    @Test
    public void test_codec() {
        V0 v = new V0();
        v.setValue(new Date());

        String text = JSON.toJSONString(v);

        assertEquals("{\"value\":" + v.getValue().getTime() + "}", text);
    }

    @Test
    public void test_codec_no_asm() {
        V0 v = new V0();
        v.setValue(new Date());

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls);
        assertEquals("{\"value\":" + v.getValue().getTime() + "}", text);
    }

    @Test
    public void test_codec_null_asm() {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(true);

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls);
        assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null_1() {
        V0 v = new V0();

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls);
        assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(null, v1.getValue());
    }

    public static class V0 {
        private Date value;

        @JSONField(format = " millis")
        public Date getValue() {
            return value;
        }

        public void setValue(Date value) {
            this.value = value;
        }

        public boolean is() {
            return true;
        }

        public boolean isa() {
            return true;
        }

        public Object get() {
            return true;
        }

        public Object geta() {
            return true;
        }

        @JSONField(serialize = false)
        public Object getA() {
            return true;
        }

        public static Object getB() {
            return true;
        }
    }
}
