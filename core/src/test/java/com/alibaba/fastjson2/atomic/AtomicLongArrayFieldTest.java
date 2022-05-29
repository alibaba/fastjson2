package com.alibaba.fastjson2.atomic;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLongArray;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtomicLongArrayFieldTest {
    @Test
    public void test_codec_null() {
        V0 v = new V0();

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls);
        assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null_1() {
        V0 v = new V0();

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue);
        assertEquals("{\"value\":[]}", text);
    }

    @Test
    public void test_codec_null_2() {
        V0 v = JSON.parseObject("{\"value\":[1,2]}", V0.class);

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue);
        assertEquals("{\"value\":[1,2]}", text);
    }

    public static class V0 {
        private AtomicLongArray value;

        public AtomicLongArray getValue() {
            return value;
        }

        public void setValue(AtomicLongArray value) {
            this.value = value;
        }
    }
}
