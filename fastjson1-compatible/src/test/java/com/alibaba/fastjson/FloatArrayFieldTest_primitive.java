package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson.serializer.SerializerFeature.WriteMapNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatArrayFieldTest_primitive {
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
    public void test_codec_null_1() throws Exception {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, mapping, WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty);
        assertEquals("{\"value\":[]}", text);
    }

    public static class V0 {
        private float[] value;

        public float[] getValue() {
            return value;
        }

        public void setValue(float[] value) {
            this.value = value;
        }
    }
}
