package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinkedListFieldTest {
    @Test
    public void test_codec_null() throws Exception {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, mapping, SerializerFeature.WriteMapNullValue);
        assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null_1() throws Exception {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        assertEquals("{\"value\":[]}", JSON.toJSONString(v, mapping, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty));
        assertEquals("{\"value\":[]}", JSON.toJSONStringZ(v, mapping, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty));
        assertEquals("{'value':[]}", JSON.toJSONStringZ(v, mapping, SerializerFeature.UseSingleQuotes, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty));
        assertEquals("{'value':[]}", JSON.toJSONStringZ(v, mapping, SerializerFeature.UseSingleQuotes, SerializerFeature.QuoteFieldNames, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty));
    }

    public static class V0 {
        private LinkedList value;

        public LinkedList getValue() {
            return value;
        }

        public void setValue(LinkedList value) {
            this.value = value;
        }
    }
}
