package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializeConfig;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson.serializer.SerializerFeature.WriteMapNullValue;
import static com.alibaba.fastjson.serializer.SerializerFeature.WriteNullNumberAsZero;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteFieldTest {
    @Test
    public void test_codec() throws Exception {
        V0 v = new V0();
        v.setValue((byte) 10);

        String text = JSON.toJSONString(v);
        System.out.println(text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null() throws Exception {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, mapping, WriteMapNullValue);
        assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null_asm() throws Exception {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(true);

        String text = JSON.toJSONString(v, mapping, WriteMapNullValue);
        assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null_1() throws Exception {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, mapping, WriteMapNullValue, WriteNullNumberAsZero);
        assertEquals("{\"value\":0}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(Byte.valueOf((byte) 0), v1.getValue());
    }

    public static class V0 {
        private Byte value;

        public Byte getValue() {
            return value;
        }

        public void setValue(Byte value) {
            this.value = value;
        }
    }
}
