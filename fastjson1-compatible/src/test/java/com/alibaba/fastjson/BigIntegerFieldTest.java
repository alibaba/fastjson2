package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigIntegerFieldTest {
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

        String text = JSON.toJSONString(v, mapping, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullNumberAsZero);
        assertEquals("{\"value\":0}", text);
    }

    public static class V0 {
        private BigInteger value;

        public BigInteger getValue() {
            return value;
        }

        public void setValue(BigInteger value) {
            this.value = value;
        }
    }
}
