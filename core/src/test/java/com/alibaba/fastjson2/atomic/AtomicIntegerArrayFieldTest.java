package com.alibaba.fastjson2.atomic;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicIntegerArray;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtomicIntegerArrayFieldTest {
    @Test
    public void test_codec_null() {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls);
        assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null_1() {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue);
        assertEquals("{\"value\":[]}", text);
    }

    @Test
    public void test_codec_null_2() {
        V0 v = JSON.parseObject("{\"value\":[1,2]}", V0.class);

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue);
        assertEquals("{\"value\":[1,2]}", text);
    }

    public static class V0 {
        private AtomicIntegerArray value;

        public AtomicIntegerArray getValue() {
            return value;
        }

        public void setValue(AtomicIntegerArray value) {
            this.value = value;
        }
    }
}
